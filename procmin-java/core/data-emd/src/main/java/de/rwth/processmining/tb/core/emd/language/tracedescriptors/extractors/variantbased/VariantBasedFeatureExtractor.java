package de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased;

import com.google.common.collect.Multiset;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantKeys;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptorExtractor;
import gnu.trove.map.TObjectFloatMap;

/**
 * Extract 
 * @param <V>
 * @param <F>
 */
public interface VariantBasedFeatureExtractor<V extends CVariant, F extends TraceDescriptor> extends 
    TraceDescriptorExtractor<F> {
	
  /**
   * Initialize the extractor based on the provided log.
   * @param clog
   */
	public default void init(CVariantLog<? extends V> clog) {};
	
	/**
	 * Extract "features" from the variant and add it to the collection. 
	 * <b>Important:</b> We assume that only a single feature is extracted per trace!
	 * Therefore, the cardinality of the returned feature multiset <b>equals</b> the cardinality of the variant.
	 * @param variant Variant to extract the features from.
	 * @param log Log that provides a context (e.g., information how to interpret the variant)
	 * @return Multiset of features (cardinality equals variant frequency)
	 */
	public Multiset<F> getTraceDescriptor(V variant, CVariantLog<? extends V> log);

	/**
	 * Extract "features" (comp. {@link #getTraceDescriptor(CVariant, CVariantLog)}) from the variant and add it to the collection. 
	 * @param variant Variant to extract the features from.
	 * @param log Log that provides a context (e.g., information how to interpret the variant)
	 * @param features Collection to which the feature is added.
	 * @return Number of added features
	 */
	public int addTraceDescriptor(V variant, CVariantLog<? extends V> log, Multiset<F> features);

	/**
	 * Extract "features" (comp. {@link #getTraceDescriptor(CVariant, CVariantLog)}) from the variant and 
	 * add it to the map. 
	 * @param variant Variant to extract the features from.
	 * @param log Log that provides a context (e.g., information how to interpret the variant)
	 * @param features Collection to which the feature is added.
	 * @return Added weight
	 */
	public float addTraceDescriptor(V variant, CVariantLog<? extends V> log, 
	    TObjectFloatMap<F> features);

	/**
	 * Extract "features" (comp. {@link #getTraceDescriptor(CVariant, CVariantLog)}) from the variant 
	 * and add it to the map by incrementing the counter by the provided increment. 
	 * 
	 * @param variant Variant to extract the features from.
	 * @param log Log that provides a context (e.g., information how to interpret the variant)
	 * @param features Collection to which the feature is added.
	 * @param increment Increment the counter for the extracted feature by this value
	 * @return Added weight
	 */
	public float addTraceDescriptor(V variant, CVariantLog<? extends V> log, 
	    TObjectFloatMap<F> features, float increment);
	  
	/**
	 * More detailed information whether the information contained in the variant 
	 * is sufficient to extract the feature.
	 * While the type annotation specifies how the data contained in the variant is interpreted,
	 * this can be used to match semantics. 
	 * For example, the extractor requires additional numerical event information (e.g., for binning),
	 * the variant keys specify that the numerical data are timestamps.
	 * @return
	 */
	public VariantKeys getRequiredVariantInfo();
	
	/**
	 * Get descriptor that corresponds to running the extraction on an empty variant.
	 * 
	 * For example, use it for padding languages.
	 * @param contextLog
	 * @return
	 */
	public F getEmptyCVariant(CVariantLog<? extends V> contextLog);

}
