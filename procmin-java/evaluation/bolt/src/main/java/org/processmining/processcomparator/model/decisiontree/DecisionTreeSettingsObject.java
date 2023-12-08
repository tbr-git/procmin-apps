package org.processmining.processcomparator.model.decisiontree;

import org.processmining.datadiscovery.DecisionTreeConfig;
import org.processmining.processcomparator.controller.settings.component.comparison.ComparisonSettingsController;

public class DecisionTreeSettingsObject implements Comparable<DecisionTreeSettingsObject> {

	DecisionTreeConfig settings;

	public DecisionTreeSettingsObject() {
		settings = new DecisionTreeConfig();
	}

	public DecisionTreeSettingsObject(ComparisonSettingsController controller) {
		this();
		settings.setBinarySplit(controller.isBinarySplit_DecisionTrees());
		settings.setConfidenceTreshold(controller.getConfidenceThreshold_DecisionTrees() / 100);
		settings.setUnpruned(!controller.isPruneTrees_DecisionTrees());
		settings.setMinPercentageObjectsOnLeaf(controller.getPercentageOfInstancesOnLeaf_DecisionTrees() / 100);
	}

	public boolean isBinarySplit() {
		return settings.isBinarySplit();
	}

	public boolean isPruned() {
		return !settings.isUnpruned();
	}

	public double getMinPercentageOfObjsOnLeaf() {
		return settings.getMinPercentageObjectsOnLeaf();
	}

	public float getConfidenceTThreshold() {
		return settings.getConfidenceThreshold();
	}

	public DecisionTreeConfig getSettings() {
		return settings;
	}

	@Override
	public int compareTo(DecisionTreeSettingsObject alternative) {
		if (this.isBinarySplit() == alternative.isBinarySplit() && this.isPruned() == alternative.isPruned()
				&& this.getConfidenceTThreshold() == alternative.getConfidenceTThreshold()
				&& this.getMinPercentageOfObjsOnLeaf() == alternative.getMinPercentageOfObjsOnLeaf())
			return 0;
		else
			return 1;
	}

}
