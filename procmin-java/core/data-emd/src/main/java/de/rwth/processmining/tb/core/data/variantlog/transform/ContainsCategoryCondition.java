package de.rwth.processmining.tb.core.data.variantlog.transform;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public class ContainsCategoryCondition<T extends CVariant> implements CVariantCondition<T> {
	
	private final int category;
	
	public ContainsCategoryCondition(int category) {
		this.category = category;
	}

	@Override
	public boolean satisfies(T variant) {
		return variant.containsCategory(category);
	}

}
