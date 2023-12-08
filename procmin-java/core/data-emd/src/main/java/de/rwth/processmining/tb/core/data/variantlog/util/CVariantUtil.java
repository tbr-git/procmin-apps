package de.rwth.processmining.tb.core.data.variantlog.util;

import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;

public class CVariantUtil {

	/**
	 * Compute the indices of activities in variant that are member of classIds and nbr of disjoint class ids.
	 *  
	 * @param variant Array of categorical variant activity codes
	 * @param classIds BitSet that contains the activities whose indices should be computed.
	 * @return Array [number unique contained class ids, number matches, index 1, ..., index n, 0, ..., 0]
	 */
	public static int[] getUniqueAndMatchingEventIndices(int[] variant, BitSet classIds) {
		int c = 2;
		// Keep track of unique contained ids
		BitSet uniqueIds = new BitSet(classIds.size());

		// Index to keep after projection
		int[] projIndices = new int[variant.length + 2];
		// Analyze variant
		for(int i = 0; i < variant.length; i++) {
			if(classIds.get(variant[i])) {
				projIndices[c] = i;
				uniqueIds.set(variant[i]);
				c++;
			}
		}
		// Number of unique contained activities
		projIndices[0] = uniqueIds.cardinality();
		// Number of matches
		projIndices[1] = c - 2;
		return projIndices;
	}
	
	/**
	 * Compute the indices of activities in variant that are member of classIds.
	 *  
	 * @param variant Array of categorical variant activity codes
	 * @param classIds BitSet that contains the activities whose indices should be computed.
	 * @return Array [number matches, index 1, ..., index n, 0, ..., 0]
	 */
	public static int[] getMatchingEventIndices(int[] variant, BitSet classIds) {
		int c = 1;
		int[] projIndices = new int[variant.length + 1];
		for(int i = 0; i < variant.length; i++) {
			if(classIds.get(variant[i])) {
				projIndices[c] = i;
				c++;
			}
		}
		projIndices[0] = c - 1;
		return projIndices;
	}
	
	/**
	 * Checks if the second set is a superset of the first one.
	 * @param set Subset (must be a set!)
	 * @param superset Superset (Can be a multiset)
	 * @return Ture, iff set is subset of superset
	 */
	public static boolean isContained(int[] set, int[] superset) {
		BitSet mandatoryEvents = new BitSet(superset.length);
		for(int c : set) {
			mandatoryEvents.set(c);
		}
		for(int e : superset) {
			mandatoryEvents.clear(e);
			if(mandatoryEvents.cardinality() == 0) {
				break;
			}
		}
		if(mandatoryEvents.cardinality() == 0) {
			return true;
		}
		else {
			return false;
		}
	}	

	/**
	 * Checks if the second set is a superset of the first one.
	 * @param set Subset (must be a set!)
	 * @param superset Superset (Can be a multiset)
	 * @return Ture, iff set is subset of superset
	 */
	public static boolean isContained(BitSet set, int[] superset) {
		BitSet mandatoryEvents = (BitSet) set.clone();

		for(int e : superset) {
			mandatoryEvents.clear(e);
			if(mandatoryEvents.cardinality() == 0) {
				return true;
			}
		}
		if(mandatoryEvents.cardinality() == 0) {
			return true;
		}
		else {
			return false;
		}
	}	
	
	/**
	 * Does the second array contains the given category code. 
	 * @param category Category to search
	 * @param a Array to be searched
	 * @return True, iff array contains category
	 */
	public static boolean containsCategory(int category, int[] a) {
		for(int c : a) {
			if(c == category) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if at least on element of the first array is contained in the second array.
	 * @param candidates Array of elements where at least one should be contained.
	 * @param superset 
	 * @return
	 */
	public static boolean isAnyContained(int[] candidates, int[] superset) {
		BitSet anyOfEvents = new BitSet(candidates.length);
		for(int c : candidates) {
			anyOfEvents.set(c);
		}
		for(int e : superset) {
			if(anyOfEvents.get(e)) {
				return true;
			}
		}
		return false;
	}	

	@SuppressWarnings("unchecked")
	public static<T> T copyVariant(T variant) throws VariantCopyingException {
		Class<?> variantType = variant.getClass();
		try {
			return (T) variantType.getDeclaredConstructor(variantType).newInstance(variant);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new VariantCopyingException("Could not build a log by copying!\n" + e.getMessage());
		}
	}

}
