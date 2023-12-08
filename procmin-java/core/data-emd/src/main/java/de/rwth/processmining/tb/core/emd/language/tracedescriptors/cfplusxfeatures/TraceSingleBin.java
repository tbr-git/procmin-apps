package de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures;

import java.util.Arrays;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class TraceSingleBin extends TraceDescriptor {
  
	private final String[] sTrace;

	private final int[] binInd;

	private static final int hashPrime = 17;

	public TraceSingleBin(String[] sTrace, int[] binInd) {
		this.sTrace = sTrace;
		this.binInd = binInd;
	}
	
	public int hashCodeTrace() {
		int result = 1;
		result = hashPrime * result + Arrays.hashCode(sTrace);
		return result;
	}

	@Override
	public int hashCode() {
		int result = hashCodeTrace();
		result = hashPrime * result + Arrays.hashCode(binInd);
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
		TraceDescBinnedActDur other = (TraceDescBinnedActDur) obj;
		if (!Arrays.equals(binInd, other.getBinInd()))
			return false;
		if (!Arrays.equals(sTrace, other.getsTrace()))
			return false;
		return true;
	}
	
	public int[] getBins() {
	  return this.binInd;
	}
	
	public int getBin(int i) {
	  return this.binInd[i];
	}


	@Override
	public String toString() {
		return "BinnedActDurTrace [sTrace=" + Arrays.toString(sTrace) + ", dTimes=" + Arrays.toString(binInd) + "]";
	}
	
	public String toString(int index) {
		return sTrace[index] + "(" + binInd[index] + ")";
	}


	public int getTraceLength() {
		return sTrace.length;
	}

	public String[] getTraceLabels() {
		return sTrace;
	}
	
//	@Override
//	public JSONObject toJson() {
//		JSONObject jo = new JSONObject();
//		JSONArray ja = new JSONArray();
//		for(int i = 0; i < sTrace.length; i++) {
//			JSONObject tmp = new JSONObject();
//			tmp.put("Activity Name", sTrace[i]);
//			tmp.put("Activity Duration", binInd[i]);
//			ja.put(tmp);
//		}
//		jo.put("Trace", ja);
//		return jo;
//	}


	@Override
	public int length() {
		return sTrace.length;
	}
}
