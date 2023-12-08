package de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public record HFDDLogTransformStep<V extends CVariant>(
    BiComparisonDataSource<V> biCompDS, FilterTag filterTag) {

}
