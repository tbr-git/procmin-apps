package org.processmining.processcomparator.view.settings.component.comparison;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ProcessMetricSettingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7213228600936499096L;

	private JComboBox<String> comboBox;
	private JCheckBox chckbxStates, chckbxTransitions;
	private JTextField textField;

	public ProcessMetricSettingsPanel(List<String> metricsList) {
		setBackground(Color.LIGHT_GRAY);
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		

		Box box1 = Box.createHorizontalBox();
		JLabel lblAnalyze = new JLabel("Process Metric: ");
		box1.add(lblAnalyze);
		comboBox = new JComboBox<String>(metricsList.toArray(new String[metricsList.size()]));
		comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboBox.getPreferredSize().height));
		box1.add(comboBox);
		add(box1);

		Box box2 = Box.createHorizontalBox();
		box2.add(Box.createHorizontalGlue());
		chckbxStates = new JCheckBox("Include States");
		chckbxStates.setSelected(true);
		chckbxStates.setBackground(Color.LIGHT_GRAY);
		box2.add(chckbxStates);
		chckbxTransitions = new JCheckBox("Include Transitions");
		chckbxTransitions.setSelected(true);
		chckbxTransitions.setBackground(Color.LIGHT_GRAY);
		box2.add(chckbxTransitions);
		add(box2);

		Box box3 = Box.createHorizontalBox();
		box3.add(Box.createHorizontalGlue());
		JLabel lblNewLabel = new JLabel("Alpha significance level (\u03B1): ");
		box3.add(lblNewLabel);
		textField = new JTextField("5");
		textField.setHorizontalAlignment(SwingConstants.TRAILING);
		textField.setMaximumSize(textField.getPreferredSize());
		textField.setColumns(4);
		box3.add(textField);
		JLabel label = new JLabel(" %");
		box3.add(label);
		add(box3);

		Box box4 = Box.createHorizontalBox();
		JLabel label_1 = new JLabel("Color Legend:");
		box4.add(label_1);
		box4.add(Box.createHorizontalGlue());
		add(box4);

		Box box5 = Box.createHorizontalBox();
		JLabel label_2 = new JLabel("MetricPic");
		//ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("/images/metrics.png"));
		//label_2.setIcon(imageIcon);
		box5.add(label_2);
		add(box5);
		
		Box box6 = Box.createHorizontalBox();
		JLabel label_6 = new JLabel("Red colors indicate that the metric is larger for variants of Group B.");
		box6.add(label_6);
		box6.add(Box.createHorizontalGlue());
		add(box6);
		
		Box box7 = Box.createHorizontalBox();
		JLabel label_7 = new JLabel("Blue colors indicate that the metric is larger for variants of Group A.");
		box7.add(label_7);
		box7.add(Box.createHorizontalGlue());
		add(box7);
	}

	public JComboBox<String> getComboBox() {
		return comboBox;
	}

	public JCheckBox getChckbxStates() {
		return chckbxStates;
	}

	public JCheckBox getChckbxTransitions() {
		return chckbxTransitions;
	}

	public JTextField getAlphaTextField() {
		return textField;
	}
}
