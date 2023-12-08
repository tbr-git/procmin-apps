package de.rwth.processmining.tb.core.data.variantlog.base;

import java.util.BitSet;

public interface CVariantAct extends CVariant {
  
	@Override
	CVariantAct projectOnCategories(BitSet projectionCategories);

	@Override
	CVariantAct copyVariant();

}
