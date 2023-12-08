package de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;

public interface HFDDVertexLogTransformerOuterContext {
	
	/**
	 * Transform the provided log based on some context information that is "outside" of the vertex v.
	 * For example, residual flow (only traces that do not intersect with a different vertex).
	 * @param v
	 * @param biCompDS
	 * @return
	 * @throws SLDSTransformerBuildingException 
	 */
	public<V extends CVariant> HFDDLogTransformStep<V> getDataSourceOuterContext(HFDDVertex v, 
	    BiComparisonDataSource<V> biCompDS) throws SLDSTransformerBuildingException;

}
