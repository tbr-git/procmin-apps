package org.processmining.processcomparator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingWorker;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.processcomparator.algorithms.DrawUtils;
import org.processmining.processcomparator.algorithms.SettingsUtils;
import org.processmining.processcomparator.algorithms.TransitionSystemUtils;
import org.processmining.processcomparator.algorithms.Utils;
import org.processmining.processcomparator.controller.dialog.ProcessSelectorController;
import org.processmining.processcomparator.controller.result.wrapper.ResultsWrapperController;
import org.processmining.processcomparator.controller.settings.SettingsController;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.model.ResultsObject;
import org.processmining.processcomparator.model.SettingsObject;
import org.processmining.processcomparator.view.ComparatorPanel;
import org.processmining.processcomparator.view.dialog.ProgressBarDialog;

/**
 * This class acts as the main bus for communications among controllers. Its
 * provided methods are triggered by the child controllers, and they may trigger
 * other child ontrollers as well, or mchange the objects kept in the bus.
 * 
 * This class also holds the models (Objects) used across the plugin.
 * 
 * @author abolt
 *
 */
public class MainController {

	/**
	 * The root panel is the main panel of the plugin. all other panels will be
	 * put on top of this one.
	 */
	private ComparatorPanel root;

	private InputObject input;
	private PluginContext pluginContext;
	private ResultsObject currentResults;

	/**
	 * Child controllers
	 */
	private ProcessSelectorController processSelectorController;
	private SettingsController settingsController;
	private ResultsWrapperController resultsController;

	private Thread thread;

	public MainController(PluginContext pluginContext, InputObject input) {
		this.input = input;
		this.pluginContext = pluginContext;

		initialize();
	}

	private void initialize() {

		//first use the process selector to get your input
		processSelectorController = new ProcessSelectorController(this, input);

		//now we create the wrapper panel that is returned
		root = new ComparatorPanel();

		//now we create the settings panel using that input and the empty results panel
		settingsController = new SettingsController(this, input);
		resultsController = new ResultsWrapperController(this);

		currentResults = TransitionSystemUtils.createResultsObject(pluginContext,
				settingsController.getStoredSettings(), input, null);
		applyTests(currentResults, settingsController.getStoredSettings());

		update(settingsController.getStoredSettings());

	}

	/**
	 * This method gets summoned when the update button is clicked and the
	 * settings changed. This is where the work is done.
	 */

	public void requestUpdate() {
		SettingsObject newSettings = SettingsUtils.createSettingsObject(settingsController);
		update(newSettings);
	}

	private void update(final SettingsObject settings) {

		final ProgressBarDialog pb = new ProgressBarDialog(5); //it can have 5 steps
		pb.addButtonListener(new CancelRunner(pb));
		pb.setUndecorated(true);
		pb.setModal(true);

		root.revalidate();
		pb.setLocationRelativeTo(root);
		pb.setAlwaysOnTop(true);
		

		Utils.hideAllPopups(); //if there were any detailDialogs visible, hide them all

		//run!
		System.out.println("dialog on " + Thread.currentThread());
		
		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			 @SuppressWarnings("fallthrough")
			@Override
			 protected Void doInBackground() throws Exception {
				 switch (settingsController.getStoredSettings().compareTo(settings)) {
						case 0 :
							break;
						case 1 : //everything has to be recalculated
							pb.setMaximum(5);
							setCurrentResults(TransitionSystemUtils.createResultsObject(getPluginContext(), settings,
									getInput(), pb));
						case 2 : //only tests and layout have to be recomputed
						case 3 : //only layout has to be recomputed (TODO: fix this separately)
							applyTests(settingsController.getMainController().getCurrentResults(), settings);
							break;
					}
				 settingsController.setStoredSettings(settings);
					
			 return null;
			 }

			 @Override
			 protected void done() {
			 pb.dispose();//close the modal dialog
			 }
			};

			sw.execute(); // this will start the processing on a separate thread
			pb.setVisible(true); //this will block user input as long as the processing task is working
			
			
			
			
//		
//		thread = new Thread(new ComparatorRunner(pb, settingsController, settings));
//		thread.start();
//		
//		pb.setVisible(true);
//
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		root.setResultsWrapperPanel(resultsController.getPanel());
		root.setSettingsPanel(settingsController.getPanel());

		root.revalidate();
		root.doLayout();
		root.repaint();
		//now that this finished, we dont need to recalculate the TS.

		//TransitionSystemUtils.createTSSettingsObject(pluginContext, input.getMerged());

	}

	public void openVariantSelector() {
		processSelectorController.showSelector();
	}

	public ComparatorPanel getPanel() {
		return root;
	}

	public PluginContext getPluginContext() {
		return pluginContext;
	}

	public InputObject getInput() {
		return input;
	}

	public void applyTests(ResultsObject input, SettingsObject settings) {
		resultsController.updateContents(
				DrawUtils.returnAsPanel(DrawUtils.createGraph(input, settings, root, this.input)));
	}

	public ResultsObject getCurrentResults() {
		return currentResults;
	}

	public void setCurrentResults(ResultsObject currentResults) {
		this.currentResults = currentResults;
	}

	class CancelRunner implements ActionListener {
		ProgressBarDialog pb;

		public CancelRunner(ProgressBarDialog p) {
			pb = p;
		}

		@SuppressWarnings("deprecation")
		public void actionPerformed(ActionEvent e) {
			pb.setVisible(false);
			pb.dispose();
			thread.stop(); //not safe... but screw you ProM for not letting me do it naturally!!!
		}
	}
}
