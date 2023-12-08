package de.rwth.processmining.tb.core.data.xlogutil;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XTrace;

// I use these functionality to implement transformations that are 
// much more difficult to implement on the variant log.
// For example, loop unrolling becomes a problem if I do it on categories only 
// and if I want to use the same re-mapping for multiple datasources.
/**
 * Interface that defines inplace XLog operations that transform a {@link XTrace} inplace.
 * 
 * @author brockhoff
 *
 */
public interface TraceTransformerInplace {
	
	/**
	 * Applies an inplace transformation of the a given trace.
	 * @param t Trace that is going to be transformed
	 * @param classifier Event classifier to map events to activities
	 */
	public void transform(XTrace t, XEventClassifier classifier);
	
	/**
	 * Apply the transformation to a collection of traces
	 * @param traces Collection of traces
	 * @param classifier Event classifier to be used
	 */
	public default void transform(Iterable<XTrace> traces, XEventClassifier classifier) {
		traces.forEach(t -> this.transform(t, classifier));
	}
	
	public String getDescription();

}
