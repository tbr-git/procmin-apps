package de.rwth.processmining.tb.core.emd.dataview;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;

public abstract class ViewRealization<F extends TraceDescriptor> {
	
	private final static Logger logger = LogManager.getLogger( ViewRealization.class );
	
	protected Optional<EMDSolContainer<F>> emdSol;
	
	private final ViewRealizationMeta viewDescription;
	
	public ViewRealization(ViewRealizationMeta viewDescription) {
		this.emdSol = Optional.empty();
		this.viewDescription = viewDescription;
	}
	
	public EMDSolContainer<F> getEMDSol() throws ViewDataException {
		if(!emdSol.isPresent())
			this.populate();
		return emdSol.get();
	}
	
	public abstract void populate() throws ViewDataException;
	
	public void reduceMemoryConsumption() throws ViewDataException {
		// Initializing a view vill delete its reference to the window data
		// -> The XLog instances in the window data are the most space-consuming elements
		this.populate();
	}

	public ViewIdentifier getViewIdentifier() {
		return viewDescription.getViewIdentifier();
	}
	
	public ViewRealizationMeta getRealizationMeta() {
		return this.viewDescription;
	}
	
	public abstract boolean isRealizable();
}
