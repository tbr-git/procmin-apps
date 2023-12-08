package de.rwth.processmining.tb.core.emd.language.transformer;

import org.apache.commons.lang3.tuple.Pair;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased.VariantBasedFeatureExtractor;

/**
 * Interface describing the transformation from a variant-based log to a stochastic language.
 */
public interface VariantLog2OrderedStochLangTransformer {

  /**
   * <b>Extract</b> a <b>single feature per trace</b> and <b>convert</b> the provided log into a <b>stochastic language</b>. 
   * 
   * @param <V> "Minimum" variant type required by the feature extractor
   * @param <F> Feature type that should constitute the stochastic language
   * @param tracesLeft Left log to be transformed.
   * @param tracesRight Right log to be transformed.
   * @param traceFeatureExtractor Feature extract that extract <b>one</b> feature per trace (-> nbr. variants of features)
   * @return Multiset of features (size of multiset equals support of variant)
   */
	public<V extends CVariant, F extends TraceDescriptor> Pair<OrderedStochasticLanguage<F>, OrderedStochasticLanguage<F>> 
	  transformWindow(CVariantLog<? extends V> logLeft, CVariantLog<? extends V> logRight, 
	      VariantBasedFeatureExtractor<V, F> featureExtractor);
	
	/**
	 * Get information on the probability mass of of non-empty traces/features. 
	 * @param tracesLeft Traces left
	 * @param tracesRight Traces right
	 * @return Non-zero probability mass information
	 */
	public ProbMassNonEmptyTrace probabilityMassNonEmptyTraces(CVariantLog<? extends CVariant> tracesLeft, 
			CVariantLog<? extends CVariant> tracesRight);
	
	public String getShortDescription();
}
