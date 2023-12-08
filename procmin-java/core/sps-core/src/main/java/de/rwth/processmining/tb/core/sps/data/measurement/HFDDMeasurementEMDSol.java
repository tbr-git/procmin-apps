package de.rwth.processmining.tb.core.sps.data.measurement;

import java.util.Optional;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.emd.dataview.ViewConfig;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolAnalyzer;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;

public class HFDDMeasurementEMDSol<F extends TraceDescriptor> extends HFDDMeasurement {
	
	private final Optional<EMDSolContainer<F>> emdSol;
	
	
	/**
	 * Copy constructor if measurements would be equal for two viewConfigs and perspective descriptors.
	 * @param m
	 * @param perspectiveDescription
	 * @param viewConfig
	 */
	public HFDDMeasurementEMDSol(HFDDMeasurementEMDSol<F> m, PerspectiveDescriptor perspectiveDescription, 
			ViewConfig viewConfig) {
		super(perspectiveDescription, viewConfig, m.getProbLeftNonEmpty(), m.getProbRightNonEmpty(), 
				m.getMetric(), m.getFlow2EmptyTraceCost(), m.isMetricDefined(), m.isProbabilityZero());
		this.emdSol = m.getEMDSolution();
	}

	public HFDDMeasurementEMDSol(PerspectiveDescriptor perspectiveDescription, ViewConfig viewConfig,
			double probLeftNonEmpty, double probRightNonEmpty, Optional<EMDSolContainer<F>> emdSol, 
			boolean metricDefined, boolean probabilityZero) {
		super(perspectiveDescription, viewConfig, probLeftNonEmpty, probRightNonEmpty, 
				emdSol.isPresent() ? Optional.of(emdSol.get().getEMD()) : Optional.empty(), 
				emdSol.isPresent() ? Optional.of(EMDSolAnalyzer.flowEmptyCost(emdSol.get())) : Optional.empty(), 
						metricDefined, probabilityZero);
		this.emdSol = emdSol;
	}
	
	public Optional<EMDSolContainer<F>> getEMDSolution() {
		return this.emdSol;
	}

}
