package de.rwth.processmining.tb.core.data.variantlog.contextaware;

import java.util.BitSet;

import org.python.bouncycastle.util.Arrays;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.transform.VariantConverter;

public class CVariant2ContextVariant<S extends CVariant> implements 
	VariantConverter<S, CVariantCatContSetImpl> {

	@Override
	public CVariantCatContSetImpl convert(S variant) {
		int[] variantCat = variant.getTraceCategories();
		int[] catCopy = Arrays.copyOf(variantCat, variantCat.length);
		
		return new CVariantCatContSetImpl(catCopy, new BitSet(), 
				variant.getSupport());
	}
	
}
