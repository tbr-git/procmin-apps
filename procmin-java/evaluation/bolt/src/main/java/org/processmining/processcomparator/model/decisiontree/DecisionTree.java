package org.processmining.processcomparator.model.decisiontree;

import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.datadiscovery.WekaDecisionTreeRuleDiscovery.TreeRule;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.processcomparator.algorithms.DecisionTreeUtils;
import org.processmining.processcomparator.model.SettingsObject;

public class DecisionTree {

	private TreeRule decisionTree;
	private String rootName;

	public DecisionTree(State source, Set<State> targets, XLog log, SettingsObject settings) {
		decisionTree = DecisionTreeUtils.createDecisionTree(log, source, targets, settings);
		rootName = source.getLabel();
	}

	public DecisionTree(TreeRule decisionTree) {
		this.decisionTree = decisionTree;
		if (decisionTree != null)
			rootName = decisionTree.getRootNode().toString();
	}

	public TreeRule getDecisionTree() {
		return decisionTree;
	}

	public String getRootName() {
		return rootName;
	}

}
