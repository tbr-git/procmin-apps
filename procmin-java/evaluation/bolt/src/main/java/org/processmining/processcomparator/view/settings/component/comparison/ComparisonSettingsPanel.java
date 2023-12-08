package org.processmining.processcomparator.view.settings.component.comparison;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ComparisonSettingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3839052176044484222L;

	public static final String decisionTree = "decisionTree", processMetric = "processMetric";

	private JRadioButton metricsRadioButton, decisionTreeRadioButton;
	private JPanel settingsPanel;
	private DecisionTreeSettingsPanel decisionTreeSettingsPanel;
	private ProcessMetricSettingsPanel processMetricSettingsPanel;

	public ComparisonSettingsPanel(DecisionTreeSettingsPanel decisionTreeSettingsPanel,
			ProcessMetricSettingsPanel processMetricSettingsPanel) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.decisionTreeSettingsPanel = decisionTreeSettingsPanel;
		this.processMetricSettingsPanel = processMetricSettingsPanel;

		Box box1 = Box.createHorizontalBox();
		box1.add(Box.createHorizontalGlue());
		JLabel lblComparisonSettings = new JLabel("Comparison Settings");
		lblComparisonSettings.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblComparisonSettings.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblComparisonSettings.setHorizontalAlignment(SwingConstants.CENTER);
		box1.add(lblComparisonSettings);
		box1.add(Box.createHorizontalGlue());
		add(box1);

		add(Box.createVerticalStrut(5));

		Box box2 = Box.createHorizontalBox();
		metricsRadioButton = new JRadioButton("Compare Annotations (Process Metrics)");
		metricsRadioButton.setSelected(true);
		box2.add(metricsRadioButton);
		box2.add(Box.createHorizontalGlue());
		add(box2);

		Box box3 = Box.createHorizontalBox();
		decisionTreeRadioButton = new JRadioButton("Compare Decision Making (Decision Trees)");
		box3.add(decisionTreeRadioButton);
		box3.add(Box.createHorizontalGlue());
		add(box3);

		ButtonGroup group = new ButtonGroup();
		group.add(metricsRadioButton);
		group.add(decisionTreeRadioButton);

		Box box4 = Box.createHorizontalBox();
		settingsPanel = new JPanel();
		settingsPanel.setLayout(new CardLayout(0, 0));
		settingsPanel.add(this.processMetricSettingsPanel, processMetric);
		settingsPanel.add(this.decisionTreeSettingsPanel, decisionTree);
		box4.add(settingsPanel);
		add(box4);
	}

	public void setSettingsPanel(String newPanel) {
		//assumes all listeners have been set before
		CardLayout cl = (CardLayout) (settingsPanel.getLayout());
		cl.show(settingsPanel, newPanel);
		this.doLayout();
		this.repaint();
	}
	
	/**
	 * Getters for compnents of the metrics comparison settings panel
	 * 
	 * @return
	 */
	public JRadioButton getMetricsRadioButton() {
		//to check if it is selected
		return metricsRadioButton;
	}

	public JComboBox<String> getComboBox_Metrics() {
		return processMetricSettingsPanel.getComboBox();
	}

	public JCheckBox getChckbxStates_Metrics() {
		return processMetricSettingsPanel.getChckbxStates();
	}

	public JCheckBox getChckbxTransitions_Metrics() {
		return processMetricSettingsPanel.getChckbxTransitions();
	}

	public JTextField getAlphaTextField_Metrics() {
		return processMetricSettingsPanel.getAlphaTextField();
	}

	/**
	 * Getters for compnents of the decision trees comparison settings panel
	 * 
	 * @return
	 */

	public JRadioButton getDecisionTreeRadioButton() {
		//to check if it is selected
		return decisionTreeRadioButton;
	}

	public JList<String> getList_DecisionTrees() {
		return decisionTreeSettingsPanel.getList();
	}

	public JTextField getAlphaTextField_DecisionTrees() {
		return decisionTreeSettingsPanel.getAlphaTextField();
	}

	public JCheckBox getAccurracyCheckBox_DecisionTrees() {
		return decisionTreeSettingsPanel.getCrossAccurracyCheckBox();
	}
}
