package de.rwth.processmining.tb.core.emd.dataview;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.XLogTraceFeatureExtractor;

public class DescriptorDistancePair<F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> {
	private final D dist;
	
	private final XLogTraceFeatureExtractor<F> descFac;

	public DescriptorDistancePair(D dist, XLogTraceFeatureExtractor<F> descFac) {
		super();
		this.dist = dist;
		this.descFac = descFac;
	}
	
	public D getDistance() {
		return dist;
	}
	
	public XLogTraceFeatureExtractor<F> getDescriptorFactory() {
		return descFac;
	}
	
	public String getShortDescription() {
		return descFac.getShortDescription() + " - " + dist.getShortDescription();
		
	}

}
