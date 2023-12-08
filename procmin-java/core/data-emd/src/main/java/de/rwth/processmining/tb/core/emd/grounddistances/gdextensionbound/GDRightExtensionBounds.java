package de.rwth.processmining.tb.core.emd.grounddistances.gdextensionbound;

import org.apache.commons.lang3.tuple.Triple;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;

public interface GDRightExtensionBounds extends TraceDescDistCalculator<BasicTraceCC> {
	
	/**
	 * Compute the trace descriptor distance + lower and upper bounds on extension of the right discriptor.
	 * @param tl
	 * @param tr
	 * @return (distance between traces without extension, 
	 * 	lower bound on distance for all possible extension of the right trace,
	 * 	upper bound on distance for all possible extension of the right trace)
	 */
	public Triple<Double, Double, Double> getRightExtBoundedDistance(BasicTraceCC tl, BasicTraceCC tr);
	
	/**
	 * Configure which bound {@link TraceDescDistCalculator#get_distance(TraceDescriptor, TraceDescriptor)} 
	 * should return.
	 * 
	 * @param boundType Bound type;
	 */
	public void configureReturnedDistance(DistanceBoundType boundType);
	
}
