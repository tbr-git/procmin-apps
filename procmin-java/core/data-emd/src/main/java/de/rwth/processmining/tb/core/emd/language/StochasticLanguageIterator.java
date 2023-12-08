package de.rwth.processmining.tb.core.emd.language;

import java.util.Iterator;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public interface StochasticLanguageIterator<T extends TraceDescriptor> extends Iterator<T>{
	public double getProbability();
}
