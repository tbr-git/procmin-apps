package de.rwth.processmining.tb.core.util.histogram;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.stat.Frequency;

public class ConsistentKDeletionHistogramDiscretizer {
	
	/**
	 * Implementation of the "consistent" k-deletion histogram method proposed in 
	 * 
	 * <p>
	 * Rachel Behar, Sara Cohen. 2020. Optimal Histograms with Outliers. In Advances in Database Technology - EDBT 2020 
	 * </p>
	 * @param values Values
	 * @param bins Number of bins
	 * @param k Maximum number of deletions
	 * @return Bin Edges
	 */
	public static HistogramSpecDataEdges consistentKDeletionHistogram(int values[], int bins, int k) {
		// Extract frequency statistics
		Frequency freq = new Frequency();
		Arrays.stream(values).forEach(v -> freq.addValue(v));
		
		// Safe the domain / support  
		long[] valuesDom = new long[freq.getUniqueCount()];
		Iterator<Comparable<?>> itValues = freq.valuesIterator();
		int i = 0;
		while (itValues.hasNext()) {
			valuesDom[i] = (Long) itValues.next();
			i++;
		}
		
		// Initialize the SSE computation info
		Pair<long[], long[]> sseInfo = initFastSSEComputation(valuesDom, freq);
		
		if (bins == 1) {
			Triple<Integer, Integer, Double> bestSingleBinParam = 
					consistentSingleBucket(valuesDom, freq, sseInfo, k, valuesDom.length - 1);
			// Return optimal 1-bin
			List<Pair<Integer, Integer>> resBins = new LinkedList<>();
			resBins.add(Pair.of((int) valuesDom[bestSingleBinParam.getLeft()], 
					(int) valuesDom[bestSingleBinParam.getMiddle()]));
			return new HistogramSpecDataEdges(resBins, Optional.of(bestSingleBinParam.getRight()));
		}
	
		// Array with best possible error: (Samples considered * bins * k)
		// Zero initialization for (p = 0 and bins >=! 1 for any k important)
		double[][][] mError = new double[valuesDom.length][bins][k + 1];
		// Array: Samples for the single bin * k * edges
		int[][][] singleBinEdges = new int[valuesDom.length][k + 1][2];
		// Include p = 0 to initialize the single bin edges -> choice between [p, p] and drop all
		for (int p = 0; p < valuesDom.length; p++) {
			for (int kPrime = 0; kPrime <= k; kPrime++) {
				Triple<Integer, Integer, Double> bestSingleBinParam = 
						consistentSingleBucket(valuesDom, freq, sseInfo, kPrime, p);
				mError[p][0][kPrime] = bestSingleBinParam.getRight();
				singleBinEdges[p][kPrime][0] = bestSingleBinParam.getLeft();
				singleBinEdges[p][kPrime][1] = bestSingleBinParam.getMiddle();
			}
		}
		
		for (int binPrime = 2; binPrime <= bins; binPrime++) {
			for(int p = 1; p < valuesDom.length; p++) {
				for (int kPrime = 0; kPrime <= k; kPrime++) {
					updateConsistentMatrix(valuesDom, freq, sseInfo, mError, p, binPrime, kPrime);
				}
			}
		}
		
		List<Pair<Integer, Integer>> resBins = recursiveTraceback(valuesDom, freq, sseInfo, mError, singleBinEdges, 
				valuesDom.length - 1, bins, k);
		
		return new HistogramSpecDataEdges(resBins, 
				Optional.of(mError[valuesDom.length - 1][bins - 1][k]));
	}
	
	public static void updateConsistentMatrix(long[] valuesDom, 
			Frequency freq, Pair<long[], long[]> sseInfo, 
			double[][][] mError, 
			int valIndex, int bin, int k)  {
	
		int count = ((Long) freq.getCount(valuesDom[valIndex])).intValue();
		double curBestE = Double.POSITIVE_INFINITY;
		if (count <= k) {
			// only called for valIndex >= 2
			curBestE = mError[valIndex - 1][bin - 1][k - count];
		}
		
		double tmpE;
		for (int q = valIndex - 1; q >= 0; q--) {
			tmpE = mError[q][bin - 2][k] + getSSE(valuesDom, sseInfo, q + 1, valIndex);
			curBestE = Math.min(curBestE, tmpE);
		}

		mError[valIndex][bin - 1][k] = curBestE;
	}
	
	public static List<Pair<Integer, Integer>> recursiveTraceback(long[] valuesDom, 
			Frequency freq, Pair<long[], long[]> sseInfo, 
			double[][][] mError, int[][][] singleBinEdges,
			int valIndex, int bin, int k) {
	
		////////////////////
		// Base Cases
		////////////////////
		if (valIndex == 0 || bin == 1) {
			// valIndex == 0: if there is only a single value, one bin is always sufficient
			int binEdgeLeft = singleBinEdges[valIndex][k][0];
			int binEdgeRight = singleBinEdges[valIndex][k][1];
			List<Pair<Integer, Integer>> resBins = new LinkedList<>();
			if (binEdgeLeft != -1) {
				resBins.add(Pair.of((int) valuesDom[binEdgeLeft], (int) valuesDom[binEdgeRight]));
			}
			return resBins;
		}
		else {
			int count = ((Long) freq.getCount(valuesDom[valIndex])).intValue();
			double curBestE = Double.POSITIVE_INFINITY;
			boolean worthABin = true;
			int fromValIndex = -1;
			int fromBin = -1;
			int fromK = -1;
			if (count <= k) {
				worthABin = false;
				curBestE = mError[valIndex - 1][bin - 1][k - count];
				fromValIndex = valIndex - 1;
				fromBin = bin;
				fromK = k - count;
			}
			
			double tmpE;
			for (int q = valIndex - 1; q >= 0; q--) {
				tmpE = mError[q][bin - 2][k] + getSSE(valuesDom, sseInfo, q + 1, valIndex);
				if (tmpE < curBestE) {
					worthABin = true;
					curBestE = tmpE;
					fromValIndex = q;
					fromBin = bin - 1;
					fromK = k;
				}
			}
			
			
			// Recurse
			List<Pair<Integer, Integer>> resBins = recursiveTraceback(valuesDom, freq, sseInfo, mError, singleBinEdges, 
					fromValIndex, fromBin, fromK);
			if (worthABin) {
				resBins.add(Pair.of((int) valuesDom[fromValIndex + 1], (int) valuesDom[valIndex]));
			}

			return resBins;
		}
		
	}
	
	/**
	 * @param valuesDom
	 * @param freq
	 * @param k
	 * @param spliceEnd splicing the valueDom array (inclusive) 
	 * @return
	 */
	public static Triple<Integer, Integer, Double> consistentSingleBucket(long[] valuesDom, 
			Frequency freq, Pair<long[], long[]> sseInfo, int k, int spliceEnd) {
	
		if (freq.getCumFreq(valuesDom[spliceEnd]) <= k) {
			return Triple.of(-1, -1, 0.0);
		}
		int q = spliceEnd;
		int kRight = 0;
		while (kRight + freq.getCount(valuesDom[q]) <= k) {
			kRight += freq.getCount(valuesDom[q]);
			q--;
		}
		
		int bestP = 0;
		int bestQ = q;
		double e = getSSE(valuesDom, sseInfo, 0, q);
		double tmpE;
		int p = 0;
		int kLeft = 0;
		
		while (kLeft + freq.getCount(valuesDom[p]) <= k) {
			kLeft += freq.getCount(valuesDom[p]);
			p++;
			while (kLeft + kRight > k) {
				q++;
				kRight -= freq.getCount(valuesDom[q]);
			}
			tmpE = getSSE(valuesDom, sseInfo, p, q);
			
			if (tmpE < e) {
				e = tmpE;
				bestP = p;
				bestQ = q;
			}
		}
		return Triple.of(bestP, bestQ, e);
	}
	
	/**
	 * Initialize the arrays required for O(1) Sum Squared Error for a bucket computation for arbitrary
	 *  [value slice start, value slice end] (both endpoints inculsive!)
	 * Method taken from: 
	 * <p>
	 * H. V. Jagadish, Nick Koudas, S. Muthukrishnan, Viswanath Poosala, Kenneth C. Sevcik, and Torsten Suel. 1998. 
	 * Optimal Histograms with Quality Guarantees. In VLDB. 275-286.
	 * 
	 * @param valuesDom Domain/Support of the frequency statistics
	 * @param freq Value frequency statistics
	 * @return Arrays P and PP from the paper (prefix sum and sum of squared prefixes)
	 */
	public static Pair<long[], long[]> initFastSSEComputation(long[] valuesDom, Frequency freq) {
		long[] p = new long[valuesDom.length];
		long[] pp = new long[valuesDom.length];
		
		for (int i = 0; i < valuesDom.length; i++) {
			long v = valuesDom[i];
			p[i] = freq.getCumFreq(v);
			pp[i] = (freq.getCount(v) * freq.getCount(v)) + (i > 0 ? pp[i - 1] : 0);
		}
		
		return Pair.of(p, pp);
	}
	
	/**
	 * Compute the Sum Squared Error for a bucket [value slice start, value slice end] (both endpoints inculsive!)
	 * for the provided frequency statistics.
	 * Method taken from: 
	 * <p>
	 * H. V. Jagadish, Nick Koudas, S. Muthukrishnan, Viswanath Poosala, Kenneth C. Sevcik, and Torsten Suel. 1998. 
	 * Optimal Histograms with Quality Guarantees. In VLDB. 275-286.
	 * 
	 * @param domainValues Domain/Support values of multiset to be discretized
	 * @param sseInfo Pair of two arrays from the SSE can be computed in O(1)
	 * @param valSliceStart value of the slice start (inclusive) (index in domain/support values)
	 * @param valSliceEnd value of the slice end (inclusive)(index in domain/support values)
	 * @return
	 */
	public static double getSSE(long[] domainValues, Pair<long[], long[]> sseInfo, int valSliceStart, int valSliceEnd) {
		long[] p = sseInfo.getLeft();
		long[] pp = sseInfo.getRight();
		long sumFreqSquared = pp[valSliceEnd] - (valSliceStart > 0 ? pp[valSliceStart - 1] : 0);
		// Note the double in the denominator
		double expectedUniformFreq = (p[valSliceEnd] - (valSliceStart > 0 ? p[valSliceStart - 1] : 0)) 
				/ (1.0 + domainValues[valSliceEnd] - domainValues[valSliceStart]);
		
		return sumFreqSquared - 
				(1 + domainValues[valSliceEnd] - domainValues[valSliceStart]) * expectedUniformFreq * expectedUniformFreq;
	}

}
