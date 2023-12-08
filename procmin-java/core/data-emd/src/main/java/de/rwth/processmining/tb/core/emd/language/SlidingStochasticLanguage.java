package de.rwth.processmining.tb.core.emd.language;

import java.util.Iterator;

import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

// Cannot extends Sliding Window because we need a StochasticLanguageIterator 
// that handles TraceDescriptors instead of String[] only
public interface SlidingStochasticLanguage<T extends TraceDescriptor> extends StochasticLanguage<T> {
	
	/**
	 * 
	 * @param traces
	 */
	public void slideOut(XTrace trace);
	
	public default void slideOut(Iterable<XTrace> traces) {
		for(XTrace trace : traces) {
			slideOut(trace);
		}
	}
	
	public void slideIn(XTrace trace);
	
	public default void slideIn(Iterator<XTrace> traces) {
		while(traces.hasNext()) {
			slideIn(traces.next());
		}
	}
	
}
