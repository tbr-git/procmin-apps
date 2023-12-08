package org.processmining.processcomparator.view.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;

public class SettingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1681644135238815580L;

	JButton updateButton;

	JPanel variantSettingsPanel;
	JPanel graphSettingsPanel;
	JPanel comparisonSettingsPanel;

	public SettingsPanel(JPanel panel1, JPanel panel2, JPanel panel3) {
		
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(500,700));
		
		JPanel content = new JPanel();
		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
						
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setPreferredSize(new Dimension(500, 700));

		Box box1 = Box.createHorizontalBox();
		box1.add(Box.createHorizontalStrut(10));
		variantSettingsPanel = panel1;
		box1.add(variantSettingsPanel);
		box1.add(Box.createHorizontalStrut(10));
		content.add(box1);

		content.add(Box.createVerticalStrut(10));

		JSeparator separator = new JSeparator();
		content.add(separator);

		content.add(Box.createVerticalStrut(10));

		Box box2 = Box.createHorizontalBox();
		box2.add(Box.createHorizontalStrut(10));
		graphSettingsPanel = panel2;
		box2.add(graphSettingsPanel);
		box2.add(Box.createHorizontalStrut(10));
		content.add(box2);

		content.add(Box.createVerticalStrut(10));

		JSeparator separator_1 = new JSeparator();
		content.add(separator_1);

		content.add(Box.createVerticalStrut(10));

		Box box3 = Box.createHorizontalBox();
		box3.add(Box.createHorizontalStrut(10));
		comparisonSettingsPanel = panel3;
		box3.add(comparisonSettingsPanel);
		box3.add(Box.createHorizontalStrut(10));
		content.add(box3);

		content.add(Box.createVerticalStrut(10));

		JSeparator separator_2 = new JSeparator();
		content.add(separator_2);
		
		content.add(Box.createVerticalStrut(10));

		Box box4 = Box.createHorizontalBox();
		box4.add(Box.createHorizontalGlue());
		updateButton = new JButton("Update Results");
		updateButton.setFont(new Font("Tahoma", Font.BOLD, 16));
		box4.add(updateButton);
		box4.add(Box.createHorizontalGlue());
		content.add(box4);

		content.add(Box.createVerticalStrut(10));

		content.add(Box.createVerticalGlue());
		
		add(scrollPane, BorderLayout.CENTER);
	}

	public JButton getButton() {
		return updateButton;
	}
}
