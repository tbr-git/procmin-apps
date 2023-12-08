package de.rwth.processmining.tb.core.emd.grounddistances.controlflow;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * *
 * <p>
 * This code has been adapted from Apache Commons Lang 3.3.
 * </p>
 * 
 * @author sander
 *
 */
public class Levenshtein {

	public static double getNormalisedDistance(String[] left, String[] right) {
		if(left.length == 0 && right.length == 0) {
			return 0;
		}
		else {
			return getDistance(left, right) / (double) Math.max(left.length, right.length);
		}
	}

	public static double getNormalisedDistance(int[] left, int[] right) {
		if(left.length == 0 && right.length == 0) {
			return 0;
		}
		else {
			return getDistance(left, right) / (double) Math.max(left.length, right.length);
		}
	}

	public static int getDistance(String[] left, String[] right) {
		TObjectIntMap<String> map = new TObjectIntHashMap<>(10, 0.5f, -1);
		int lastIndex = -1;

		int[] leftL = new int[left.length];
		for (int i = 0; i < left.length; i++) {
			leftL[i] = map.adjustOrPutValue(left[i], 0, lastIndex + 1);
			if (leftL[i] == lastIndex + 1) {
				lastIndex++;
			}
		}

		int[] rightL = new int[right.length];
		for (int i = 0; i < right.length; i++) {
			rightL[i] = map.adjustOrPutValue(right[i], 0, lastIndex + 1);
			if (rightL[i] == lastIndex + 1) {
				lastIndex++;
			}
		}

		return getDistance(leftL, rightL);
	}

	public static int getDistance(int[] left, int[] right) {
		if (left == null || right == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		/*
		 * This implementation use two variable to record the previous cost
		 * counts, So this implementation use less memory than previous impl.
		 */

		int n = left.length; // length of left
		int m = right.length; // length of right

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		if (n > m) {
			// swap the input strings to consume less memory
			final int[] tmp = left;
			left = right;
			right = tmp;
			n = m;
			m = right.length;
		}

		int[] p = new int[n + 1];

		// indexes into strings left and right
		int i; // iterates through left
		int j; // iterates through right
		int upper_left;
		int upper;

		int rightJ; // jth character of right
		int cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			upper_left = p[0];
			rightJ = right[j - 1];
			p[0] = j;

			for (i = 1; i <= n; i++) {
				upper = p[i];
				cost = left[i - 1] == rightJ ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost
				p[i] = Math.min(Math.min(p[i - 1] + 1, p[i] + 1), upper_left + cost);
				upper_left = upper;
			}
		}

		return p[n];
	}

}
