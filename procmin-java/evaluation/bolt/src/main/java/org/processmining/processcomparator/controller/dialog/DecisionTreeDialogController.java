package org.processmining.processcomparator.controller.dialog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.processcomparator.model.decisiontree.DecisionPoint;
import org.processmining.processcomparator.view.dialog.decisiontree.DecisionMatrixCell;
import org.processmining.processcomparator.view.dialog.decisiontree.DecisionPointDialog2;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.misc.InputMappedClassifier;
import weka.classifiers.trees.J48;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

/**
 * This class generates and calculates all the contents of the
 * DecisionPointDialog.
 * 
 * @author abolt
 *
 */
public class DecisionTreeDialogController {

	private DecisionPointDialog2 dialog;
	private DecisionPoint dp;

	//private boolean splitted;

	public DecisionTreeDialogController(DecisionPoint dp) throws Exception {
		this.dp = dp;
		//splitted = false;
		initialize();
	}

	private void initialize() throws Exception {

		dialog = new DecisionPointDialog2(dp.getName());

		//sets the corner cell, the only non-interactive

		JPanel corner = new JPanel();
		corner.setPreferredSize(new Dimension(200, 200));
		corner.setLayout(new BoxLayout(corner, BoxLayout.Y_AXIS));
		Box box1 = Box.createHorizontalBox();
		//box1.add(Box.createHorizontalGlue());
		box1.add(new JLabel("Cells are interactive..."));
		//box1.add(Box.createHorizontalGlue());
		corner.add(box1);
		corner.add(Box.createVerticalStrut(5));
		Box box2 = Box.createHorizontalBox();
		//box2.add(Box.createHorizontalGlue());
		box2.add(new JLabel("...just click on them!"));
		//box2.add(Box.createHorizontalGlue());
		corner.add(box2);

		dialog.addCell(corner);
		dialog.addCell(makeColumnHeader("Group A", dp.getClassifierA(),
				new TreeVisualizer(null, ((InputMappedClassifier) dp.getClassifierA()).graph(), new PlaceNode2())));
		dialog.addCell(makeColumnHeader("Group B", dp.getClassifierB(),
				new TreeVisualizer(null, ((InputMappedClassifier) dp.getClassifierB()).graph(), new PlaceNode2())));
		dialog.addCell(makeColumnHeader("Agree/Disagree", dp.getClassifierSIM(),
				new TreeVisualizer(null, ((InputMappedClassifier) dp.getClassifierSIM()).graph(), new PlaceNode2())));

		dialog.addCell(makeRowHeader("Group A", dp.getTreeA_logA().numInstances()));
		dialog.addCell(new DecisionMatrixCell(dp.getTreeA_logA().correct(), dp.getTreeA_logA().incorrect(),
				"Correctly Classified", "Incorrectly Classified", dp.getTreeA_logA().confusionMatrix(),
				dp.getClasses()));
		dialog.addCell(new DecisionMatrixCell(dp.getTreeB_logA().correct(), dp.getTreeB_logA().incorrect(),
				"Correctly Classified", "Incorrectly Classified", dp.getTreeB_logA().confusionMatrix(),
				dp.getClasses()));
		dialog.addCell(new DecisionMatrixCell(
				dp.getTreeSIM_logA().confusionMatrix()[0][0] + dp.getTreeSIM_logA().confusionMatrix()[1][0],
				dp.getTreeSIM_logA().confusionMatrix()[0][1] + dp.getTreeSIM_logA().confusionMatrix()[1][1], "Agree",
				"Disagree", dp.getTreeSIM_logA().confusionMatrix(), dp.getClassesSIM()));

		dialog.addCell(makeRowHeader("Group B", dp.getTreeB_logB().numInstances()));
		dialog.addCell(new DecisionMatrixCell(dp.getTreeA_logB().correct(), dp.getTreeA_logB().incorrect(),
				"Correctly Classified", "Incorrectly Classified", dp.getTreeA_logB().confusionMatrix(),
				dp.getClasses()));
		dialog.addCell(new DecisionMatrixCell(dp.getTreeB_logB().correct(), dp.getTreeB_logB().incorrect(),
				"Correctly Classified", "Incorrectly Classified", dp.getTreeB_logB().confusionMatrix(),
				dp.getClasses()));
		dialog.addCell(new DecisionMatrixCell(
				dp.getTreeSIM_logB().confusionMatrix()[0][0] + dp.getTreeSIM_logB().confusionMatrix()[1][0],
				dp.getTreeSIM_logB().confusionMatrix()[0][1] + dp.getTreeSIM_logB().confusionMatrix()[1][1], "Agree",
				"Disagree", dp.getTreeSIM_logB().confusionMatrix(), dp.getClassesSIM()));

		dialog.addCell(
				makeRowHeader("Grpups A + B", dp.getTreeA_logA().numInstances() + dp.getTreeB_logB().numInstances()));
		dialog.addCell(new DecisionMatrixCell(dp.getTreeA_logA().correct() + dp.getTreeA_logB().correct(),
				dp.getTreeA_logA().incorrect() + dp.getTreeA_logB().incorrect(), "Correctly Classified",
				"Incorrectly Classified",
				sumConfusionMatrixes(dp.getTreeA_logA().confusionMatrix(), dp.getTreeA_logB().confusionMatrix()),
				dp.getClasses()));
		dialog.addCell(new DecisionMatrixCell(dp.getTreeB_logA().correct() + dp.getTreeB_logB().correct(),
				dp.getTreeB_logA().incorrect() + dp.getTreeB_logB().incorrect(), "Correctly Classified",
				"Incorrectly Classified",
				sumConfusionMatrixes(dp.getTreeB_logA().confusionMatrix(), dp.getTreeB_logB().confusionMatrix()),
				dp.getClasses()));
		dialog.addCell(new DecisionMatrixCell(
				dp.getTreeSIM_logAB().confusionMatrix()[0][0] + dp.getTreeSIM_logAB().confusionMatrix()[1][0],
				dp.getTreeSIM_logAB().confusionMatrix()[0][1] + dp.getTreeSIM_logAB().confusionMatrix()[1][1], "Agree",
				"Disagree", dp.getTreeSIM_logAB().confusionMatrix(), dp.getClassesSIM()));

		dialog.setAlwaysOnTop(true);
		dialog.setLocation(MouseInfo.getPointerInfo().getLocation());
		dialog.setPreferredSize(new Dimension(1000, 800));
		dialog.pack();

		dialog.setVisible(true);

	}

	public DecisionPoint getDP() {
		return dp;
	}

	private double[][] sumConfusionMatrixes(double[][] matrix1, double[][] matrix2) {
		double[][] result = new double[matrix1.length][matrix1[0].length];
		for (int i = 0; i < matrix1.length; i++)
			for (int j = 0; j < matrix1[0].length; j++)
				result[i][j] = matrix1[i][j] + matrix2[i][j];
		return result;
	}

	private JPanel makeRowHeader(String message, double numInstances) {
		JPanel container = new JPanel();
		container.setPreferredSize(new Dimension(200, 200));
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		Box box1 = Box.createVerticalBox();
		//box1.add(Box.createVerticalGlue());
		box1.add(new JLabel("Observation Instances:"));
		box1.add(new JLabel(message));
		box1.add(Box.createVerticalStrut(5));
		box1.add(new JLabel("# instances: " + numInstances));
		//box1.add(Box.createVerticalGlue());
		container.add(box1);
		return container;
	}

	private JPanel makeColumnHeader(String name, AbstractClassifier classif, JPanel tree) {

		assert (classif instanceof InputMappedClassifier);

		JPanel result = new JPanel();
		result.setPreferredSize(new Dimension(200, 200));
		result.setLayout(new BorderLayout());

		JPanel header;
		final JPanel container;
		JPanel buttons;

		//add the header (always visible)
		header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		Box box1 = Box.createVerticalBox();
		box1.add(new JLabel("Decision Tree trained with:"));
		box1.add(new JLabel("Observation Instances " + name));
		header.add(box1);

		result.add(header, BorderLayout.NORTH);

		//add the content
		container = new JPanel();
		container.setLayout(new CardLayout());

		J48 classifier = (J48) ((InputMappedClassifier)classif).getClassifier();

		JPanel text = new JPanel();
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
		Box box2 = Box.createVerticalBox();
		//box2.add(Box.createVerticalGlue());
		box2.add(new JLabel("Tree Statistics"));
		box2.add(Box.createVerticalStrut(5));
		box2.add(new JLabel("Tree Size = " + classifier.measureTreeSize()));
		box2.add(new JLabel("# Leafs = " + classifier.measureNumLeaves()));
		box2.add(new JLabel("# Rules = " + classifier.measureNumRules()));
		//box2.add(Box.createVerticalGlue());
		text.add(box2);

		container.setLayout(new CardLayout());
		container.add(text, "text");
		container.add(tree, "tree");
		result.add(container, BorderLayout.CENTER);

		buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		JButton showMetrics, showTree;
		showMetrics = new JButton("Show Tree Metrics");
		showMetrics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CardLayout c = (CardLayout) container.getLayout();
				c.show(container, "text");
				container.doLayout();
				container.repaint();
			}
		});
		showTree = new JButton("Show Decision Tree");
		showTree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CardLayout c = (CardLayout) container.getLayout();
				c.show(container, "tree");
				container.doLayout();
				container.repaint();
			}
		});
		Box box3 = Box.createHorizontalBox();
		box3.add(Box.createHorizontalGlue());
		box3.add(showMetrics);
		box3.add(Box.createHorizontalGlue());
		box3.add(showTree);
		box3.add(Box.createHorizontalGlue());
		buttons.add(box3);

		result.add(buttons, BorderLayout.SOUTH);

		return result;
	}

	public DecisionPointDialog2 getDialog() {
		return dialog;
	}

}

//dotModel = new Dot();
//Node rootNode = new TreeBuild().create(new StringReader(model.graph()));
//
//while (rootNode.getParent(0) != null)
//	rootNode = rootNode.getParent(0).getSource();
//
//Map<Node, DotNode> nodes = new HashMap<Node, DotNode>();
//Map<Edge, DotEdge> edges = new HashMap<Edge, DotEdge>();
//
//nodes.put(rootNode, dotModel.addNode(rootNode.getLabel()));
//
//for (int i = 0; rootNode.getChild(i) != null; i++) {
//	Edge edge = rootNode.getChild(i);
//	Node source = edge.getSource();
//	Node target = edge.getTarget();
//	nodes.put(target, dotModel.addNode(target.getLabel()));
//	edges.put(edge, dotModel.addEdge(nodes.get(source), nodes.get(target), edge.getLabel()));
//}