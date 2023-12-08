package de.rwth.processmining.tb.core.sps.data.measurement;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.emd.dataview.ViewConfig;

public class HFDDMeasurement {
	
	/**
	 * {@link ViewConfig} used to instantiate the measurement.
	 */
	@JsonIgnore
	private final ViewConfig viewConfig;
	
	/**
	 * Name of the perspective that is realized using the provided {@link this#viewConfig}.
	 */
	@JsonIgnore
	private final PerspectiveDescriptor perspectiveDescription;
	
	/**
	 * Metric value/measurement value.
	 */
	private final Optional<Double> metric;
	
	/**
	 * Cost induced by flows to empty traces.
	 */
	private final Optional<Double> flow2EmptyTraceCost;
	
	/**
	 * Is the metric value defined?
	 */
	private final boolean metricDefined;
	
	/**
	 * Probability in left log that the trace is non-empty 
	 */
	private final double probLeftNonEmpty;

	/**
	 * Probability in right log that the trace is non-empty 
	 */
	private final double probRightNonEmpty;
	
	/**
	 * Probability mass on both sides is zero;
	 */
	private final boolean probabilityZero;
	
	/**
	 * Copy constructor if measurements would be equal for two viewConfigs and perspective descriptors.
	 * @param m
	 * @param perspectiveDescription
	 * @param viewConfig
	 */
	public HFDDMeasurement(HFDDMeasurement m, PerspectiveDescriptor perspectiveDescription, ViewConfig viewConfig) {
		this.perspectiveDescription = perspectiveDescription;
		this.viewConfig = viewConfig;
		
		this.probLeftNonEmpty = m.getProbLeftNonEmpty();
		this.probRightNonEmpty = m.getProbRightNonEmpty();
		this.metric = m.getMetric();
		this.flow2EmptyTraceCost = m.getFlow2EmptyTraceCost();
		this.metricDefined = m.isMetricDefined();
		this.probabilityZero = m.isProbabilityZero(); 
	}
	
	public HFDDMeasurement(PerspectiveDescriptor perspectiveDescription, ViewConfig viewConfig, 
			double probLeftNonEmpty, double probRightNonEmpty, 
			Optional<Double> metric, Optional<Double> flow2EmptyTraceCost,
			boolean metricDefined, boolean probabilityZero) {
		this.perspectiveDescription = perspectiveDescription;
		this.viewConfig = viewConfig;
		this.probLeftNonEmpty = probLeftNonEmpty;
		this.probRightNonEmpty = probRightNonEmpty;
		this.metric = metric;
		this.flow2EmptyTraceCost = flow2EmptyTraceCost;
		this.metricDefined = metricDefined;
		this.probabilityZero = probabilityZero;
	}

	public HFDDMeasurement(PerspectiveDescriptor perspectiveDescription, ViewConfig viewConfig, 
			double probLeftNonEmpty, double probRightNonEmpty, boolean metricDefined, boolean probabilityZero) {
		this(perspectiveDescription, viewConfig, probLeftNonEmpty, probRightNonEmpty, 
				Optional.empty(), Optional.empty(), metricDefined, probabilityZero);
	}

	public HFDDMeasurement(PerspectiveDescriptor perspectiveDescription, ViewConfig viewConfig, 
			double probLeftNonEmpty, double probRightNonEmpty, double metricValue, 
			double flow2EmptyTraceCost, boolean probabilityZero) {
		this(perspectiveDescription, viewConfig, probLeftNonEmpty, probRightNonEmpty, 
				Optional.of(metricValue), Optional.of(flow2EmptyTraceCost), true, probabilityZero);
	}

	public ViewConfig getViewConfig() {
		return viewConfig;
	}

	public PerspectiveDescriptor getPerspectiveDescription() {
		return perspectiveDescription;
	}

	public Optional<Double> getMetric() {
		return metric;
	}

	public boolean isMetricDefined() {
		return metricDefined;
	}

	public double getProbLeftNonEmpty() {
		return probLeftNonEmpty;
	}

	public double getProbRightNonEmpty() {
		return probRightNonEmpty;
	}

	public Optional<Double> getFlow2EmptyTraceCost() {
		return flow2EmptyTraceCost;
	}

	public boolean isProbabilityZero() {
		return probabilityZero;
	}

}
