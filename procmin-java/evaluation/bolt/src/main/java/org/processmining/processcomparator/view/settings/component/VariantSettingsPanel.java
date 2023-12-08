package org.processmining.processcomparator.view.settings.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.deckfour.xes.model.XLog;
import org.processmining.processcomparator.model.InputObject;

public class VariantSettingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3257313215517468117L;
	
	private JButton btnVariantSelector;
	JTextArea textArea_A, textArea_B;

	public VariantSettingsPanel(InputObject input) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		Box box0 = Box.createHorizontalBox();
		box0.add(Box.createHorizontalGlue());
		JLabel lblVariantSettings_1 = new JLabel("Process Variant Settings");
		lblVariantSettings_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblVariantSettings_1.setFont(new Font("Tahoma", Font.BOLD, 14));
		box0.add(lblVariantSettings_1);
		box0.add(Box.createHorizontalGlue());
		add(box0);
		
		add(Box.createVerticalStrut(5));
		
		Box box1 = Box.createHorizontalBox();
		JLabel groupALabel = new JLabel("Group A: ");
		box1.add(groupALabel);
		add(box1);
		textArea_A = new JTextArea();
		textArea_A.setEditable(false);
//		textArea_A.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		JScrollPane panel_A = new JScrollPane(textArea_A);
		panel_A.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		box1.add(panel_A);
		
		add(Box.createVerticalStrut(1));

		Box box2 = Box.createHorizontalBox();
		JLabel groupBLabel = new JLabel("Group B: ");
		box2.add(groupBLabel);
		textArea_B = new JTextArea();
		textArea_B.setEditable(false);
//		textArea_B.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		JScrollPane panel_B = new JScrollPane(textArea_B);
		panel_B.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		box2.add(panel_B);
		add(box2);
		
		add(Box.createVerticalStrut(5));
		
		Box box3 = Box.createHorizontalBox();
		box3.add(Box.createHorizontalGlue());
		btnVariantSelector = new JButton("Open Variant Selector");
		btnVariantSelector.setAlignmentX(Component.CENTER_ALIGNMENT);
		box3.add(btnVariantSelector);
		box3.add(Box.createHorizontalGlue());
		add(box3);
	
		updateLists(input);
	}

	public void updateLists(InputObject input) {

		String variantList_A = "";
		for (XLog log : input.getSelected_A())
			variantList_A = variantList_A + log.getAttributes().get("concept:name").toString() + "\n";
		textArea_A.setText(variantList_A);

		String variantList_B = "";
		for (XLog log : input.getSelected_B())
			variantList_B = variantList_B + log.getAttributes().get("concept:name").toString() + "\n";
		textArea_B.setText(variantList_B);

		this.doLayout();
		this.repaint();
	}

	public JButton getButton() {
		return btnVariantSelector;
	}

	
}
