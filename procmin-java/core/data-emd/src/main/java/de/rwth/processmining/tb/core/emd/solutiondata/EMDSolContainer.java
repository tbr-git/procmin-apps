package de.rwth.processmining.tb.core.emd.solutiondata;

import org.apache.logging.log4j.message.Message;

import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class EMDSolContainer<F extends TraceDescriptor> implements Message {
	
	public static class Builder<F extends TraceDescriptor> {
		private double emd;
		
		private NonZeroFlows nonZeroFlows;

		private double[][] cMat;
		
		private OrderedStochasticLanguage<F> Ll;
		
		private OrderedStochasticLanguage<F> Lr;
		
		public Builder() {
			emd = -1;
			nonZeroFlows = null;
			cMat = null;
			Ll = null;
			Lr = null;
		}
		
		public Builder<F> addEMD(double emd) {
			this.emd = emd;
			return this;
		}

		public Builder<F> addNonZeroFlows(NonZeroFlows nonZeroFlows) {
			this.nonZeroFlows = nonZeroFlows;
			return this;
		}

		public Builder<F> addDistances(double[][] c) {
			this.cMat = c;
			return this;
		}

		public Builder<F> addLangLeft(OrderedStochasticLanguage<F> Ll) {
			this.Ll = Ll;
			return this;
		}
		
		public Builder<F> addLangRight(OrderedStochasticLanguage<F> Lr) {
			this.Lr = Lr;
			return this;
		}
		
		public EMDSolContainer<F> build() {
			if(emd > -0.5 && nonZeroFlows != null && cMat != null && Ll != null && Lr != null) {
				return new EMDSolContainer<F>(emd, cMat, nonZeroFlows, Ll, Lr); 
			}
			else
				return null;
		}
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6254243884344277387L;

	private final double emd;
	
	private final NonZeroFlows nonZeroFlows;

	private final double[][] cMat;
	
	private final OrderedStochasticLanguage<F> Ll;
	
	private final OrderedStochasticLanguage<F> Lr;
	
	public EMDSolContainer(double emd, double[][] cMat, NonZeroFlows nonZeroFlows, 
			OrderedStochasticLanguage<F> Ll, OrderedStochasticLanguage<F> Lr) {
		this.emd = emd;
		this.nonZeroFlows = nonZeroFlows;
		this.Ll = Ll;
		this.Lr = Lr;
		this.cMat = cMat;
	}

	@Override
	public String getFormattedMessage() {
		//JSONObject jo = getJSON();
	  //TODO
	  return "";
	}
//	
//	public JSONObject getJSON() {
//		JSONObject jo = new JSONObject();
//		jo.put("EMD", this.emd);
//		jo.put("Language left", Ll.toJson());
//		jo.put("Language right", Lr.toJson());
//		jo.put("Nonzero flows", nonZeroFlows.toJSON());
//		jo.put("Distance", cMat);
//		return jo;
//		
//	}

	@Override
	public String getFormat() {
		return "";
	}

	@Override
	public Object[] getParameters() {
		return new Object[]{emd, nonZeroFlows, Ll, Lr};
	}

	@Override
	public Throwable getThrowable() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public NonZeroFlows getNonZeroFlows() {
		return nonZeroFlows;
	}
	
	public OrderedStochasticLanguage<F> getLanguageLeft() {
		return Ll;
	}

	public OrderedStochasticLanguage<F> getLanguageRight() {
		return Lr;
	}
	
	public double getCost(int iSrc, int iTar) {
		return cMat[iSrc][iTar];
	}
	
	public double getEMD() {
		return emd;
	}
		
}
