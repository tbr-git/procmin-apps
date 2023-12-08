package de.rwth.processmining.tb.core.data.variantlog.transform;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public interface CVariantTransformer<T extends CVariant> {
	//TODO CVariantTransformer<T, U> -> changes type
	
	public boolean requiresDuplicateDetection();
	

	public T apply(T variant, boolean inplace);

	public default T apply(T variant) {
		return apply(variant, false);
	}

}
