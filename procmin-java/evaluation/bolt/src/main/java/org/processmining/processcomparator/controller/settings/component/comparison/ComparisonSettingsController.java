package org.processmining.processcomparator.controller.settings.component.comparison;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;

import org.processmining.processcomparator.algorithms.Utils;
import org.processmining.processcomparator.controller.settings.SettingsController;
import org.processmining.processcomparator.model.ConstantDefinitions;
import org.processmining.processcomparator.view.dialog.decisiontree.DecisionTreeSettingsDialog;
import org.processmining.processcomparator.view.settings.component.comparison.ComparisonSettingsPanel;
import org.processmining.processcomparator.view.settings.component.comparison.DecisionTreeSettingsPanel;
import org.processmining.processcomparator.view.settings.component.comparison.ProcessMetricSettingsPanel;

public class ComparisonSettingsController {

	private ComparisonSettingsPanel panel; //main panel

	private DecisionTreeSettingsPanel decisionTreeSettingsPanel;
	private ProcessMetricSettingsPanel processMetricSettingsPanel;

	private DecisionTreeSettingsDialog dialog;

	@SuppressWarnings("unused")
	private SettingsController parent;

	public ComparisonSettingsController(SettingsController parentController) {
		this.parent = parentController;
		decisionTreeSettingsPanel = new DecisionTreeSettingsPanel(
				Utils.getAttributeList(parentController.getVariantSettingsController().getInput().getMerged()));
		processMetricSettingsPanel = new ProcessMetricSettingsPanel(ConstantDefinitions.comparisonTypes);

		panel = new ComparisonSettingsPanel(decisionTreeSettingsPanel, processMetricSettingsPanel);
		dialog = new DecisionTreeSettingsDialog();
		
		panel.getMetricsRadioButton().addActionListener(new SettingsPanelListener());
		panel.getDecisionTreeRadioButton().addActionListener(new SettingsPanelListener());

		decisionTreeSettingsPanel.getOpenDecisionTreeSettingsButton()
				.addActionListener(new DecisionTreeSettingsDialogListener());

		refresh();
	}

	/**
	 * Getters for the process metrics comparison settings
	 * 
	 * @return
	 */
	public boolean isProcessMetricsSelected() {
		return panel.getMetricsRadioButton().isSelected();
	}

	public String getComparisonSelection_Metrics() {
		return (String) panel.getComboBox_Metrics().getSelectedItem();
	}

	public boolean isStatesSelected_Metrics() {
		return panel.getChckbxStates_Metrics().isSelected();
	}

	public boolean isTransitionsSelected_Metrics() {
		return panel.getChckbxTransitions_Metrics().isSelected();
	}

	public double getAlpha_Metrics() {
		return Double.parseDouble(panel.getAlphaTextField_Metrics().getText());
	}

	/**
	 * Getters for the decision trees comparison settings
	 * 
	 * @return
	 */

	public boolean isDecisionTreesSelected() {
		return panel.getDecisionTreeRadioButton().isSelected();
	}

	public List<String> getSelectedAttributes_DecisionTrees() {
		List<String> result = new ArrayList<String>();
		JList<String> list = panel.getList_DecisionTrees();
		for (int i : list.getSelectedIndices())
			result.add(list.getModel().getElementAt(i));
		for (String s : result)
			System.out.println("Selected: " + s);
		return result;
	}

	public boolean isAcurracySelected_DecisionTrees() {
		return panel.getAccurracyCheckBox_DecisionTrees().isSelected();
	}

	public double getAlpha_DecisionTrees() {
		return Double.parseDouble(panel.getAlphaTextField_DecisionTrees().getText());
	}

	/* TODO INCORPORATE TO THE COMPARATOR */
	public boolean isBinarySplit_DecisionTrees() {
		return dialog.getChckbxBinaryBranchSplit().isSelected();
	}

	/* TODO INCORPORATE TO THE COMPARATOR */
	public boolean isPruneTrees_DecisionTrees() {
		return dialog.getChckbxPruneTree().isSelected();
	}

	/* TODO INCORPORATE TO THE COMPARATOR */
	public float getConfidenceThreshold_DecisionTrees() {
		return Float.parseFloat(dialog.getConfidenceThreshold().getText());
	}
	
	public double getSimilarityThreshold_DecisionTrees() {
		return Double.parseDouble(dialog.getSimilarityThreshold().getText());
	}

	/* TODO INCORPORATE TO THE COMPARATOR */
	public double getPercentageOfInstancesOnLeaf_DecisionTrees() {
		return Double.parseDouble(dialog.getPercentageOfInstancesInLeaf().getText());
	}

	private class SettingsPanelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (panel.getMetricsRadioButton().isSelected()) {
				panel.setSettingsPanel(ComparisonSettingsPanel.processMetric);
			} else if (panel.getDecisionTreeRadioButton().isSelected()) {
				panel.setSettingsPanel(ComparisonSettingsPanel.decisionTree);
			}
			refresh();
		}
	}

	private class DecisionTreeSettingsDialogListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			dialog.setPreferredSize(new Dimension(350,200));
			dialog.pack();
			dialog.setVisible(true);
			
			refresh();

		}

	}

	private void refresh() {
		panel.doLayout();
		panel.repaint();
	}

	public JPanel getPanel() {
		return panel;
	}
}
