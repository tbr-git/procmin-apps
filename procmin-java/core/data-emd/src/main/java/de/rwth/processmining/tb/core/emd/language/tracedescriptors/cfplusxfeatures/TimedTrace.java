package de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures;

import java.util.Arrays;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class TimedTrace extends TraceDescriptor {

	private String[] sTrace;
	private double[] dTimes;
	private static final int hashPrime = 31;


	public TimedTrace(String[] sTrace, double[] dTimes) {
		this.sTrace = sTrace;
		this.dTimes = dTimes;
	}
	
	
	public int hashCodeTrace() {
		int result = 1;
		result = hashPrime * result + Arrays.hashCode(dTimes);
		result = hashPrime * result + Arrays.hashCode(sTrace);
		return result;
	}

	@Override
	public int hashCode() {
		int result = hashCodeTrace();
		result = hashPrime * result + Arrays.hashCode(dTimes);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimedTrace other = (TimedTrace) obj;
		if (!Arrays.equals(dTimes, other.dTimes))
			return false;
		if (!Arrays.equals(sTrace, other.sTrace))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "TimedTrace [sTrace=" + Arrays.toString(sTrace) + ", dTimes=" + Arrays.toString(dTimes) + "]";
	}
	
	public String toString(int index) {
		return sTrace[index] + "(" + dTimes[index] + ")";
	}


	public int getTraceLength() {
		return sTrace.length;
	}

	public double[] getTimes() {
		return dTimes;
	}

	public String[] getTraceLabels() {
		return sTrace;
	}
	
//	@Override
//	public JSONObject toJson() {
//		JSONObject jo = new JSONObject();
//		JSONArray ja = new JSONArray(sTrace);
//		jo.put("Trace", ja);
//		return jo;
//	}

	@Override
	public int length() {
		return sTrace.length;
	}
	

}
