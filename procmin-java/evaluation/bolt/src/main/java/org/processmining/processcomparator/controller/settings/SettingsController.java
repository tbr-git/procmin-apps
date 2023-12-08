package org.processmining.processcomparator.controller.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.processmining.processcomparator.algorithms.SettingsUtils;
import org.processmining.processcomparator.controller.MainController;
import org.processmining.processcomparator.controller.settings.component.GraphSettingsController;
import org.processmining.processcomparator.controller.settings.component.VariantSettingsController;
import org.processmining.processcomparator.controller.settings.component.comparison.ComparisonSettingsController;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.model.SettingsObject;
import org.processmining.processcomparator.view.settings.SettingsPanel;

/**
 * This class controlls the settings panel and keeps the current state of the
 * settings. It also provides a reference to the main controller.
 * 
 * @author abolt
 *
 */
public class SettingsController {

	//parent controller
	private MainController parent;

	//child controllers
	private VariantSettingsController variantSettingsController;
	private GraphSettingsController graphSettingsController;
	private ComparisonSettingsController comparisonSettingsController;

	//panel
	private SettingsPanel panel;

	//current state settings
	private SettingsObject current;

	//input
	private InputObject input;

	public SettingsController(MainController mainController, InputObject input) {
		this.parent = mainController;
		this.input = input;
		initialize();
	}

	private void initialize() {

		variantSettingsController = new VariantSettingsController(this, input);
		graphSettingsController = new GraphSettingsController(this);
		comparisonSettingsController = new ComparisonSettingsController(this);

		panel = new SettingsPanel(variantSettingsController.getPanel(), graphSettingsController.getPanel(),
				comparisonSettingsController.getPanel());

		//add update button behavior
		panel.getButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				triggerUpdate();
			}
		});

		current = SettingsUtils.createSettingsObject(input, parent.getPluginContext());
	}

	public void triggerUpdate() {
		//trigger the mainController to do stuff...
		parent.requestUpdate();
	}

	public SettingsPanel getPanel() {
		return panel;
	}
	
	public MainController getMainController(){
		return parent;
	}

	public void openVariantSelector() {
		parent.openVariantSelector();
	}

	public VariantSettingsController getVariantSettingsController() {
		return variantSettingsController;
	}

	public GraphSettingsController getGraphSettingsController() {
		return graphSettingsController;
	}

	public ComparisonSettingsController getComparisonSettingsController() {
		return comparisonSettingsController;
	}

	public SettingsObject getStoredSettings() {
		return current;
	}
	
	public void setStoredSettings(SettingsObject settings){
		current = settings;
	}
}
