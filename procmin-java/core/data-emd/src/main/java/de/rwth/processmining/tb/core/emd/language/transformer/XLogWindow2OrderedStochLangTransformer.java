package de.rwth.processmining.tb.core.emd.language.transformer;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.XLogTraceFeatureExtractor;

public interface XLogWindow2OrderedStochLangTransformer {

  /**
   * Transform the provided logs into stochastic languages using the provided feature descriptor.
   * 
   * @param <F> Type of the extracted feature
   * @param tracesLeft Left traces.
   * @param tracesRight Right traces.
   * @param descFactory Feature extractor
   * @return Two stochastic languages.
   */
	public default<F extends TraceDescriptor> Pair<OrderedStochasticLanguage<F>, OrderedStochasticLanguage<F>> 
	  transformWindow(Collection<XTrace> tracesLeft, Collection<XTrace> tracesRight, 
			XLogTraceFeatureExtractor<F> featureExtractor) {
		return transformWindow(tracesLeft.iterator(), tracesRight.iterator(), featureExtractor);
	};
	
	/**
   * Transform the provided logs into stochastic languages using the provided feature descriptor.
   * 
   * @param <F> Type of the extracted feature
   * @param tracesLeft Iterator left traces.
   * @param tracesRight Iterator right traces.
	 * @param descFactory Feature extractor
   * @return Two stochastic languages.
	 */
	public<F extends TraceDescriptor> Pair<OrderedStochasticLanguage<F>, OrderedStochasticLanguage<F>> 
	  transformWindow(Iterator<XTrace> itTracesLeft, Iterator<XTrace> itTracesRight, 
			XLogTraceFeatureExtractor<F> featureExtractor);
	
	/**
	 * Get a short description of the transformer.
	 * @return
	 */
	public String getShortDescription();
}
