package org.processmining.processcomparator.view.settings.component.comparison;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

public class DecisionTreeSettingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6458946883512425453L;
	private JList<String> list;
	private JTextField textField;
	private JCheckBox chckbxCompareCrossaccurracyOf;
	private JButton openSettings;

	public DecisionTreeSettingsPanel(List<String> availableAttributes) {
		setBackground(Color.LIGHT_GRAY);
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		Box box1 = Box.createHorizontalBox();
		JLabel lblUseTheFollowing = new JLabel("Build Decision Trees using the following data attributes:");
		box1.add(lblUseTheFollowing);
		box1.add(Box.createHorizontalGlue());
		add(box1);

		Box box2 = Box.createHorizontalBox();
		list = new JList<String>(availableAttributes.toArray(new String[availableAttributes.size()]));
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setViewportView(list);
		box2.add(listScroller);
		add(box2);

		Box box5 = Box.createHorizontalBox();
		box5.add(Box.createHorizontalGlue());
		openSettings = new JButton("Open Decision Tree Settings");
		openSettings.setFont(new Font("Tahoma", Font.BOLD, 13));
		box5.add(openSettings);
		box5.add(Box.createHorizontalGlue());
		add(box5);
		
		Box box6 = Box.createHorizontalBox();
		JLabel lblColorLegend = new JLabel("Color Legend:");
		lblColorLegend.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lblColorLegend.setFont(new Font("Tahoma", Font.PLAIN, 13));
		box6.add(lblColorLegend);
		box6.add(Box.createHorizontalGlue());
		add(box6);

		Box box7 = Box.createHorizontalBox();
		JLabel lblNewLabel_1 = new JLabel("DecTree");
		//ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("/images/decisiontree.png"));
		//lblNewLabel_1.setIcon(imageIcon);
		box7.add(lblNewLabel_1);
		add(box7);
	}

	public JList<String> getList() {
		return list;
	}

	public JTextField getAlphaTextField() {
		return textField;
	}

	public JCheckBox getCrossAccurracyCheckBox() {
		return chckbxCompareCrossaccurracyOf;
	}
	
	public JButton getOpenDecisionTreeSettingsButton(){
		return openSettings;
	}
}
