package org.processmining.processcomparator.view.dialog.decisiontree;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class DecisionTreeSettingsDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4969970679039546600L;
	private JTextField threshold;
	

	private JTextField percentageOfInstancesInLeaf, similarityThreshold;
	private JCheckBox chckbxBinaryBranchSplit, chckbxPruneTree;

	public DecisionTreeSettingsDialog() {
		setTitle("Decision Tree Settings");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		getContentPane().add(Box.createVerticalStrut(10));

		Box box1 = Box.createHorizontalBox();
		box1.add(Box.createHorizontalStrut(10));
		chckbxBinaryBranchSplit = new JCheckBox("Binary Tree");
		box1.add(chckbxBinaryBranchSplit);
		box1.add(Box.createHorizontalGlue());
		getContentPane().add(box1);

		Box box2 = Box.createHorizontalBox();
		box2.add(Box.createHorizontalStrut(10));
		chckbxPruneTree = new JCheckBox("Prune Tree");
		chckbxPruneTree.setSelected(true);
		box2.add(chckbxPruneTree);
		box2.add(Box.createHorizontalGlue());
		getContentPane().add(box2);

		Box box3 = Box.createHorizontalBox();
		box3.add(Box.createHorizontalStrut(10));
		JLabel lblConfidenceInterval = new JLabel("Confidence Threshold: ");
		box3.add(lblConfidenceInterval);
		threshold = new JTextField();
		threshold.setText("25");
		threshold.setHorizontalAlignment(SwingConstants.RIGHT);
		threshold.setColumns(4);
		threshold.setMaximumSize(threshold.getPreferredSize());
		box3.add(threshold);
		JLabel label = new JLabel(" %");
		box3.add(label);
		box3.add(Box.createHorizontalGlue());
		getContentPane().add(box3);

		Box box4 = Box.createHorizontalBox();
		box4.add(Box.createHorizontalStrut(10));
		JLabel lblMinimumPercentageOf = new JLabel("Minimum percentage of instances in a Leaf: ");
		box4.add(lblMinimumPercentageOf);
		percentageOfInstancesInLeaf = new JTextField();
		percentageOfInstancesInLeaf.setText("7");
		percentageOfInstancesInLeaf.setHorizontalAlignment(SwingConstants.RIGHT);
		percentageOfInstancesInLeaf.setColumns(4);
		percentageOfInstancesInLeaf.setMaximumSize(percentageOfInstancesInLeaf.getPreferredSize());
		box4.add(percentageOfInstancesInLeaf);
		JLabel label_1 = new JLabel(" %");
		box4.add(label_1);
		box4.add(Box.createHorizontalGlue());
		getContentPane().add(box4);
		
		Box box5 = Box.createHorizontalBox();
		box5.add(Box.createHorizontalStrut(10));
		JLabel lblSimilarityThreshold = new JLabel("Similarity Threshold: ");
		box5.add(lblSimilarityThreshold);
		similarityThreshold = new JTextField();
		similarityThreshold.setText("20");
		similarityThreshold.setHorizontalAlignment(SwingConstants.RIGHT);
		similarityThreshold.setColumns(4);
		similarityThreshold.setMaximumSize(similarityThreshold.getPreferredSize());
		box5.add(similarityThreshold);
		JLabel label_2 = new JLabel(" %");
		box5.add(label_2);
		box5.add(Box.createHorizontalGlue());
		getContentPane().add(box5);
		
		getContentPane().add(Box.createVerticalStrut(10));
	}

	public JTextField getConfidenceThreshold() {
		return threshold;
	}
	
	public JTextField getSimilarityThreshold() {
		return similarityThreshold;
	}

	public JTextField getPercentageOfInstancesInLeaf() {
		return percentageOfInstancesInLeaf;
	}

	public JCheckBox getChckbxBinaryBranchSplit() {
		return chckbxBinaryBranchSplit;
	}

	public JCheckBox getChckbxPruneTree() {
		return chckbxPruneTree;
	}
}
