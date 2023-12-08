package de.rwth.processmining.tb.core.sps.algorithm.measure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.transformer.ProbMassNonEmptyTrace;
import de.rwth.processmining.tb.core.sps.data.csgraph.CSMeasurementTypes;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraph;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertexCS;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurementEMDSol;

public abstract class CSVertexMeasurerBase
  <V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
  implements CSVertexMeasurer<V> {

	private final static Logger logger = LogManager.getLogger( CSVertexMeasurerBase.class );

	/**
	 * Handle to CS Graph that defines the neighborhood context.
	 */
	private final CSGraph csGraph;
	
	/**
	 * Save measurer.
	 */
	private HFDDVertexMeasurerImpl<V, F, D> measurer;
	
	/**
	 * Measure only probability.
	 */
	private boolean probabilityOnly;
	
	public CSVertexMeasurerBase(CSGraph csGraph) {
		super();
		this.csGraph = csGraph;
		measurer = null;
		this.probabilityOnly = false;
	} 

	public CSVertexMeasurerBase(CSGraph csGraph, boolean probabilityOnly) {
		super();
		this.csGraph = csGraph;
		measurer = null;
		this.probabilityOnly = probabilityOnly;
	} 
	
	@Override
	public boolean processVertex(CSGraphVertex v, BiComparisonDataSource<? extends V> biCompDS) {
	
		// Instantiate if not yet instantiated
		if (measurer == null) {
			synchronized(this) {
				if (measurer == null) {
					measurer = getMeasurer(biCompDS);	
				}
			}
		}
		
		////////////////////////////////////////
		// Run Measurement
		////////////////////////////////////////
		//////////////////////////////
		// Add EMD Solution
		//////////////////////////////
		if (!probabilityOnly && (v instanceof CSGraphVertexCS vCS)) {
			////////////////////
			// EMD
			////////////////////
			HFDDMeasurementEMDSol<F> m = measurer.measureVertexDetails(v.getHfddVertexRef(), biCompDS, false);
			//TODO Store probability in HFDD measurement properly
			vCS.setProbabilityMassInfo(getMeasurementType(), new ProbMassNonEmptyTrace(
					m.getProbLeftNonEmpty(), m.getProbRightNonEmpty(), m.isProbabilityZero()));
			vCS.setMeasurement(getMeasurementType(), m);
			return true;
		}
		else {
			// Only probabilities
			ProbMassNonEmptyTrace p = measurer.getProbabilityMassNonEmpty(v.getHfddVertexRef(), biCompDS);
			v.setProbabilityMassInfo(getMeasurementType(), p);
			return true;
		}
	}
	
	protected abstract HFDDVertexMeasurerImpl<V, F, D> getMeasurer(BiComparisonDataSource<? extends V> biCompDS);
	
	protected abstract CSMeasurementTypes getMeasurementType();

	public CSGraph getCsGraph() {
		return csGraph;
	}

	public boolean isProbabilityOnly() {
		return probabilityOnly;
	}

	public void setProbabilityOnly(boolean probabilityOnly) {
		this.probabilityOnly = probabilityOnly;
	}
	

}
