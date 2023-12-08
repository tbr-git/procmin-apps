package de.rwth.processmining.tb.core.emd.language;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public interface OrderedStochasticLanguage<T extends TraceDescriptor> extends StochasticLanguage<T> {
	
	public T get(int index);
	
}
