package de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures;

import java.util.Arrays;

import de.rwth.processmining.tb.core.data.variantlog.abstraction.CVariantAbst;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;


public class AbstTraceCC extends TraceDescriptor {
	
	/**
	 * Pointer to the trace (using number proxy).
	 * <p>
	 * Direct pointer into the event log and, therefore, this array
	 * <b>must not</b> be manipulated.
	 */
	private final int[] trace;
	
	/**
	 * Pointer to the abstraction.
	 * <p>
	 * Direct pointer into the event log and, therefore, this array
	 * <b>must not</b> be manipulated.
	 * abstraction[i][0] is supposed to contain the number of applied abstractions
	 * abstraction[i][1...] should contain the abstractions in ascending order
	 */
	private final int[][] abstraction;
	
	/**
	 * Reference to the category mapper used for this trace.
	 */
	private final CategoryMapper categoryMapper;
	
	public AbstTraceCC(int[] trace, int[][] abstraction, CategoryMapper categoryMapper) {
		this.trace = trace;
		this.abstraction = abstraction;
		this.categoryMapper = categoryMapper;
	}

	public AbstTraceCC(CVariantAbst variant, CategoryMapper categoryMapper) {
		this.trace = variant.getTraceCategories();
		this.abstraction = variant.getAbstractions();
		this.categoryMapper = categoryMapper;
	}

	
	

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * Arrays.hashCode(trace);
		// The abstraction array contains many 0s.
		// I don't want to shift too much of the original trace out by multiplication
		int resultTmp = 1;
		for(int i = 0; i < abstraction.length; i++) {
			if(abstraction[i] != null) {
				resultTmp = 31 * resultTmp +  Arrays.hashCode(abstraction[i]);
			}
		}
		result = 31 * result + resultTmp; 
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
		AbstTraceCC other = (AbstTraceCC) obj;
		if (!Arrays.equals(trace, other.trace))
			return false;
		// Compare abstractions
		// Assume that trace == other.trace implies that length 
		// of abstraction will match
		for(int i = 0; i < trace.length; i++) {
			// I don't use Arrays.equal since this might not consider
			// the buffers properly 
			for(int j = 0; j < abstraction[i][0]; j++) {
				// As 0th element contains length 
				// this should not cause out of bounds exceptions
				if(abstraction[i][j] != other.abstraction[i][j]) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ECC basic trace with abstractions");
		builder.append("[trace="); 
		builder.append(Arrays.toString(trace));
		builder.append("\nabstractions:\n"); 
		for(int i = 0; i < abstraction.length; i++) {
			builder.append(i);
			builder.append(": ");
			builder.append(Arrays.toString(abstraction[i]));
			builder.append("\n");
		}
		builder.append("]");
		return builder.toString();
	}
	
//	@Override
//	public JSONObject toJson() {
//		// Trace
//		JSONObject jo = new JSONObject();
//		JSONArray jaTrace = new JSONArray(Arrays.stream(this.trace).mapToObj(
//				i -> this.categoryMapper.getActivity4Category(i)).toArray(String[]::new));
//		// Abstraction array
//		JSONArray jaAbstractions = new JSONArray();
//		for(int i = 0; i < abstraction.length; i++) {
//			JSONObject  joAbst = new JSONObject();
//			int[] a = abstraction[i];
//			// Nbr of abstractions
//			joAbst.put("size", a[0]);
//			// Add list of abstractions
//			JSONArray jaAbst = new JSONArray();
//			for(int j = 1; j <= a[0]; j++) {
//				jaAbst.put(a[j]);
//			}
//			joAbst.put("eventAbstractions", jaAbst);
//			jaAbstractions.put(joAbst);
//		}
//		
//		jo.put("trace", jaTrace);
//		jo.put("abstractions", jaAbstractions);
//
//		return jo;
//	}

	@Override
	public String toString(int index) {
		StringBuilder builder = new StringBuilder();
		// Activity name
		builder.append(categoryMapper.getActivity4Category(trace[index]));
		// Add abstractions
		if (abstraction[index][0] > 0) {
			int[] activityAbstractions = abstraction[index];
			builder.append(" (");
			// All but last
			for (int i = 0; i < activityAbstractions[0] - 1; i++) {
				builder.append(activityAbstractions[i + 1]);
				// List delimiter
				builder.append(", ");
			}
			// Last abstraction
			builder.append(activityAbstractions[activityAbstractions[0]]);
			// No list delimiter but close list
			builder.append(")");
		}
		return builder.toString();
	}
	
	public int[] getTrace() {
		return trace;
	}
	
	public int[][] getAbstractions() {
		return abstraction;
	}
	
	/**
	 * Get the abstractions for a certain event index.
	 * <p>
	 * This method assumes that the event index is valid.
	 * Since it is called frequently, it does <b>not</b> check the boundaries.
	 * @param eventIndex
	 * @return Abstractions for the event at eventIndex
	 */
	public int[] getAbstractionsAt(int eventIndex) {
		return abstraction[eventIndex];
	}
	
	/**
	 * Get the letter at a certain event index.
	 * <p>
	 * This method assumes that the event index is valid.
	 * Since it is called frequently, it does <b>not</b> check the boundaries.
	 * @param eventIndex
	 * @return Letter of the event at eventIndex
	 */
	public int getLetterAt(int eventIndex) {
		return trace[eventIndex];
	}
	
	public int length() {
		return this.trace.length;
	}

}
