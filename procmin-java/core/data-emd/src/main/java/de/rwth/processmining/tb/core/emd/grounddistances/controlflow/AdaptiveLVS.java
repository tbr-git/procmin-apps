package de.rwth.processmining.tb.core.emd.grounddistances.controlflow;

import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.AbstTraceCC;


public class AdaptiveLVS implements TraceDescDistCalculator<AbstTraceCC> {
	
	private boolean[] freeDelete;

	private boolean[] freeInsert;
	
	private boolean[] freeRename;
	
	private int maxAbstNbr = 10;
	
	private List<BitSet> condActFreeTraceDelete;

	private List<BitSet> condActFreeTraceInsert;
	
	private static final double APPROX_ZERO = Math.pow(10, -5);
	
	public AdaptiveLVS() {
		this.freeDelete = new boolean[this.maxAbstNbr];
		this.freeInsert = new boolean[this.maxAbstNbr];
		this.freeRename = new boolean[this.maxAbstNbr];
		Arrays.fill(freeDelete, false);
		Arrays.fill(freeInsert, false);
		Arrays.fill(freeRename, false);
		condActFreeTraceDelete = new LinkedList<>();
		condActFreeTraceInsert = new LinkedList<>();
	}

	/**
	 * Copy constructor.
	 * @param aLVS Distance to copy
	 */
	public AdaptiveLVS(AdaptiveLVS aLVS) {
		this.maxAbstNbr = aLVS.maxAbstNbr;
		this.freeDelete = Arrays.copyOf(aLVS.freeDelete, aLVS.freeDelete.length);
		this.freeInsert = Arrays.copyOf(aLVS.freeInsert, aLVS.freeInsert.length);
		this.freeRename = Arrays.copyOf(aLVS.freeRename, aLVS.freeRename.length);
	}
	
	public void addFreeDelete(int abstNr) {
		incArraySizes(abstNr);
		this.freeDelete[abstNr] = true;
	}

	public void addFreeInsert(int abstNr) {
		incArraySizes(abstNr);
		this.freeInsert[abstNr] = true;
	}
	
	public void addFreeRename(int abstNr) {
		incArraySizes(abstNr);
		this.freeRename[abstNr] = true;
	}
	
	public void conditionFreeTraceDelete(int[] activities) {
		BitSet b = new BitSet();
		for (int a : activities) {
			b.set(a);
		}
		this.condActFreeTraceDelete.add(b);
	}

	public void conditionFreeTraceInsert(int[] activities) {
		BitSet b = new BitSet();
		for (int a : activities) {
			b.set(a);
		}
		this.condActFreeTraceInsert.add(b);
	}
	
	private void incArraySizes(int abstNr) {
		if(abstNr >= maxAbstNbr) {
			maxAbstNbr = abstNr + 10;
			this.freeDelete = Arrays.copyOf(freeDelete, maxAbstNbr);
			this.freeInsert = Arrays.copyOf(freeInsert, maxAbstNbr);
			this.freeRename = Arrays.copyOf(freeRename, maxAbstNbr);
		}
	}
	
	@Override
	public double get_distance(AbstTraceCC t1, AbstTraceCC t2) {
		int m = t1.length();
		int n = t2.length();
		if (n == 0 && m == 0) {
			return 0;
		}
		// Left trace empty -> Is right insertion free?
		if (m == 0 && this.condActFreeTraceInsert != null) {
			for (BitSet actCond : this.condActFreeTraceInsert) {
				if (doesDescriptorContainActivities(t2, actCond)) {
					return 0;
				}
			}
		}
		// Right trace empty -> Is left trace deletion free?
		if (n == 0 && this.condActFreeTraceDelete != null) {
			for (BitSet actCond : this.condActFreeTraceDelete) {
				if (doesDescriptorContainActivities(t1, actCond)) {
					return 0;
				}
			}
		}

		Pair<Integer, Integer> costs = get_AdaptiveLVS_Distance(t1, t2);
		double distance = (double) costs.getLeft();
		// Normalization not necessary if distance == 0
		// Moreover, distance approx. 0 could be caused by zero cost deletion and insertions
		// causing division by zero.
		if(distance > APPROX_ZERO) {
			distance /= costs.getRight();
		}
		return distance;
	}
	
	private boolean doesDescriptorContainActivities(AbstTraceCC t, BitSet activities) {
		int requiredMatches = activities.cardinality();

		for (int a : t.getTrace()) {
			if (activities.get(a)) {
				requiredMatches--;
			}
			if (requiredMatches == 0) {
				return true;
			}
		}
		return false;
	}
	
	public Pair<Integer, Integer> get_AdaptiveLVS_Distance(AbstTraceCC t1, AbstTraceCC t2) {
		
		int m = t1.length(); // length of left
		int n = t2.length(); // length of right
		
		// Cost matrix (column) since we can compute is column-wise 
		// if we do not want to backtrack the path
		int[] p = new int[m + 1];
		// indexes into strings left and right
		int i; // iterates through left
		int j; // iterates through right
		int costUpperLeft;

		long rightJ; // jth character of right
		int cost; // cost
		// Costs associated with complete deletion of left trace
		// (used for normalization)
		int costAllDelete; 
		// Costs associated with complete insertion of right trace
		// (used for normalization)
		int costAllInsert; 
		
		// Calculate first cost column
		// Corresponds to a full deletion of the left trace
		p[0] = 0;
		for (i = 1; i <= m; i++) {
			p[i] = p[i-1] + costDelete(t1, i-1);
		}
		costAllDelete = p[m];

		// Compute cost matrix column by column
		for (j = 1; j <= n; j++) {
			costUpperLeft = p[0];
			rightJ = t2.getLetterAt(j - 1);
			p[0] = p[0] + costInsert(t2, j - 1);
			for (i = 1; i <= m; i++) {
				cost = costUpperLeft + (t1.getLetterAt(i-1) == rightJ ? 0 : 1);
				if(cost > 0) {
					// Compare matching with renaming
					cost = Math.min(cost, costUpperLeft + costRename(t1, t2, i - 1, j - 1));
					if(cost > 0) {
						// Compare with Delete
						cost = Math.min(cost, p[i-1] + costDelete(t1, i - 1));
						if(cost > 0) {
							// Compare with Insert
							cost = Math.min(cost, p[i] + costInsert(t2, j - 1));
						}
					}
				}
				costUpperLeft = p[i];
				p[i] = cost;
			}
		}
		costAllInsert = p[0];
		return Pair.of(p[m], Math.max(costAllDelete, costAllInsert));
	}
	
	/**
	 * Get the deletion cost for the event at the given index.
	 * @param t Trace to be checked
	 * @param eventIndex Event index (to be deleted).
	 * @return 0 iff deletion free else 1
	 */
	private int costDelete(AbstTraceCC t, int eventIndex) {
		int[] abstractions = t.getAbstractionsAt(eventIndex);
		int a;
		// Iterate over all instruction
		// 0th entry contains number of instruction!
		// Check all added abstractions
		for (int i = 1; i <= abstractions[0]; i++) {
			a = abstractions[i];
			// No cost iff deletion is free
			if(this.freeDelete[a]) {
				return 0;
			}
		}
		return 1;
	}

	/**
	 * Get the insertion cost for the event at the given index.
	 * @param t Trace to be checked
	 * @param eventIndex Event index (to be inserted).
	 * @return 0 iff insertion free else 1
	 */
	private int costInsert(AbstTraceCC t, int eventIndex) {
		int[] abstractions = t.getAbstractionsAt(eventIndex);
		int a;
		// Iterate over all instruction
		// 0th entry contains number of instruction!
		// Check all added abstractions
		for (int i = 1; i <= abstractions[0]; i++) {
			a = abstractions[i];
			// No cost iff insertion is free
			if(this.freeInsert[a]) {
				return 0;
			}
		}
		return 1;
	}
	
	private int costRename(AbstTraceCC t1, AbstTraceCC t2, int eventIndex1, int eventIndex2)  {
		int[] abstractions1 = t1.getAbstractionsAt(eventIndex1);
		int[] abstractions2 = t2.getAbstractionsAt(eventIndex2);
	
		int i1 = 1, i2 = 1;
		int a1, a2;
		// TODO extensive Junit tests
		// Abstractions are sorted
		while(i1 <= abstractions1[0] && i2 <= abstractions2[0]) {
			a1 = abstractions1[i1];
			a2 = abstractions2[i2];
			if(a1 == a2 && freeRename[a1]) {
				return 0;
			}
			if(a1 < a2) {
				i1++;
			}
			else {
				i2++;
			}
		}
		return 1;
		
	}

	@Override
	public String getShortDescription() {
		return "AdaptiveLevenshtein";
	}

}
