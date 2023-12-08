package de.rwth.processmining.tb.core.emd.solutiondata;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Triple;

public class NonZeroFlows implements Iterable<Triple<Integer, Integer, Double>>{
	
	public static class Builder {
		
		private ArrayList<Integer> lIndSrc;

		private ArrayList<Integer> lIndTar;

		private ArrayList<Double> lFlow;
		
		public Builder() {
			lIndSrc = new ArrayList<>();
			lIndTar = new ArrayList<>();
			lFlow = new ArrayList<>();
		}
		
		public Builder addNonZeroFlow(int indSrc, int indTar, double flow) {
			lIndSrc.add(indSrc);
			lIndTar.add(indTar);
			lFlow.add(flow);
			return this;
		}
		
		public NonZeroFlows build() {
			return new NonZeroFlows(lIndSrc.stream().mapToInt(i -> i).toArray(), 
					lIndTar.stream().mapToInt(i -> i).toArray(), lFlow.stream().mapToDouble(d -> d).toArray());
		}
		
		
		public NonZeroFlows buildfromSolution(int lenL, int lenR, double[] p_sol) {
			double f;
			for (int l = 0; l < lenL; l++) {
				for (int r = 0; r < lenR; r++) {
					f = p_sol[l * lenR + r];
					if(Double.compare(f,  0) > 0) {
						this.addNonZeroFlow(l, r, f);
					}
				}
			}
			return this.build();
		}
	}
	
	/**
	 * 
	 */
	private final int[] indSrc;
	
	private final int[] indTar;
	
	private final double[] flow;
	
	private NonZeroFlows(int[] indSrc, int[] indTar, double[] flow) {
		this.indSrc = indSrc;
		this.indTar = indTar;
		this.flow = flow;
	}

	@Override
	public Iterator<Triple<Integer, Integer, Double>> iterator() {
		Iterator<Triple<Integer, Integer, Double>> it = new Iterator<Triple<Integer,Integer,Double>>() {
			
			private int cur = 0;

			@Override
			public boolean hasNext() {
				return cur < indSrc.length;
			}

			@Override
			public Triple<Integer, Integer, Double> next() {
				Triple<Integer, Integer, Double> res = Triple.of(indSrc[cur], indTar[cur], flow[cur]);
				cur++;
				return res;
			}

		};
		return it;
	}	
	
	
//	public JSONArray toJSON() {
//		JSONArray ja = new JSONArray();
//		
//		for(Triple<Integer, Integer, Double> t : this) {
//			JSONObject jo = new JSONObject();
//			jo.put("Source", t.getLeft());
//			jo.put("Target", t.getMiddle());
//			jo.put("Flow", t.getRight());
//			ja.put(jo);
//		}
//		
//		return ja;
//		
//	}
	
	
	public int getCountNonZeroFlows() {
		return indSrc.length;
	}
	
	
	

}
