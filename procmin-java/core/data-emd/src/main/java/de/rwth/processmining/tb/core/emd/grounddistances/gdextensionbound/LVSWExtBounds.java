package de.rwth.processmining.tb.core.emd.grounddistances.gdextensionbound;

import org.apache.commons.lang3.tuple.Triple;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;

public class LVSWExtBounds implements GDRightExtensionBounds {
	
	protected DistanceBoundType currentBoundType = DistanceBoundType.NONE;

	@Override
	public double get_distance(BasicTraceCC t1, BasicTraceCC t2) {
		Triple<Double, Double, Double> distances = getRightExtBoundedDistance(t1, t2);
		
		switch(currentBoundType) {
			case NONE:
				return distances.getLeft();
			case LOWER: 
				return distances.getMiddle();
			case UPPER: 
				return distances.getRight();
			default:
				return 1;
		}
	}

	@Override
	public String getShortDescription() {
		return "Levensthein distance with an additional lower and upper bound on the distance "
				+ "for all possible extension of the right-hand side trace"; 
	}

	@Override
	public Triple<Double, Double, Double> getRightExtBoundedDistance(BasicTraceCC trace1, BasicTraceCC trace2) {
			return getRightExtBoundedDistance(trace1.getTraceCategories(), trace2.getTraceCategories());
	}
	
	public static Triple<Double, Double, Double> getRightExtBoundedDistance(int[] t1, int[] t2) {
		if (t1 == null || t2 == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		int m = t1.length; // length of left
		int n = t2.length; // length of right

		if (m == 0) {
			return Triple.of(1.0, 1.0, 1.0);
		} else if (m == 0) {
			return Triple.of(1.0, 1.0, 1.0);
		}

		////////////////////////////////////////
		// Don't swap traces based on length
		////////////////////////////////////////

		int[] p = new int[m + 1];

		// indexes into strings left and right
		int i; // iterates through left
		int j; // iterates through right
		int upper_left;
		int left;

		int rightJ; // jth character of right
		int cost; // cost

		// Fill left-most "before string" column
		for (i = 0; i <= m; i++) {
			p[i] = i;
		}

		for (j = 1; j <= n; j++) {
			upper_left = p[0];
			rightJ = t2[j - 1];
			p[0] = j;

			for (i = 1; i <= m; i++) {
				left = p[i];
				cost = t1[i - 1] == rightJ ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost
				p[i] = Math.min(Math.min(p[i - 1] + 1, p[i] + 1), upper_left + cost);
				upper_left = left;
			}
		}
		double lb = 1.0;
		double ub = 1.0;
		for (i = 0; i <= m; i++) {
			lb = Math.min(lb, ((double) p[i]) / Math.max(m, (n + m - i)));
		}


		return Triple.of(((double) p[m]) / Math.max(n, m), lb, ub);
	}

	@Override
	public void configureReturnedDistance(DistanceBoundType boundType) {
		this.currentBoundType = boundType;
	}

}
