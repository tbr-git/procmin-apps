package org.processmining.processcomparator.model.decisiontree;

import java.util.Enumeration;
import java.util.Set;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.processcomparator.algorithms.DecisionTreeUtils;
import org.processmining.processcomparator.algorithms.Utils;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.model.SettingsObject;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.misc.InputMappedClassifier;
import weka.core.Instance;
import weka.core.Instances;

public class DecisionPoint {

	//	DecisionTree treeA, treeB, treeSIM;
	//private InputMappedClassifier classifierA, classifierB, classifierSIM;
	private AbstractClassifier classifierA, classifierB, classifierSIM;
	private DecisionTree dtA, dtB;
	private String name;

	//classifications using tree A
	private Evaluation treeA_logA, treeA_logA_correct, treeA_logA_incorrect;
	private Evaluation treeA_logB, treeA_logB_correct, treeA_logB_incorrect;
	private Evaluation treeA_logAB, treeA_logAB_correct, treeA_logAB_incorrect;

	//classification using tree B
	private Evaluation treeB_logA, treeB_logA_correct, treeB_logA_incorrect;
	private Evaluation treeB_logB, treeB_logB_correct, treeB_logB_incorrect;
	private Evaluation treeB_logAB, treeB_logAB_correct, treeB_logAB_incorrect;

	//classification using tree Sim
	private Evaluation treeSIM_logA, treeSIM_logA_correct, treeSIM_logA_incorrect;
	private Evaluation treeSIM_logB, treeSIM_logB_correct, treeSIM_logB_incorrect;
	private Evaluation treeSIM_logAB, treeSIM_logAB_correct, treeSIM_logAB_incorrect;

	private Object[] classes, classesSIM;

	public DecisionPoint(State source, Set<State> targets, InputObject input, SettingsObject settings)
			throws Exception {

		//Instances original and extended
		Instances instancesA, instancesA_correct, instancesA_incorrect;
		Instances instancesA_extended, instancesA_correct_extended, instancesA_incorrect_extended;

		Instances instancesB, instancesB_correct, instancesB_incorrect;
		Instances instancesB_extended, instancesB_correct_extended, instancesB_incorrect_extended;

		Instances instancesAll_extended;

		int numInstancesA = 0, numInstancesB = 0;
		name = source.getLabel();
		Instances[] aux = null;

		DecisionTree treeA = new DecisionTree(source, targets, Utils.mergeLogs(input.getSelected_A()), settings);
		dtA = treeA;
		numInstancesA = treeA.getDecisionTree().getInstances().numInstances();
		
		//classifierA = DecisionTreeUtils.buildMappableClassifier(treeA);
		//classifierA = treeA.getDecisionTree().getClassifier();

		DecisionTree treeB = new DecisionTree(source, targets, Utils.mergeLogs(input.getSelected_B()), settings);
		dtB = treeB;
		numInstancesB = treeB.getDecisionTree().getInstances().numInstances();
		//classifierB = DecisionTreeUtils.buildMappableClassifier(treeB);
		//classifierB = treeB.getDecisionTree().getClassifier();

		//Similarity Tree
		DecisionTree treeSIM = new DecisionTree(source, targets, input.getMerged(), settings);
		Instances instancesAll = treeSIM.getDecisionTree().getInstances();
		instancesA = new Instances(instancesAll, numInstancesA);
		instancesB = new Instances(instancesAll, numInstancesB);
		
		InputMappedClassifier auxClassifier = DecisionTreeUtils.adaptClassifier(instancesAll, settings);
		
		Instances instaA =  treeA.getDecisionTree().getInstances();
		for(Instance i : instaA){
			instancesA.add(auxClassifier.constructMappedInstance(i));
		}
		Instances instaB =  treeB.getDecisionTree().getInstances();
		for(Instance i : instaB){
			instancesB.add(auxClassifier.constructMappedInstance(i));
		}

		classes = extractClasses(instancesAll);

//		for (int i = 0; i < instancesAll.numInstances(); i++) {
//			if (i < numInstancesA)
//				instancesA.add(instancesAll.get(i));
//			else
//				instancesB.add(instancesAll.get(i));
//		}

		classifierA = DecisionTreeUtils.adaptClassifier(instancesA, settings);
		classifierB = DecisionTreeUtils.adaptClassifier(instancesB, settings);

		//Tree A with Log A
		treeA_logA = DecisionTreeUtils.classifyUsingTree(classifierA, instancesA, instancesA, settings);
		aux = DecisionTreeUtils.getCorrectlyAndIncorrectlyClassifiedInstances(treeA_logA.predictions(), instancesA);
		instancesA_correct = aux[0];
		instancesA_incorrect = aux[1];
		treeA_logA_correct = DecisionTreeUtils.classifyUsingTree(classifierA, instancesA, instancesA_correct, settings);
		treeA_logA_incorrect = DecisionTreeUtils.classifyUsingTree(classifierA, instancesA, instancesA_incorrect,
				settings);

		//Tree B with Log B		
		treeB_logB = DecisionTreeUtils.classifyUsingTree(classifierB, instancesB, instancesB, settings);
		aux = DecisionTreeUtils.getCorrectlyAndIncorrectlyClassifiedInstances(treeB_logB.predictions(), instancesB);
		instancesB_correct = aux[0];
		instancesB_incorrect = aux[1];
		treeB_logB_correct = DecisionTreeUtils.classifyUsingTree(classifierB, instancesB, instancesB_correct, settings);
		treeB_logB_incorrect = DecisionTreeUtils.classifyUsingTree(classifierB, instancesB, instancesB_incorrect,
				settings);

		//Tree A with Log B
		treeA_logB = DecisionTreeUtils.classifyUsingTree(classifierA, instancesAll, instancesB, settings);
		treeA_logB_correct = DecisionTreeUtils.classifyUsingTree(classifierA, instancesAll, instancesB_correct,
				settings);
		treeA_logB_incorrect = DecisionTreeUtils.classifyUsingTree(classifierA, instancesAll, instancesB_incorrect,
				settings);

		//Tree B with Log A
		treeB_logA = DecisionTreeUtils.classifyUsingTree(classifierB, instancesAll, instancesA, settings);
		treeB_logA_correct = DecisionTreeUtils.classifyUsingTree(classifierB, instancesAll, instancesA_correct,
				settings);
		treeB_logA_incorrect = DecisionTreeUtils.classifyUsingTree(classifierB, instancesAll, instancesA_incorrect,
				settings);

		//Similarity Tree
		//		DecisionTree treeSIM = new DecisionTree(source, targets, input.getMerged(), settings);
		//
		//		Instances instancesAll = treeSIM.getDecisionTree().getInstances();
		//		classifierSIM = new InputMappedClassifier();
		//		classifierSIM.setClassifier(new J48());

		instancesAll_extended = DecisionTreeUtils.extendInstances(instancesAll, classifierA, instancesA, classifierB,
				instancesB);
		classesSIM = extractClasses(instancesAll_extended);
		classifierSIM = DecisionTreeUtils.adaptClassifier(instancesAll_extended, settings);

		treeSIM_logAB = DecisionTreeUtils.classifyUsingTree(classifierSIM, instancesAll_extended, instancesAll_extended,
				settings);

		instancesA_extended = DecisionTreeUtils.extendInstances(instancesA, classifierA, instancesA, classifierB,
				instancesB);
		instancesA_correct_extended = DecisionTreeUtils.extendInstances(instancesA_correct, classifierA, instancesA,
				classifierB, instancesB);
		instancesA_incorrect_extended = DecisionTreeUtils.extendInstances(instancesA_incorrect, classifierA, instancesA,
				classifierB, instancesB);

		instancesB_extended = DecisionTreeUtils.extendInstances(instancesB, classifierA, instancesA, classifierB,
				instancesB);
		instancesB_correct_extended = DecisionTreeUtils.extendInstances(instancesB_correct, classifierA, instancesA,
				classifierB, instancesB);
		instancesB_incorrect_extended = DecisionTreeUtils.extendInstances(instancesB_incorrect, classifierA, instancesA,
				classifierB, instancesB);

		treeSIM_logA = DecisionTreeUtils.classifyUsingTree(classifierSIM, instancesAll_extended, instancesA_extended,
				settings);
		treeSIM_logA_correct = DecisionTreeUtils.classifyUsingTree(classifierSIM, instancesAll_extended,
				instancesA_correct_extended, settings);
		treeSIM_logA_incorrect = DecisionTreeUtils.classifyUsingTree(classifierSIM, instancesAll_extended,
				instancesA_incorrect_extended, settings);

		treeSIM_logB = DecisionTreeUtils.classifyUsingTree(classifierSIM, instancesAll_extended, instancesB_extended,
				settings);
		treeSIM_logB_correct = DecisionTreeUtils.classifyUsingTree(classifierSIM, instancesAll_extended,
				instancesB_correct_extended, settings);
		treeSIM_logB_incorrect = DecisionTreeUtils.classifyUsingTree(classifierSIM, instancesAll_extended,
				instancesB_incorrect_extended, settings);


	}

	private Object[] extractClasses(Instances instances) {

		Object[] classes = new Object[instances.classAttribute().numValues()];
		Enumeration<Object> e = instances.classAttribute().enumerateValues();
		int counter = 0;
		while (e.hasMoreElements()) {
			classes[counter] = e.nextElement();
			counter++;
		}
		return classes;
	}

	//	public InputMappedClassifier getClassifierA() {
	//		return classifierA;
	//	}
	//
	//	public InputMappedClassifier getClassifierB() {
	//		return classifierB;
	//	}
	//
	//	public InputMappedClassifier getClassifierSIM() {
	//		return classifierSIM;
	//	}

	public String getName() {
		return name;
	}

	public Evaluation getTreeA_logA() {
		return treeA_logA;
	}

	public Evaluation getTreeA_logA_correct() {
		return treeA_logA_correct;
	}

	public Evaluation getTreeA_logA_incorrect() {
		return treeA_logA_incorrect;
	}

	public Evaluation getTreeA_logB() {
		return treeA_logB;
	}

	public Evaluation getTreeA_logB_correct() {
		return treeA_logB_correct;
	}

	public Evaluation getTreeA_logB_incorrect() {
		return treeA_logB_incorrect;
	}

	public Evaluation getTreeA_logAB() {
		return treeA_logAB;
	}

	public Evaluation getTreeA_logAB_correct() {
		return treeA_logAB_correct;
	}

	public Evaluation getTreeA_logAB_incorrect() {
		return treeA_logAB_incorrect;
	}

	public Evaluation getTreeB_logA() {
		return treeB_logA;
	}

	public Evaluation getTreeB_logA_correct() {
		return treeB_logA_correct;
	}

	public Evaluation getTreeB_logA_incorrect() {
		return treeB_logA_incorrect;
	}

	public Evaluation getTreeB_logB() {
		return treeB_logB;
	}

	public Evaluation getTreeB_logB_correct() {
		return treeB_logB_correct;
	}

	public Evaluation getTreeB_logB_incorrect() {
		return treeB_logB_incorrect;
	}

	public Evaluation getTreeB_logAB() {
		return treeB_logAB;
	}

	public Evaluation getTreeB_logAB_correct() {
		return treeB_logAB_correct;
	}

	public Evaluation getTreeB_logAB_incorrect() {
		return treeB_logAB_incorrect;
	}

	public Evaluation getTreeSIM_logA() {
		return treeSIM_logA;
	}

	public Evaluation getTreeSIM_logA_correct() {
		return treeSIM_logA_correct;
	}

	public Evaluation getTreeSIM_logA_incorrect() {
		return treeSIM_logA_incorrect;
	}

	public Evaluation getTreeSIM_logB() {
		return treeSIM_logB;
	}

	public Evaluation getTreeSIM_logB_correct() {
		return treeSIM_logB_correct;
	}

	public Evaluation getTreeSIM_logB_incorrect() {
		return treeSIM_logB_incorrect;
	}

	public Evaluation getTreeSIM_logAB() {
		return treeSIM_logAB;
	}

	public Evaluation getTreeSIM_logAB_correct() {
		return treeSIM_logAB_correct;
	}

	public Evaluation getTreeSIM_logAB_incorrect() {
		return treeSIM_logAB_incorrect;
	}

	public Object[] getClasses() {
		return classes;
	}
	
	public Object[] getClassesSIM() {
		return classesSIM;
	}

	public AbstractClassifier getClassifierA() {
		return classifierA;
	}

	public AbstractClassifier getClassifierB() {
		return classifierB;
	}

	public AbstractClassifier getClassifierSIM() {
		return classifierSIM;
	}

	public DecisionTree getDtA() {
		return dtA;
	}

	public DecisionTree getDtB() {
		return dtB;
	}
}
