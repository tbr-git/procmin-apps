package org.processmining.processcomparator.controller.settings.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.processmining.processcomparator.algorithms.Utils;
import org.processmining.processcomparator.algorithms.runners.TsSettingsRunner;
import org.processmining.processcomparator.controller.settings.SettingsController;
import org.processmining.processcomparator.model.TsSettingsObject;
import org.processmining.processcomparator.view.dialog.ProgressBarDialog;
import org.processmining.processcomparator.view.settings.component.GraphSettingsPanel;

public class GraphSettingsController {

	private SettingsController parent;
	private GraphSettingsPanel panel;
	private TsSettingsObject tsSettings;
	private Thread thread;

	public GraphSettingsController(SettingsController parent) {
		this.parent = parent;
		initialize();
	}

	private void initialize() {
		panel = new GraphSettingsPanel();

		//add the listener for the "open TS settings" button
		panel.getBtnOpenSettings().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO show the TS dialog, and then save the resulting settings object into tsSettings
				
				ProgressBarDialog pb = new ProgressBarDialog(5); //it can have 5 steps
				pb.addButtonListener(new CancelRunner(pb));
				pb.setUndecorated(true);

				panel.revalidate();
				pb.setLocationRelativeTo(panel);
				pb.setAlwaysOnTop(true);
				pb.setVisible(true);

				Utils.hideAllPopups(); //if there were any detailDialogs visible, hide them all

				//run!
				thread = new Thread(new TsSettingsRunner(pb, parent));
				thread.start();

				pb.dispose();
				try {
					Thread.currentThread().sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				panel.getRdbtnUseCustomSettings().setSelected(true);
			}
		});
		panel.doLayout();
		panel.repaint();
	}

	public JPanel getPanel() {
		return panel;
	}

	public boolean isDefaultTsSettingsSelected() {
		return panel.getRdbtnUseDefaultSettings().isSelected();
	}

	public boolean isCustomTsSettingsSelected() {
		return panel.getRdbtnUseCustomSettings().isSelected();
	}

	public String getStateThicknessSelection() {
		return (String) panel.getComboBoxState().getSelectedItem();
	}

	public String getTransitionThicknessSelection() {
		return (String) panel.getComboBoxTransition().getSelectedItem();
	}

	public boolean isFilterSelected() {
		return panel.getChckbxFilter().isSelected();
	}

	public double getFilterThreshold() {
		return Double.parseDouble(panel.getTextFieldFT().getText());
	}

	public boolean isShowTransitionLabelsSelected() {
		return panel.getChckbxShowTransitionLabels().isSelected();
	}

	public TsSettingsObject getTsSettings() {
		return tsSettings;
	}
	
	public void setTsSettings(TsSettingsObject settings) {
		tsSettings = settings;
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
