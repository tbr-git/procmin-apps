package de.rwth.processmining.tb.core.data.variantlog.transform;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public class ContainsAllCategoriesCondition<T extends CVariant> implements CVariantCondition<T> {

	private final int[] categories;
	
	public ContainsAllCategoriesCondition(int[] categories) {
		this.categories = categories;
	}
	
	@Override
	public boolean satisfies(T variant) {
		return variant.containsAllCategories(categories);
	}

}
