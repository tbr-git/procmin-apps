package de.rwth.processmining.tb.core.sps.algorithm.measure;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.transformer.ProbMassNonEmptyTrace;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurement;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurementEMDSol;

/**
 * Specifies base functionalities that a class that measures (e.g., using EMD)
 * differences w.r.t. HFDD vertices.
 * <p>
 * <b>IMPORTANT: </b>
 * Implementing subclasses should be thread-safe.
 * 
 * @param <V> Variant type that serves as input
 * @param <F> Feature extracted from the variant (to interpret the EMD solution)
 * @author brockhoff
 *
 */
public interface HFDDVertexMeasurer<V extends CVariant, F extends TraceDescriptor> {
	
	/**
	 * Measure a vertex given the data.
	 * Running <b>must not</b> change the state of the measurer!!! 
	 * @param <V> Minimal type of the variants
	 * @param <B> Actual variant type
	 * @param v Handle to the HFDD Vertex
	 * @param biCompDS Data source to run the measurement between two logs on
	 * @param safe Add the result of the measurement to the vertex data.  
	 * @return Measurement result
	 */
	public<B extends V> HFDDMeasurement measureVertex(HFDDVertex v, BiComparisonDataSource<B> biCompDS, boolean safe);

	/**
	 * Run a detailed measurement on the vertex given the data source.
	 * In contrast to this{@link #measureVertex(HFDDVertex, BiComparisonDataSource, boolean)}, 
	 * the returned {@link HFDDMeasurementEMDSol} object will contain more information, 
	 * in particular, the actual best flow.
	 * Running <b>must not</b> change the state of the measurer!!! 
	 * 
	 * @param <V> Type of the variants
	 * @param <F> Type of the features over which EMD is computed
	 * @param v Handle to the HFDD Vertex
	 * @param biCompDS Data source to run the measurement between two logs on
	 * @param safe Add the result of the measurement to the vertex data.  
	 * @return Measurement result
	 */
	public<B extends V> HFDDMeasurementEMDSol<F> measureVertexDetails(HFDDVertex v, BiComparisonDataSource<B> biCompDS, boolean safe);

	/**
	 * Get the probability mass of non-empty traces for the provided vertex.
	 * @param <V> Type of the variants
	 * @param v Handle to the HFDD Vertex
	 * @param biCompDS Data source to run the measurement between two logs on
	 * @return Non-empty trace probability mass for left and right log.
	 */
	public<B extends V> ProbMassNonEmptyTrace getProbabilityMassNonEmpty(HFDDVertex v, BiComparisonDataSource<B> biCompDS);
	
	/**
	 * Get the description for this measurement.
	 * @return
	 */
	public PerspectiveDescriptor getMeasurementDescription();

}
