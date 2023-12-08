package de.rwth.processmining.tb.core.emd.dataview;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.deckfour.xes.info.impl.XLogInfoImpl;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.grounddistances.cfplusbins.TimeBinnedWLVSWithEdit;
import de.rwth.processmining.tb.core.emd.grounddistances.controlflow.LevenshteinStringStateful;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTrace;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.BasicTraceDescriptorFactory;

public class DescriptorDistancePairFactory {
	
	public static Collection<DescriptorDistancePair<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>>> 
    availableSubViewPairsFor (
        DescriptorDistancePair<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>> pair) {
		
		//TODO We might add pair twice
	  // TODO HACK INTEGRATION DO IT PROPERLYJ
		List<DescriptorDistancePair<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>>> l = 
		    new LinkedList<>();
		if(pair.getDistance() instanceof TimeBinnedWLVSWithEdit) {
      l.add(new DescriptorDistancePair<BasicTrace, TraceDescDistCalculator<BasicTrace>>(
          new LevenshteinStringStateful(), new BasicTraceDescriptorFactory(XLogInfoImpl.NAME_CLASSIFIER)));
		}
		return l;
	}

	//public static<F extends TraceDescriptor> Collection<DescriptorDistancePair<? super F, TraceDescDistCalculator<? super F>>> 
  //  availableSubViewPairsFor (
  //      DescriptorDistancePair<F, ? extends TraceDescDistCalculator<F>> pair) {
	//	
	//	//TODO We might add pair twice
	//  // TODO HACK INTEGRATION DO IT PROPERLYJ
	//	List<DescriptorDistancePair<? super F, TraceDescDistCalculator<? super F>>> l = new LinkedList<>();
	//	if(pair.getDistance() instanceof TimeBinnedWLVSWithEdit) {
	//		l.add(new DescriptorDistancePair(new LevenshteinStringStateful(), new BasicTraceDescriptorFactory(XLogInfoImpl.NAME_CLASSIFIER)));
	//	}
	//	return l;
	//}

}
