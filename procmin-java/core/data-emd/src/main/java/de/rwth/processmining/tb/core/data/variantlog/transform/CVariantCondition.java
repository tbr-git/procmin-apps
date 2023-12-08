package de.rwth.processmining.tb.core.data.variantlog.transform;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public interface CVariantCondition<T extends CVariant> {
	
	public boolean satisfies(T variant);

}
