package de.rwth.processmining.tb.core.data.variantlog.base;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Optional;

import org.deckfour.xes.model.XTrace;

/**
 * Interface for an efficient classifier complete variant.
 * 
 * Even though a variant-based log based on the object-oriented variant implementation is slightly slower 
 * than a pure array-based implementation (2d int array variant * (variant length and count), this should be
 * out-weighted by the object-orientation advantages. 
 * Here some benchmark results on a 16GB i7 (8gen) machine:
 * For each benchmark run, a random log with the specification below has been created and a projection on
 * three activities has been performed. Note that the number of variants is approximate because events has been created
 * with random activities.
 * 
 * <table>
 *	<thead>
 *	<tr><th>Method</th> <th>Approx. nbr variants</th> <th>Nbr Activities</th> <th>Trace len</th> <th>time</th></tr>
 *	</thead>
 *	<tbody>
 *  <tr><td>OO</td><td>100</td><td>100</td><td>100</td><td>46339.365 +- 307.896 ops/s</td></tr>
 *  <tr><td>Array</td><td>100</td><td>100</td><td>100</td><td>47111.780 +- 708.542 ops/s</td></tr>
 *  <tr><td>OO</td><td>1000</td><td>25</td><td>20</td><td>9244.256 +- 119.234 ops/s</td></tr>
 *  <tr><td>Array</td><td>1000</td><td>25</td><td>20</td><td>9656.791 +- 111.609 ops/s</td></tr>
 *   <tr><td>OO</td><td>500</td><td>100</td><td>200</td><td>3808.160 +- 23.545 ops/s</td></tr>
 *   <tr><td>Array</td><td>500</td><td>100</td><td>200</td><td>4465.548 +- 60.523 ops/s</td></tr>
 * </tbody>
 * </table>
 * 
 * <p>
 * Implementations of this interface <b>must define a copy constructor</b> 
 * that takes an argument of the same type or supertype of their class. 
 * This constructor must make a deep copy of the argument 
 * so that manipulation of this object does not lead to manipulation of the returned one and vice versa. 
 * 
 * @author brockhoff
 *
 */
public interface CVariant { 
//public interface CVariant<T extends CVariant<T>> { 
	// T extends CVariant<T> is used to implement the property that projection and copying
	// should be type consistent.
	
	/**
	 * Iterator over the categories in the variant. 
	 * 
	 * <p>
	 * Benchmarking showed that iterators are quite performant. 
	 * 5-fold iteration through a 1000 elements int array:
	 * <table>
	 * 	<thead>
	 * 	  <tr><th>Method</th><th>Runs</th><th>Throughput</th></tr>
	 * 	</thead>
	 * 	<tbody>
	 * 	  <tr><td>Iterator</td><td>3</td><td>436574,932 ops/s</td></tr>
	 * 	  <tr><td>Simple Iteration</td><td>3</td><td>441921,804 ops/s</td></tr>
	 *  </tbody>
	 * </table>
	 * * @return Iterator over categorical codes in variant
	 * */
	public Iterator<Integer> iteratorVariantCategorical();
	
	/**
	 * Get total support of the variant
	 * @return Total support of the variant (nbr. of cases that correspond to this variant)
	 */
	public int getSupport();
	
	/**
	 * Set the total support. Used for updating.
	 */
	public void setSupport(int support);

	/**
	 * Get get the length of a variant in terms of number of activities. 
	 * @return Number of activities in this variant.
	 */
	public int getVariantLength();
	
	/**
	 * Project the variant on a set of categories. 
	 * @param projectionCategories Categories of the activities this variant should be projected on.
	 * @return The projected variant; null iff empty
	 */
	public CVariant projectOnCategories(BitSet projectionCategories);

	/**
	 * Determines whether this variant contains all categories from the given collection.
	 * @param categories Array of category codes (Considered a set)
	 * @return True, iff variant contains all given categories.
	 */
	public boolean containsAllCategories(BitSet categories);

	/**
	 * Determines whether this variant contains all categories from the given collection.
	 * @param categories Array of category codes
	 * @return True, iff variant contains all given categories.
	 */
	public boolean containsAllCategories(int[] categories);
	
	/**
	 * Determines whether this variant contains the given category from the given collection.
	 * @param category Category code
	 * @return True, iff variant contains this category
	 */
	public boolean containsCategory(int category);
	
	/**
	 * Determines whether this variant contains any of the given categories.
	 * @param categories Array of category codes.
	 * @return True, iff variant contains at least one of the given categories.
	 */
	public boolean containsAnyCategory(int[] categories);
	
	/**
	 * Extract a subtrace from this variant (including from, excluding to);
	 * For from < 0, extraction will start right from the variant start.
	 * If to exceeds trace length, the rest of the case will be extracted.
	 * 
	 * @param from Start extraction at this position (including) 
	 * @param to Extract up to this position (excluding)
	 * @param inplace If inplace, the variant itself will be replaced by it's subtrace
	 * @return Variant based on the extracted subtrace
	 */
	public CVariant extractSubtrace(int from, int to, boolean inplace);

	/**
	 * Extract a subtrace from this variant (including from, excluding to);
	 * For from < 0, extraction will start right from the variant start.
	 * If to exceeds trace length, the rest of the case will be extracted.
	 * 
	 * @param indices Set of index flags (if position is set, keep activity at this position)
	 * @param inplace If inplace, the variant itself will be replaced by it's subtrace
	 * @return Variant based on the extracted subtrace
	 */
	public CVariant extractSubtrace(BitSet indices, boolean inplace);
	
	/**
	 * Copy the variant.
	 * @return Copy of the variant
	 */
	public CVariant copyVariant();
	
	/**
	 * Get some descriptive information. Which information is contained in the key.
	 * @return Descriptive information on this variant.
	 */
	public VariantKeys getVariantKey();
	
	/**
	 * Get the categorical codes for the events abstracted in this variant.
	 * @return Array of category codes
	 */
	public int[] getTraceCategories();
	
	/**
	 * Transform this variant into a XTrace. 
	 * @param traceName Trace name to be assigned
	 * @param categoryMapper If provided, it will be used to translate the activity names, 
	 * 	otherwise the categorical code will be used
	 * @return XTrace Trace -> List of events WITHOUT timestamps
	 */
	public XTrace variant2XTrace(String traceName, Optional<CategoryMapper> categoryMapper);
	
}
