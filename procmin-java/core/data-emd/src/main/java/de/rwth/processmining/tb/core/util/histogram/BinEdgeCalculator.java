package de.rwth.processmining.tb.core.util.histogram;

import java.util.List;

public interface BinEdgeCalculator {
	
	double[] calculateBinEdges(List<Double> lTimes);

}
