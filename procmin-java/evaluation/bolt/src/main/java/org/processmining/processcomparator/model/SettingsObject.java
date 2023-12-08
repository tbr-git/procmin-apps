package org.processmining.processcomparator.model;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.tsanalyzer2.TSAnalyzer;
import org.processmining.processcomparator.algorithms.Utils;
import org.processmining.processcomparator.controller.settings.SettingsController;
import org.processmining.processcomparator.controller.settings.component.GraphSettingsController;
import org.processmining.processcomparator.controller.settings.component.comparison.ComparisonSettingsController;
import org.processmining.processcomparator.model.decisiontree.DecisionTreeSettingsObject;

import com.google.common.collect.TreeMultiset;

public class SettingsObject implements Comparable<SettingsObject> {

	
	private TreeMultiset<String> selected_A, selected_B;
	private boolean graph_isDefaultSelected, graph_isCustomSelected, graph_isFilterSelected,
			graph_isShowTransitionLabelsSelected, comparison_metric_selected, comparison_metric_isStatesSelected,
			comparison_metric_isTransitionsSelected, comparison_tree_selected;
	private TsSettingsObject graph_tsSettings;
	private String graph_StateThicknessSelection, graph_TransitionThicknessSelection,
			comparison_metric_selectedComparisonType;
	private double graph_filterThreshold, comparison_metric_alpha, comparison_tree_similarity;
	private List<String> comparison_tree_selectedAttributes;
	private DecisionTreeSettingsObject dtObject;

	/**
	 * Default contructor, initializes with default values
	 */
	public SettingsObject(InputObject input, PluginContext pluginContext) {

		selected_A = TreeMultiset.create();
		for (XLog log : input.getSelected_A())
			selected_A.add(log.getAttributes().get("concept:name").toString());

		selected_B = TreeMultiset.create();
		for (XLog log : input.getSelected_B())
			selected_B.add(log.getAttributes().get("concept:name").toString());

		graph_isDefaultSelected = true;
		graph_isCustomSelected = false;
		graph_tsSettings = new TsSettingsObject(Utils.getTSSettings(pluginContext, input.getMerged()));
		graph_StateThicknessSelection = TSAnalyzer.trace_frequency;
		graph_TransitionThicknessSelection = TSAnalyzer.trace_frequency;
		graph_isFilterSelected = true;
		graph_filterThreshold = 5;
		graph_isShowTransitionLabelsSelected = false;

		comparison_metric_selected = true;
		comparison_metric_selectedComparisonType = TSAnalyzer.trace_frequency;
		comparison_metric_isStatesSelected = true;
		comparison_metric_isTransitionsSelected = true;
		comparison_metric_alpha = 5;

		comparison_tree_selected = false;
		comparison_tree_similarity = 20;
		comparison_tree_selectedAttributes = new ArrayList<String>();

		dtObject = new DecisionTreeSettingsObject();
	}

	/**
	 * Builds a settings object based on the current state of the settigns
	 * 
	 * @param controller
	 */
	public SettingsObject(SettingsController controller) {

		InputObject input = controller.getVariantSettingsController().getInput();

		selected_A = TreeMultiset.create();
		for (XLog log : input.getSelected_A())
			selected_A.add(log.getAttributes().get("concept:name").toString());

		selected_B = TreeMultiset.create();
		for (XLog log : input.getSelected_B())
			selected_B.add(log.getAttributes().get("concept:name").toString());

		GraphSettingsController graphSettingsController = controller.getGraphSettingsController();
		graph_isDefaultSelected = graphSettingsController.isDefaultTsSettingsSelected();
		graph_isCustomSelected = graphSettingsController.isCustomTsSettingsSelected();
		graph_tsSettings = graphSettingsController.getTsSettings();
		graph_StateThicknessSelection = graphSettingsController.getStateThicknessSelection();
		graph_TransitionThicknessSelection = graphSettingsController.getTransitionThicknessSelection();
		graph_isFilterSelected = graphSettingsController.isFilterSelected();
		graph_filterThreshold = graphSettingsController.getFilterThreshold();
		graph_isShowTransitionLabelsSelected = graphSettingsController.isShowTransitionLabelsSelected();

		ComparisonSettingsController comparisonSettingsController = controller.getComparisonSettingsController();
		comparison_metric_selected = comparisonSettingsController.isProcessMetricsSelected();
		comparison_metric_selectedComparisonType = comparisonSettingsController.getComparisonSelection_Metrics();
		comparison_metric_isStatesSelected = comparisonSettingsController.isStatesSelected_Metrics();
		comparison_metric_isTransitionsSelected = comparisonSettingsController.isTransitionsSelected_Metrics();
		comparison_metric_alpha = comparisonSettingsController.getAlpha_Metrics();

		comparison_tree_selected = comparisonSettingsController.isDecisionTreesSelected();
		comparison_tree_similarity = comparisonSettingsController.getSimilarityThreshold_DecisionTrees();
		comparison_tree_selectedAttributes = comparisonSettingsController.getSelectedAttributes_DecisionTrees();

		dtObject = new DecisionTreeSettingsObject(comparisonSettingsController);
		System.out.println("updated!");
	}

	/**
	 * Returns 0 if both Settigns object are equal, so no changes. Returns 1 if
	 * the new Settigns Object means that the whole TS needs to be recalculated.
	 * Returns 2 if the TS has not changed, but the comparisons have to be
	 * recalculated. Returns 3 if the TS and the comparisons have not changed,
	 * but the filters r visual things have changed
	 */
	public int compareTo(SettingsObject alternative) {
		//check for changes that would return 1
		if (!selected_A.containsAll(alternative.selected_A) || !alternative.selected_A.containsAll(selected_A)
				|| !selected_B.containsAll(alternative.selected_B) || !alternative.selected_B.containsAll(selected_B))
			//the variant selections changed, start over.
			return 1;
		if (graph_isDefaultSelected != alternative.graph_isDefaultSelected)
			return 1;
		if (graph_isCustomSelected && graph_tsSettings != null
				&& !graph_tsSettings.isEqual(alternative.graph_tsSettings))
			return 1;

		//check for the changes that would return 2
		if (comparison_metric_selected != alternative.comparison_metric_selected
				|| comparison_tree_selected != alternative.comparison_tree_selected)
			return 2;
		if (!comparison_metric_selectedComparisonType.equals(alternative.comparison_metric_selectedComparisonType))
			//different comparison types;
			return 2;
		if (comparison_metric_isStatesSelected != alternative.comparison_metric_isStatesSelected
				|| comparison_metric_isTransitionsSelected != alternative.comparison_metric_isTransitionsSelected)
			//different selections of states and transitions;
			return 2;
		if (comparison_tree_selectedAttributes.containsAll(alternative.comparison_tree_selectedAttributes)
				&& alternative.comparison_tree_selectedAttributes.containsAll(comparison_tree_selectedAttributes))
			return 2;
		//check for the changes that would return 3
		if (!graph_StateThicknessSelection.equals(alternative.graph_StateThicknessSelection)
				|| !graph_TransitionThicknessSelection.equals(alternative.graph_TransitionThicknessSelection))
			return 3;
		if (graph_isFilterSelected != alternative.graph_isFilterSelected)
			return 3;
		if (graph_isFilterSelected && graph_filterThreshold != alternative.graph_filterThreshold)
			return 3;
		if (graph_isShowTransitionLabelsSelected != alternative.graph_isShowTransitionLabelsSelected)
			return 3;
		if (comparison_metric_alpha != alternative.comparison_metric_alpha)
			return 3;
		if (comparison_tree_similarity != alternative.comparison_tree_similarity)
			return 3;
		if (dtObject.compareTo(alternative.dtObject) != 0)
			return 3;

		//if all conditions above are not met, then they are equal
		return 0;

	}

	public TreeMultiset<String> getSelected_A() {
		return selected_A;
	}

	public TreeMultiset<String> getSelected_B() {
		return selected_B;
	}

	public boolean isGraph_isDefaultSelected() {
		return graph_isDefaultSelected;
	}

	public boolean isGraph_isCustomSelected() {
		return graph_isCustomSelected;
	}

	public boolean isGraph_isFilterSelected() {
		return graph_isFilterSelected;
	}

	public boolean isGraph_isShowTransitionLabelsSelected() {
		return graph_isShowTransitionLabelsSelected;
	}

	public boolean isComparison_isStatesSelected() {
		return comparison_metric_isStatesSelected;
	}

	public boolean isComparison_isTransitionsSelected() {
		return comparison_metric_isTransitionsSelected;
	}

	public TsSettingsObject getGraph_tsSettings() {
		return graph_tsSettings;
	}

	public String getGraph_StateThicknessSelection() {
		return graph_StateThicknessSelection;
	}

	public String getGraph_TransitionThicknessSelection() {
		return graph_TransitionThicknessSelection;
	}

	public String getComparison_selectedComparisonType() {
		return comparison_metric_selectedComparisonType;
	}

	public double getGraph_filterThreshold() {
		return graph_filterThreshold;
	}

	public double getComparison_alpha() {
		return comparison_metric_alpha;
	}

	public boolean isComparison_metric_selected() {
		return comparison_metric_selected;
	}

	public boolean isComparison_tree_selected() {
		return comparison_tree_selected;
	}

	public double getComparison_tree_similarity() {
		return comparison_tree_similarity;
	}

	public List<String> getComparison_tree_selectedAttributes() {
		return comparison_tree_selectedAttributes;
	}

	public boolean isBinarySplit_DecisionTrees() {
		return dtObject.isBinarySplit();
	}

	public boolean isPruneTrees_DecisionTrees() {
		return dtObject.isPruned();
	}

	public float getConfidenceThreshold_DecisionTrees() {
		return dtObject.getConfidenceTThreshold();
	}

	public double getPercentageOfInstancesOnLeaf_DecisionTrees() {
		return dtObject.getMinPercentageOfObjsOnLeaf();
	}

	public DecisionTreeSettingsObject getDecisionTreeSettingsObject() {
		return dtObject;
	}

}
