package de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures;

import java.util.Arrays;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class TraceDescBinnedActDur extends TraceDescriptor {

	private String[] sTrace;
	private int[] binInd;
	private static final int hashPrime = 17;


	public TraceDescBinnedActDur(String[] sTrace, int[] binInd) {
		this.sTrace = sTrace;
		this.binInd = binInd;
	}
	
	
	public int hashCodeTrace() {
		int result = 1;
		result = hashPrime * result + Arrays.hashCode(getsTrace());
		return result;
	}

	@Override
	public int hashCode() {
		int result = hashCodeTrace();
		result = hashPrime * result + Arrays.hashCode(getBinInd());
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
		if (!Arrays.equals(getBinInd(), other.getBinInd()))
			return false;
		if (!Arrays.equals(getsTrace(), other.getsTrace()))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "BinnedActDurTrace [sTrace=" + Arrays.toString(getsTrace()) + ", dTimes=" + Arrays.toString(getBinInd()) + "]";
	}
	
	public String toString(int index) {
		return getsTrace()[index] + "(" + getBinInd()[index] + ")";
	}


	public int getTraceLength() {
		return getsTrace().length;
	}

	public int[] getTimes() {
		return getBinInd();
	}

	public String[] getTraceLabels() {
		return getsTrace();
	}
	
//	@Override
//	public JSONObject toJson() {
//		JSONObject jo = new JSONObject();
//		JSONArray ja = new JSONArray();
//		for(int i = 0; i < getsTrace().length; i++) {
//			JSONObject tmp = new JSONObject();
//			tmp.put("Activity Name", getsTrace()[i]);
//			tmp.put("Activity Duration", getBinInd()[i]);
//			ja.put(tmp);
//		}
//		jo.put("Trace", ja);
//		return jo;
//	}


	@Override
	public int length() {
		return getsTrace().length;
	}


  public int[] getBinInd() {
    return binInd;
  }


  public String[] getsTrace() {
    return sTrace;
  }
}
