package de.rwth.processmining.tb.core.util.histogram;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Specification of a histogram using data points for the bin edges.
 * If no new data is going to be added (i.e., the histogram is mused to describe the data only),
 * this option makes sense.
 */
public record HistogramSpecDataEdges(List<Pair<Integer, Integer>> bins, Optional<Double> sse) {

}
