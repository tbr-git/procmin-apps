package de.rwth.processmining.tb.core.data.variantlog.abstraction;

import java.util.BitSet;
import java.util.Iterator;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public interface CVariantAbst extends CVariant {
	
	/**
	 * Get array of abstraction for activity index
	 * First entry should contain the number of added abstractions.
	 * Remaining entries contain the abstractions in ascending order.
	 * (This array may also contain size information)
	 * @param activityIndex
	 * @return
	 */
	public int[] getAbstractionsAt(int activityIndex);
	
	/**
	 * Get array of abstractions.
	 * Each entry is the array of abstractions for the activitiy at this position
	 * First entry in activity abstraction array should contain the number of added abstractions.
	 * Remaining entries contain the abstractions in ascending order.
	 * (This array may also contain size information)
	 * @return Abstraction array
	 */
	public int[][] getAbstractions();

	/**
	 * Get the abstraction count for the activity index.
	 * @param activityIndex 
	 * @return Count of abstractions for acitivity at index
	 */
	public int getNbrAbstractionsAt(int activityIndex);
	
	/**
	 * Add abstraction to activity at index.
	 * @param activityIndex Activity index where abstraction will be appended
	 * @param abstraction Abstraction code to append
	 */
	public void addAbstractionAt(int activityIndex, int abstraction);
	
	/**
	 * 
	 * @return Iterator over activity category code + abstraction array
	 */
	public Iterator<CVariantAbstActivityData> iteratorActAbst();

	@Override
	public CVariantAbst projectOnCategories(BitSet projectionCategories);

	@Override
	public CVariantAbst copyVariant();


}
