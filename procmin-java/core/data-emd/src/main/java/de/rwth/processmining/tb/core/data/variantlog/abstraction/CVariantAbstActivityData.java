package de.rwth.processmining.tb.core.data.variantlog.abstraction;

public class CVariantAbstActivityData {
	
	private final int activityCategory;
	
	private final int[] abstractions;

	public CVariantAbstActivityData(int activityCategory, int[] abstractions) {
		super();
		this.activityCategory = activityCategory;
		this.abstractions = abstractions;
	}

	public int getActivityCategory() {
		return activityCategory;
	}

	public int[] getAbstractions() {
		return abstractions;
	}

}
