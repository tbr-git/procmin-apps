package de.rwth.processmining.tb.core.data.variantlog.transform;

import de.rwth.processmining.tb.core.data.variantlog.contextaware.CVariantCatContSet;

public class CVariantPrefixWithFutureTruncator<T extends CVariantCatContSet> 
		implements CVariantTransformer<T> {
	
	/**
	 * Length of the prefix to which the variant will be truncated. 
	 */
	private final int prefixLength;

	public CVariantPrefixWithFutureTruncator(int prefixLength) {
		this.prefixLength = prefixLength;
	}
	
	@Override
	public boolean requiresDuplicateDetection() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T apply(T variant, boolean inplace) {
		variant.setRemovedToCSet(true);
		return (T) variant.extractSubtrace(0, prefixLength, inplace);
	}

	public int getPrefixLength() {
		return prefixLength;
	}

}
