package org.processmining.processcomparator.algorithms;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import org.deckfour.xes.model.XLog;
import org.processmining.datadiscovery.BasicDecisionTreeImpl;
import org.processmining.datadiscovery.DecisionTreeConfig;
import org.processmining.datadiscovery.ProjectedLog;
import org.processmining.datadiscovery.RuleDiscoveryException;
import org.processmining.datadiscovery.WekaDecisionTreeRuleDiscovery;
import org.processmining.datadiscovery.WekaDecisionTreeRuleDiscovery.TreeRule;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.processcomparator.listeners.node.DecisionTreeNodeListener;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.model.ResultsObject;
import org.processmining.processcomparator.model.SettingsObject;
import org.processmining.processcomparator.model.decisiontree.DecisionPoint;
import org.processmining.processcomparator.model.decisiontree.projections.ProjectedLogTSImpl;
import org.processmining.processcomparator.view.ComparatorPanel;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.misc.InputMappedClassifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class DecisionTreeUtils {

	public static TreeRule createDecisionTree(XLog log, State source, Set<State> targets, SettingsObject settings) {

		ProjectedLog projectedLog = new ProjectedLogTSImpl(log, settings.getComparison_tree_selectedAttributes());

		DecisionTreeConfig config = settings.getDecisionTreeSettingsObject().getSettings();
		config.setMineDirectlyFollowingClasses(true);
		config.setUseWeights(false);

		WekaDecisionTreeRuleDiscovery<TreeRule> decisionTree = new BasicDecisionTreeImpl(config, projectedLog,
				log.size());

		try {
			TreeRule rule = decisionTree.discover(source, targets);
			return rule;

		} catch (RuleDiscoveryException e) {
			System.out.println("Something failed with the decision tree:\n");
			e.printStackTrace();
		}
		return null;

	}

	public static Dot enhanceWithDecisionTrees(ComparatorPanel root, Dot graph, ResultsObject results,
			SettingsObject settings, Map<NodeID, DotNode> nodes, Map<Object, DotEdge> edges, InputObject input)
			throws Exception {

		if (settings.getComparison_tree_selectedAttributes().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Please select at least one attribute to create Decision Trees.",
					"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			for (State s : results.getTs().getNodes()) {
				DotNode node = nodes.get(s.getId());
				if (node != null) {

					Set<State> targets = TransitionSystemUtils.getNextStates(results.getTs(), s);
					Set<State> auxTargets = new TreeSet<State>();
					auxTargets.addAll(targets);

					for (State auxState : auxTargets) {
						// if the target node is not visible, remove it from the possible targets
						if (nodes.get(auxState.getId()) == null)
							targets.remove(auxState);
					}
					auxTargets.clear();
					auxTargets.addAll(targets);

					for (State auxS : auxTargets) //if the transition is not visible, remove it as a valid target
						if (graph.getFirstEdge(node, nodes.get(auxS.getId())) == null)
							targets.remove(auxS);

					if (targets.size() >= 2) { //there is a decision point

						DecisionPoint dp = new DecisionPoint(s, auxTargets, input, settings);
						double percentAgree = (dp.getTreeSIM_logAB().confusionMatrix()[0][0]
								+ dp.getTreeSIM_logAB().confusionMatrix()[1][0]) / dp.getTreeSIM_logAB().numInstances();
						if (percentAgree < (settings.getComparison_tree_similarity() / 100))
							DrawUtils.highlightNode(nodes.get(s.getId()), Color.RED);
						else
							DrawUtils.highlightNode(nodes.get(s.getId()), Color.LIGHT_GRAY);

						nodes.get(s.getId()).addSelectionListener(new DecisionTreeNodeListener(root, s, dp));

					}
				}
			}
		}

		return graph;
	}

	public static Evaluation classifyUsingTree(AbstractClassifier tree, Instances training, Instances test,
			SettingsObject settings) throws Exception {

		Instances newTest = null;
		Classifier classifier = null;
		for (Instance i : test)
			if (!training.checkInstance(i))
				System.out.println("Not compatible...");
		if (testInstances(test, tree)) {
			newTest = test;
			classifier = tree;
		} else {
			InputMappedClassifier newClassifier = adaptClassifier(training, settings);
			newTest = adaptInstancesToClassifier(test, newClassifier);
			classifier = newClassifier;
			
		}
		System.out.println(classifier.toString());
		Evaluation result = new Evaluation(training);
		result.evaluateModel(classifier, newTest);

		return result;
	}

	/**
	 * takes instances and adds the predictions made by trees A and B as
	 * attributes, and changes the class to agree / disagree
	 * 
	 * DO NOT USE YET
	 * 
	 * @param instances
	 * @param treeA
	 * @param treeB
	 * @return
	 * @throws Exception
	 */

	public static Instances extendInstances(Instances instances, AbstractClassifier treeA, Instances instancesA,
			AbstractClassifier treeB, Instances instancesB) throws Exception {

		//first make a copy
		Instances newInstances = new Instances(instances);

		//add the new attributes
		ArrayList<String> fvClassVal = new ArrayList<String>();
		fvClassVal.add("agree");
		fvClassVal.add("disagree");
		Attribute classAttribute = new Attribute("theClass", fvClassVal, newInstances.numAttributes());
		newInstances.insertAttributeAt(classAttribute, newInstances.numAttributes());

		for (Instance instance : newInstances) {
			if (treeA.classifyInstance(instance) == treeB.classifyInstance(instance))
				instance.setValue(classAttribute, "agree");
			else
				instance.setValue(classAttribute, "disagree");
		}
		//set the class
		newInstances.renameAttribute(newInstances.numAttributes() - 1, "old_predicted_class");
		newInstances.setClassIndex(newInstances.numAttributes() - 1);

		return newInstances;
	}

	public static Instances[] getCorrectlyAndIncorrectlyClassifiedInstances(ArrayList<Prediction> predictions,
			Instances instances) {
		Instances[] result = new Instances[2]; //0 is for correct, 1 is for incorrect

		List<Instance> correctlyClassifiedList = new ArrayList<Instance>();
		List<Instance> incorrectlyClassifiedList = new ArrayList<Instance>();

		for (int i = 0; i < instances.size(); i++) {
			Instance instance = instances.get(i);
			Prediction prediction = predictions.get(i);

			if (prediction.actual() == prediction.predicted())
				correctlyClassifiedList.add(instance);
			else
				incorrectlyClassifiedList.add(instance);
		}

		Instances correct = new Instances(instances, correctlyClassifiedList.size());
		correct.clear();
		correct.addAll(correctlyClassifiedList);

		Instances incorrect = new Instances(instances, incorrectlyClassifiedList.size());
		incorrect.clear();
		incorrect.addAll(incorrectlyClassifiedList);

		result[0] = correct;
		result[1] = incorrect;
		return result;
	}

	public static boolean testInstances(Instances instances, AbstractClassifier classifier) {
		try {
			double result = 0;
			for (Instance i : instances)
				result = classifier.classifyInstance(i);
			if (result >= 0)
				return true;
			return false;
		} catch (Exception e) {
			System.out.println("Error: the provided instances cannot be classified by the provided Classifier");
			e.printStackTrace();
			return false;
		}
	}

	public static Instances adaptInstancesToClassifier(Instances test, InputMappedClassifier classifier)
			throws Exception {
		Instances result = new Instances(test, test.size());
		for (Instance i : test)
			result.add(classifier.constructMappedInstance(i));

		return result;
	}

	public static InputMappedClassifier adaptClassifier(Instances training, SettingsObject settings) throws Exception {
		InputMappedClassifier newClassifier = new InputMappedClassifier();
		J48 tree = new J48();
		tree.setBinarySplits(settings.isBinarySplit_DecisionTrees());
		tree.setConfidenceFactor(settings.getDecisionTreeSettingsObject().getConfidenceTThreshold());
		tree.setMinNumObj((int) (settings.getDecisionTreeSettingsObject().getMinPercentageOfObjsOnLeaf()
				* training.numInstances()));
		tree.setUnpruned(!settings.isPruneTrees_DecisionTrees());

		newClassifier.setClassifier(tree);
		newClassifier.setModelHeader(training);
		newClassifier.buildClassifier(training);
		newClassifier.setSuppressMappingReport(true);

		return newClassifier;

	}
}