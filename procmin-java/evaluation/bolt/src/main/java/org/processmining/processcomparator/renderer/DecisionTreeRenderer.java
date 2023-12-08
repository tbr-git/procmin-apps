package org.processmining.processcomparator.renderer;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.processmining.datadiscovery.WekaDecisionTreeRuleDiscovery.TreeRule;
import org.processmining.datadiscovery.estimators.weka.WekaTreeClassificationAdapter.WekaCondition;
import org.processmining.datadiscovery.estimators.weka.WekaTreeClassificationAdapter.WekaLeafNode;
import org.processmining.datadiscovery.estimators.weka.WekaTreeClassificationAdapter.WekaNode;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.processcomparator.model.decisiontree.DecisionTree;

import com.google.common.collect.TreeTraverser;

import weka.classifiers.trees.J48;

public class DecisionTreeRenderer {

	public static DotPanel visualizeDecisionTree(DecisionTree decisionTree) {

		Dot graph = new Dot();

		TreeRule tree = decisionTree.getDecisionTree();

		Map<WekaNode, DotNode> nodes = new HashMap<WekaNode, DotNode>();
		Map<WekaCondition, DotEdge> edges = new HashMap<WekaCondition, DotEdge>();

		WekaNode root = tree.getRootNode();

		nodes.put(root, graph.addNode(decisionTree.getRootName()));

		recursiveDecisionTreeRenderer(graph, tree, nodes, edges, root);

		return new DotPanel(graph);
	}

	private static void recursiveDecisionTreeRenderer(Dot graph, TreeRule tree, Map<WekaNode, DotNode> nodes,
			Map<WekaCondition, DotEdge> edges, WekaNode currentNode) {

		TreeTraverser<WekaNode> traverser = tree.treeTraverser();
		for (WekaNode leaf : traverser.children(currentNode)) {
			if (leaf instanceof WekaLeafNode) {
				nodes.put(leaf,
						graph.addNode(
								((WekaLeafNode) leaf).getClassName() + " (" + ((WekaLeafNode) leaf).getInstanceCount()
										+ "/" + ((WekaLeafNode) leaf).getWrongInstanceCount() + ")"));
				edges.put(leaf.getCondition(), graph.addEdge(nodes.get(currentNode), nodes.get(leaf),
						leaf.getCondition().toExpressionString()));
			} else {
				nodes.put(leaf, graph.addNode(" "));
				edges.put(leaf.getCondition(), graph.addEdge(nodes.get(currentNode), nodes.get(leaf),
						leaf.getCondition().toExpressionString()));
				recursiveDecisionTreeRenderer(graph, tree, nodes, edges, leaf);
			}
		}

	}

	public JPanel renderWekaTree(J48 model) throws Exception {

//		Dot dotModel = new Dot();
//		Node rootNode = new TreeBuild().create(new StringReader(model.graph()));
//		dotModel.addNode(rootNode);
//		
		return new JPanel();
	}
}
