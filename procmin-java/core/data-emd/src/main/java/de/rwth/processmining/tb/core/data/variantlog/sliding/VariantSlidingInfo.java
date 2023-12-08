package de.rwth.processmining.tb.core.data.variantlog.sliding;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public record VariantSlidingInfo<T extends CVariant>(T variant, long count) {

}
