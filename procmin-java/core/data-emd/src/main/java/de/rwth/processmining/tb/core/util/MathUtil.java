package de.rwth.processmining.tb.core.util;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class MathUtil {

	/**
	 * Calculates the bin boundaries for the given percentiles including infinity.
	 * @param lVal List of values
	 * @param percentiles Percentiles (0, 100)
	 * @return Bin boundaries for given percentiles with last boundary positive infinity.
	 */
	public static double[] getPercentileBins(List<Double> lVal, int[] percentiles) {
		//TODO Handling the case where most values are equal
		double[] quantileTimes = new double[percentiles.length + 1];
		quantileTimes[percentiles.length] = Double.POSITIVE_INFINITY;
		double[] arrVal = lVal.stream().mapToDouble(Double::doubleValue).toArray();
		Percentile perc = new Percentile();
		perc.setData(arrVal);
		for(int i = 0; i < percentiles.length; i++) {
			//Linear interpolation between neighboring values
			quantileTimes[i] = perc.evaluate(percentiles[i]);
		}
		return quantileTimes;
	}
	
	public static double getPercentile(double[] arr, double p) {
		Percentile perc = new Percentile();
		perc.setData(arr);
		return perc.evaluate(p);
	}
}
