package de.rwth.processmining.tb.core.emd.grounddistances;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class TraceDistFreeTraceDelInsWrapper<F extends TraceDescriptor> implements TraceDescDistCalculator<F> {
	
	private final TraceDescDistCalculator<F> traceDist;

	private boolean freeLeftTraceDelete;
	
	private boolean freeRightTraceInsert;

	public TraceDistFreeTraceDelInsWrapper(TraceDescDistCalculator<F> traceDist) {
		super();
		freeLeftTraceDelete = false;
		freeRightTraceInsert = false;
		this.traceDist = traceDist;
	}
	
	public void setFreeLeftTraceDelete(boolean freeTraceDelete) {
		freeLeftTraceDelete = freeTraceDelete;
	}

	public void setFreeRightTraceInsert(boolean freeTraceInsert) {
		freeRightTraceInsert = freeTraceInsert;
	}

	@Override
	public double get_distance(F t1, F t2) {
		int m = t1.length();
		int n = t2.length();
		if (n == 0 && m == 0) {
			return 0;
		}
		
		// Free delete left and right trace empty
		if (freeLeftTraceDelete && n == 0) {
			return 0;
		}
		// Free insert right and left trace empty
		if (freeRightTraceInsert && m == 0) {
			return 0;
		}
		
		return this.traceDist.get_distance(t1, t2);
	}

	@Override
	public String getShortDescription() {
		return "Wrapped " + this.traceDist.getShortDescription();
	}

}
