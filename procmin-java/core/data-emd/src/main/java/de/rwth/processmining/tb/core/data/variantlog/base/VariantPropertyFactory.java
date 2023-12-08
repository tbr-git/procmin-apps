package de.rwth.processmining.tb.core.data.variantlog.base;

public class VariantPropertyFactory {
	
	/**
	 * Aggregated property key.
	 */
	private int cumProp;
	
	public VariantPropertyFactory() {
	}
	
	public VariantPropertyFactory addProperty(int property) {
		this.cumProp |= property;
		return this;
	}
	
	public VariantKeys build() {
		return new VariantKeys(cumProp);
	}
	
}
