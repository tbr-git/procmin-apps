package de.rwth.processmining.tb.core.data.variantlog.base;

import java.util.function.Function;

import org.deckfour.xes.model.XTrace;

public interface XTraceToVariantTransformer<V extends CVariant> extends Function<XTrace, V> {

}
