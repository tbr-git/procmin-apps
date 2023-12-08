package de.rwth.processmining.tb.core.emd.language;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public interface StochasticLanguage<T extends TraceDescriptor> {

	public int getNumberOfTraceVariants();
	
	public int getAbsoluteNbrOfTraces();
	
	public StochasticLanguageIterator<T> iterator(); 

	//public JSONObject toJson();
	
	public double getTotalWeight();
	
	public double getProbability(TraceDescriptor traceDesc);
	
	public boolean contains(TraceDescriptor traceDescriptor);
}
