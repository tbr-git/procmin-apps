package de.rwth.processmining.tb.core.util.bitsets;

import java.util.BitSet;
import java.util.Comparator;

/**
 * Compares BitSets s1 and s2 by inclusion.
 * 
 * Negative iff s1 \subseteq(!!!) s2  
 * Positive iff s2 \subset s1
 * Zero else (i.e., set are wrt. set inclusion
 * 
 * @author brockhoff
 *
 */
public class BitSetInclusionComparator implements Comparator<BitSet> {
	
	@Override
	public int compare(BitSet set1, BitSet set2) {
		// Check for activity itemset intersections s1 \subset s2 or s2 \subset s1
		BitSet tmp = (BitSet) set1.clone();
		tmp.and(set2);
		if (set1.cardinality() == tmp.cardinality()) {			// s1 \subseteq  s2
			return -1;
		}
		else if (set2.cardinality() == tmp.cardinality()) {		// s2 \subset s1
			return 1;
		}
		else {													// s1 != s2 and no inclusion
			return 0;
		}
	}
}
