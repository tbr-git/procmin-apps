package org.processmining.processcomparator.view.settings.component;

import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.processmining.processcomparator.model.ConstantDefinitions;

public class GraphSettingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8182217251486973679L;

	private JRadioButton rdbtnUseDefaultSettings, rdbtnUseCustomSettings;
	private JComboBox<String> comboBoxState, comboBoxTransition;
	private JCheckBox chckbxFilter, chckbxShowTransitionLabels;
	private JTextField textFieldFT;
	private JButton btnOpenSettings;
	private JLabel lblTransitionSystemSettings;

	public GraphSettingsPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		Box box0 = Box.createHorizontalBox();
		box0.add(Box.createHorizontalGlue());
		lblTransitionSystemSettings = new JLabel("Transition System Settings");
		lblTransitionSystemSettings.setHorizontalAlignment(SwingConstants.CENTER);
		lblTransitionSystemSettings.setFont(new Font("Tahoma", Font.BOLD, 14));
		box0.add(lblTransitionSystemSettings);
		box0.add(Box.createHorizontalGlue());
		add(box0);
		
		add(Box.createVerticalStrut(5));
		ButtonGroup group = new ButtonGroup();

		Box box1 = Box.createHorizontalBox();
		rdbtnUseDefaultSettings = new JRadioButton("Use Default Settings");
		rdbtnUseDefaultSettings.setSelected(true);
		group.add(rdbtnUseDefaultSettings);
		box1.add(rdbtnUseDefaultSettings);
		box1.add(Box.createHorizontalGlue());
		add(box1);

		Box box2 = Box.createHorizontalBox();
		rdbtnUseCustomSettings = new JRadioButton("Use Custom Settings");
		group.add(rdbtnUseCustomSettings);
		box2.add(rdbtnUseCustomSettings);
		btnOpenSettings = new JButton("Open Settings");
		box2.add(btnOpenSettings);
		box2.add(Box.createHorizontalGlue());
		add(box2);

		add(Box.createVerticalStrut(5));

		Box box3 = Box.createHorizontalBox();
		JLabel lblGraphProperties = new JLabel("Graph Properties:");
		lblGraphProperties.setFont(new Font("Tahoma", Font.BOLD, 13));
		box3.add(lblGraphProperties);
		box3.add(Box.createHorizontalGlue());
		add(box3);

		add(Box.createVerticalStrut(5));
		
		Box box4 = Box.createHorizontalBox();
		box4.add(Box.createHorizontalGlue());
		JLabel lblStatenodeThickness = new JLabel("State (Node) thickness represents: ");
		box4.add(lblStatenodeThickness);
		comboBoxState = new JComboBox<String>(ConstantDefinitions.stateThicknessTypes);
		comboBoxState.setMaximumSize(comboBoxState.getPreferredSize());
		box4.add(comboBoxState);
		add(box4);

		Box box5 = Box.createHorizontalBox();
		box5.add(Box.createHorizontalGlue());
		JLabel label = new JLabel("Transition (Arc) thickness represents: ");
		box5.add(label);
		comboBoxTransition = new JComboBox<String>(ConstantDefinitions.transitionThicknessTypes);
		comboBoxTransition.setMaximumSize(comboBoxTransition.getPreferredSize());
		box5.add(comboBoxTransition);
		add(box5);

		add(Box.createVerticalStrut(5));

		Box box6 = Box.createHorizontalBox();
		JLabel lblGraphFilter = new JLabel("Graph Filter:");
		lblGraphFilter.setFont(new Font("Tahoma", Font.BOLD, 13));
		box6.add(lblGraphFilter);
		box6.add(Box.createHorizontalGlue());
		add(box6);
		
		add(Box.createVerticalStrut(5));

		Box box7 = Box.createHorizontalBox();
		chckbxFilter = new JCheckBox("Filter elements below the frequency threshold: ");
		chckbxFilter.setSelected(true);
		box7.add(chckbxFilter);
		textFieldFT = new JTextField();
		textFieldFT.setHorizontalAlignment(SwingConstants.TRAILING);
		textFieldFT.setMaximumSize(textFieldFT.getPreferredSize());
		textFieldFT.setText("5");
		textFieldFT.setColumns(4);
		box7.add(textFieldFT);
		JLabel label_1 = new JLabel(" %");
		box7.add(label_1);
		box7.add(Box.createHorizontalGlue());
		add(box7);

		Box box8 = Box.createHorizontalBox();
		chckbxShowTransitionLabels = new JCheckBox("Show transition labels");
		box8.add(chckbxShowTransitionLabels);
		box8.add(Box.createHorizontalGlue());
		add(box8);
	}

	public JRadioButton getRdbtnUseDefaultSettings() {
		return rdbtnUseDefaultSettings;
	}

	public JRadioButton getRdbtnUseCustomSettings() {
		return rdbtnUseCustomSettings;
	}

	public JComboBox<String> getComboBoxState() {
		return comboBoxState;
	}

	public JComboBox<String> getComboBoxTransition() {
		return comboBoxTransition;
	}

	public JCheckBox getChckbxFilter() {
		return chckbxFilter;
	}

	public JCheckBox getChckbxShowTransitionLabels() {
		return chckbxShowTransitionLabels;
	}

	public JTextField getTextFieldFT() {
		return textFieldFT;
	}

	public JButton getBtnOpenSettings() {
		return btnOpenSettings;
	}
}
