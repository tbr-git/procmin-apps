package de.rwth.processmining.tb.core.sps.algorithm.measure;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;

/**
 * Specifies base functionalities that a class that measures (e.g., using EMD)
 * differences w.r.t. CS Graph vertices.
 * <p>
 * <b>IMPORTANT: </b>
 * Implementing subclasses should be thread-safe.
 * 
 * @author brockhoff
 *
 */
public interface CSVertexMeasurer <T extends CVariant> {
	
	/**
	 * Measure a vertex given the data.
	 * Running <b>must not</b> change the state of the measurer!!! 
	 * @param <T> Type of the variants
	 * @param v Handle to the CSGraph Vertex
	 * @param biCompDS Data source to run the measurement between two logs on
	 * @param safe Add the result of the measurement to the vertex data.  
	 * @return Measurement result
	 */
	public boolean processVertex(CSGraphVertex v, BiComparisonDataSource<? extends T> biCompDS);

	/**
	 * Get the description for this measurement.
	 * @return
	 */
	public PerspectiveDescriptor getMeasurementDescription();

}
