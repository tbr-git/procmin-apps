package de.rwth.processmining.tb.core.sps.algorithm.measure;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

/**
 * Simple data class that wraps the filtered log and some statistics on it. 
 * @author brockhoff
 *
 * @param <E> Variant type
 */
public record LogNStats<E extends CVariant>(BiComparisonDataSource<? extends E> biCompDS, 
		double probNonEmptyLeft, double probNonEmptyRight, boolean change) {
}
