package de.rwth.processmining.tb.core.data.variantlog.transform;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public interface VariantConverter <S extends CVariant, T extends CVariant>{
	
	/**
	 * Convert the variant to a different variant type.
	 * @param variant Variant to convert
	 * @return Converted variant
	 */
	public T convert(S variant);

}
