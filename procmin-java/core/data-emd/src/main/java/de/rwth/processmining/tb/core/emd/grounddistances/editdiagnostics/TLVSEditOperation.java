package de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class TLVSEditOperation extends LVSEditOperation {
	
	private final int binDiff;

	public TLVSEditOperation(LVSOpNames operation, int binDiff, int indexL, int indexR) {
		super(operation, indexL, indexR);
		this.binDiff = binDiff;
	}
	
	public int getBinDiff() {
		return binDiff;
	}

	public String getDescription(TraceDescriptor traceL, TraceDescriptor traceR) {
		String strOp;
		switch(operation) {
			case MATCH:
				strOp = LVSOpNames.MATCH.toString() + "(" + traceL.toString(indexL) + 
					"-" + traceR.toString(indexR) + ")";	
				break;
			case TMATCH:
				strOp = LVSOpNames.TMATCH.toString() + "-" + binDiff + "(" + traceL.toString(indexL) + 
					"-" + traceR.toString(indexR) + ")";	
				break;
			case INSERT:
				strOp = LVSOpNames.INSERT.toString() + "-" + binDiff + "(" + traceR.toString(indexR) + ")";	
				break;
			case DELETE:
				strOp = LVSOpNames.DELETE.toString() + "-" + binDiff + "(" + traceL.toString(indexL) + ")";	
				break;
			case RENAME:
				strOp = LVSOpNames.RENAME.toString() + "-" + binDiff + "(" + traceL.toString(indexL) + 
					"-" + traceR.toString(indexR) + ")";	
				break;
			default:
				strOp = "";
		}
		return strOp;
	}

}
