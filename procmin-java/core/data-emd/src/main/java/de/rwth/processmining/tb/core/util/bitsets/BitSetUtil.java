package de.rwth.processmining.tb.core.util.bitsets;

import java.util.BitSet;

public class BitSetUtil {
	
	public static boolean isSubset(BitSet subset, BitSet superset) {
		
		if (subset.cardinality() > superset.cardinality()) {
			return false;
		}
		else { 
			for (int i = subset.nextSetBit(0); i >= 0; i = subset.nextSetBit(i + 1)) {
				if (!superset.get(i)) { 
					return false; 
				} 
			}
		}
		return true;
	}

	public static double jaccardIndex(BitSet set1, BitSet set2) {
		BitSet tmp = (BitSet) set1.clone();

		tmp.and(set2);

		double intersection = (double) tmp.cardinality();

		return intersection / (set1.cardinality() + set2.cardinality() - intersection);
	}

	public static double jaccardIndex2(BitSet set1, BitSet set2) {

		int intersection = 0;
		for (int i = set1.nextSetBit(0); i >= 0; i = set1.nextSetBit(i + 1)) {
			if (set2.get(i)) { 
				intersection++;
			} 
		}
		return ((double) intersection) / (set1.cardinality() + set2.cardinality() - intersection);
	}
}
