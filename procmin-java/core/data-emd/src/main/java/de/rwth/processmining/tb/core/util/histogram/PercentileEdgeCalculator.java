package de.rwth.processmining.tb.core.util.histogram;

import java.util.Arrays;
import java.util.List;

import de.rwth.processmining.tb.core.util.MathUtil;

public class PercentileEdgeCalculator implements BinEdgeCalculator {
	
	private int[] binQuantils;
	
	public PercentileEdgeCalculator(int[] binQuantils) {
		this.binQuantils = binQuantils;
		Arrays.sort(this.binQuantils);
	}

	@Override
	public double[] calculateBinEdges(List<Double> lTimes) {
		double[] bins = MathUtil.getPercentileBins(lTimes, binQuantils);
		// Ensure that bin boarders differ at least by 10^-6
		for(int i = 1; i < bins.length; i++) {
			bins[i] = Math.max(bins[i-1] + 1e-6, bins[i]);
		}
		return bins;
	}

}
