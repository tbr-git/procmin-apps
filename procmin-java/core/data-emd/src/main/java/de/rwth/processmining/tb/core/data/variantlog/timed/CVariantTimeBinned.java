package de.rwth.processmining.tb.core.data.variantlog.timed;

import java.util.BitSet;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public interface CVariantTimeBinned extends CVariant  {
	
	/**
	 * Get time bin for activity at index.
	 * @param index Activity index in variant
	 * @return
	 */
	public int getTimeBinAt(int index);
	
	/**
	 * 
	 * @return Iterator of pairs of category and time index.
	 */
	public Iterator<Pair<Integer, Integer>> iteratorCategoryTime();
	
	@Override
	public CVariantTimeBinned projectOnCategories(BitSet projectionCategories);

	@Override
	public CVariantTimeBinned copyVariant();

}
