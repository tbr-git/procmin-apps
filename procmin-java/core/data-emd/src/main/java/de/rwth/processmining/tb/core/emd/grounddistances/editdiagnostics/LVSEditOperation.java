package de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class LVSEditOperation {
	protected final LVSOpNames operation;
	
	protected final int indexL;
	
	protected final int indexR;

	public LVSEditOperation(LVSOpNames operation, int indexL, int indexR) {
		super();
		this.operation = operation;
		this.indexL = indexL;
		this.indexR = indexR;
	}

	public LVSOpNames getOperation() {
		return operation;
	}

	public int getIndexL() {
		return indexL;
	}

	public int getIndexR() {
		return indexR;
	}
	
//	public JSONObject getJSON() {
//		JSONObject jo = new JSONObject();
//		jo.put("IndexL", this.indexL);
//		jo.put("IndexR", this.indexR);
//		jo.put("EditOp", operation.toString());
//		return jo;
//		
//	}
	
	public String getDescription(TraceDescriptor traceL, TraceDescriptor traceR) {
		String strOp;
		switch(operation) {
			case MATCH:
				strOp = LVSOpNames.MATCH.toString() + "(" + traceL.toString(indexL) + 
					"-" + traceR.toString(indexR) + ")";	
			case INSERT:
				strOp = LVSOpNames.INSERT.toString() + "(" + traceR.toString(indexR) + ")";	
				break;
			case DELETE:
				strOp = LVSOpNames.DELETE.toString() + "(" + traceL.toString(indexL) + ")";	
				break;
			case RENAME:
				strOp = LVSOpNames.RENAME.toString() + "(" + traceL.toString(indexL) + 
					"-" + traceR.toString(indexR) + ")";	
				break;
			default:
				strOp = "";
		}
		return strOp;
	}
	
}
