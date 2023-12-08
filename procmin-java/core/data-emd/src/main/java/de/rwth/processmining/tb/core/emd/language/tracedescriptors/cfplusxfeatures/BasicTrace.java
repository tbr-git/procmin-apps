package de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures;

import java.util.Arrays;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class BasicTrace extends TraceDescriptor{
	
	private final String[] sTrace;

	public BasicTrace(String[] sTrace) {
		this.sTrace = sTrace;
	}
	
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(sTrace);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicTrace other = (BasicTrace) obj;
		if (!Arrays.equals(sTrace, other.sTrace))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "BasicTrace [sTrace=" + Arrays.toString(sTrace) + "]";
	}
	
	@Override
	public String toString(int index) {
		return sTrace[index];
	}

	public String[] getsTrace() {
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
