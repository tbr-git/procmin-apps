package de.rwth.processmining.tb.core.data.variantlog.base;

import java.util.BitSet;
import java.util.Collection;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.variantlog.transform.CVariantCondition;
import de.rwth.processmining.tb.core.data.variantlog.transform.CVariantTransformer;
import de.rwth.processmining.tb.core.data.variantlog.util.VariantCopyingException;

public interface CVariantLog<T extends CVariant> extends Iterable<T> {

	/**
	 * Get classifier that has been used for categorization
	 * @return
	 */
	public XEventClassifier getClassifier();

	/**
	 * Project the traces on the given activities.
	 * Remove empty traces.
	 * @param eventClasses Activities to be projected on
	 * @return Projected variants
	 */
	public CVariantLog<T> project(Collection<String> eventClasses);

	/**
	 * Project the traces on the given activities.
	 * Remove empty traces.
	 * @param eventClasses Activities to be projected on
	 * @return Projected variants
	 */
	public CVariantLog<T> project(BitSet eventClasses);
	
	/**
	 * Create a new log that contains only traces that contain all the given activities.
	 * @param eventClasses Collection of activities that the resulting traces should contain.
	 * @return Filtered log
	 */
	public CVariantLog<T> filterTracesMandatoryActivities(Collection<String> eventClasses) throws VariantCopyingException;

	/**
	 * Create a new log that contains only traces that contain all the given activities.
	 * @param eventClasses Array of activity category codes that the resulting traces should contain.
	 * @return Filtered log
	 */
	public CVariantLog<T> filterTracesMandatoryActivities(int[] eventClasses) throws VariantCopyingException;

	/**
	 * Get all variant that contain only traces that contain all the given activities.
	 * <b> Does not copy </b>
	 * @param eventClasses Array of activity category codes that the resulting traces should contain.
	 * @return Filtered log
	 */
	public Collection<T> getTracesMandatoryActivities(int[] eventClasses) throws VariantCopyingException;
	
	/**
	 * Create a new log that contains only variants that satisfy/ don't satisfy the condition.
	 * @param condition A variant condition
	 * @param keep Keep variants if true, drop variant if false.
	 * @return Filtered log
	 * @throws VariantCopyingException
	 */
	public CVariantLog<T> filterVariantByCondition(CVariantCondition<T> condition, 
	    boolean keep)  throws VariantCopyingException;

	/**
	 * Create a new log that contains only variants that satisfy/ don't satisfy any of the conditions.
	 * 
	 * @param conditions A collection of variant condition
	 * @param keep Keep variants if true, drop variant if false.
	 * @return Filtered log
	 * @throws VariantCopyingException
	 */
	public CVariantLog<T> filterVariantByConditionAny(Collection<CVariantCondition<T>> conditions, 
	    boolean keep)  throws VariantCopyingException;
	
	/**
	 * Get number of variants.
	 * @return Number of distinct variants.
	 */
	public int nbrVariants();

	/**
	 * Get the number of activities classes.
	 * @return Number of categories.
	 */
	public int getNbrActivityClasses();
	
	/**
	 * Get log size in terms of number of cases.
	 * @return Log size
	 */
	public int sizeLog();
	
	/**
	 * Get the activity that is represented by the given category within the context of this log.
	 * @param category Category code to be translated into an activity name
	 * @return Activity name or null if the category is not in the log
	 */
	public String getActivity4Category(int category);

	/**
	 * Is this variant contained in the log.
	 * @param variant Variant to check containment for
	 * @return
	 */
	public boolean contains(Object variant);
	
	/**
	 * Find and return this variant.
	 * @param variant Variant to find
	 * @return The variant equal to the given variant if contained else null
	 */
	public T get(Object variant);
	
	/**
	 * Get the maximal categorical activity code in use.
	 * @return Maximal activity category code.
	 */
	public int getMaxCategoryCode();
	
	/**
	 * Apply the variant Transformer to each variant in the log.
	 * @param variantTransformer transformer instance
	 * @param inplace transform the variant log inplace
	 */
	public CVariantLog<T> applyVariantTransformer(CVariantTransformer<T> variantTransformer, 
			boolean inplace);
	
	/**
	 * Get complete string representation in terms of normal activity names.
	 * @return
	 */
	public String toStringFull();
	
	/**
	 * Get some descriptive information. Which information is contained in the keys.
	 * @return Descriptive information on contained variants.
	 */
	public VariantKeys getVariantKey();
	
	/**
	 * Create a copy of the log. 
	 * Does a deep copy of the variants; shallow otherwise. 
	 * @return Deep copy of the log
	 */
	public CVariantLog<T> copyLog() throws VariantCopyingException;
	
	/**
	 * Creates a copy of the log in terms of meta data; however
	 * it will not contain any variants.
	 * @return Empty log with same meta data
	 */
	public CVariantLog<T> getEmptyCopy();
	
	/**
	 * Return the mapper instance that can be used to map (and inverse map) between
	 * categories and activity names.
	 * @return Category mapper (inverse mapper)
	 */
	public CategoryMapper getCategoryMapper();
	
	/**
	 * Generate a XLog for the variants in this variant log.
	 * For each variant, one XTrace will be generated. 
	 * 
	 * @param logName Name of the Xlog
	 * @param activityClearNames If true, translate the activity category codes into clear names.
	 * @return Xlog that contains a trace for each variant in the variant log
	 */
	public XLog variants2XLog(String logName, boolean activityClearNames);
}

