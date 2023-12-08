package org.processmining.processcomparator.view.dialog.decisiontree;

import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;

public class DecisionPointDialog2 extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8087553500840073274L;

	public DecisionPointDialog2(String title) {
		setTitle("Decision Point Explorer: " + title);
		//		GridBagLayout gridBagLayout = new GridBagLayout();
		//		gridBagLayout.columnWidths = new int[] { 0, 0 };
		//		gridBagLayout.rowHeights = new int[] { 0, 0 };
		//		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		//		gridBagLayout.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		getContentPane().setLayout(new GridLayout(0,4));
	}

	public void addCell(JPanel panel) {
//		GridBagConstraints gbc_panel = new GridBagConstraints();
//		gbc_panel.fill = GridBagConstraints.BOTH;
//		gbc_panel.gridx = x;
//		gbc_panel.gridy = y;
		getContentPane().add(panel);
	}

	public void clear() {
		getContentPane().removeAll();
	}
}
