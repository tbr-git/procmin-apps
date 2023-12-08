package de.rwth.processmining.tb.core.emd.language;

import java.util.function.Function;

import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public interface XTraceToDescriptor<T extends TraceDescriptor> extends Function<XTrace, T> {

}
