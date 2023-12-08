package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.editpatterns;

import de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics.LVSEditOperation;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class LVSEditOpTriple {
	
	final LVSEditOperation op;

	final TraceDescriptor traceL; 

	final TraceDescriptor traceR;

	public LVSEditOpTriple(LVSEditOperation op, TraceDescriptor traceL, TraceDescriptor traceR) {
		super();
		this.op = op;
		this.traceL = traceL;
		this.traceR = traceR;
	}

	public LVSEditOperation getOp() {
		return op;
	}

	public TraceDescriptor getTraceL() {
		return traceL;
	}

	public TraceDescriptor getTraceR() {
		return traceR;
	}
	

}
