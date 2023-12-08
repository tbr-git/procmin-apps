package de.rwth.processmining.tb.core.emd.language;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.ProMCanceller;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.map.TObjectFloatMap;

public class StochasticLanguageImpl<T extends TraceDescriptor> implements StochasticLanguage<T> {

	private final static Logger logger = LogManager.getLogger( StochasticLanguageImpl.class );

	/**
	 * Mapping from trace to its total frequency
	 */
	protected TObjectFloatMap<T> sLog;
	
	/**
	 * Total weight over all traces
	 */
	protected double totalWeight;
	
	
	/**
	 * Absolute number of traces on which this stochastic language is derived
	 */
	protected int absoluteNbrTraces;
	
	/**
	 * Constructor
	 * 
	 * @param sLog Mapping from traces to total frequency
	 * @param totalWeight Total weight over all traces
	 */
	protected StochasticLanguageImpl(TObjectFloatMap<T> sLog, double totalWeight, int absoluteNbrOfTraces) {
		this.sLog = sLog;
		this.totalWeight = totalWeight;
		this.absoluteNbrTraces = absoluteNbrOfTraces;
	}
	
	
	

	/**
	 * Implementation based on {@link org.processmining.earthmoversstochasticconformancechecking.algorithms.XLog2StochasticLanguage#convert(XLog, XEventClassifier, ProMCanceller)}.
	 */
	@Override
	public StochasticLanguageIterator<T> iterator() {
		double normalizationWeight = this.totalWeight;
		TObjectFloatIterator<T> it = sLog.iterator();
		return new StochasticLanguageIterator<T>() {

			public T next() {
				it.advance();
				return it.key();
			}

			public boolean hasNext() {
				return it.hasNext();
			}

			public double getProbability() {
				return it.value() / normalizationWeight;
			}
		};
	}

	@Override
	public int getNumberOfTraceVariants() {
		return sLog.size();
	}

	@Override
	public String toString() {
		String strSLog = "";
		//accessing keys/values through an iterator:
		for (StochasticLanguageIterator<T> it = iterator(); it.hasNext(); ) {
			strSLog += it.next().toString();
			strSLog += ": ";
			strSLog += String.valueOf(it.getProbability());
			strSLog += "\n";
		}
		return "FreqBasedStochasticLanguage [sLog=" + strSLog + ", totalWeight=" + totalWeight + "]";
	}
	
//	@Override
//	public JSONObject toJson() {
//		JSONObject jo = new JSONObject();
//		JSONArray ja = new JSONArray();
//		for (StochasticLanguageIterator<T> it = this.iterator(); it.hasNext(); ) {
////		for (TObjectFloatIterator<TraceDescriptor> it = sLog.iterator(); it.hasNext(); ) {
//			TraceDescriptor desc = it.next();
//			JSONObject tmp = new JSONObject();
//			tmp.put("Variant", desc.toJson());
//			tmp.put("Count", sLog.get(desc));
//			tmp.put("Probability", it.getProbability());
//			ja.put(tmp);
//		}
//		jo.put("Traces", ja);
//		return jo;
//	}
	
	@Override
	public double getTotalWeight() {
		return this.totalWeight;
	}
	
	@Override
	public double getProbability(TraceDescriptor traceDesc) {
		if(sLog.containsKey(traceDesc)) {
			return sLog.get(traceDesc) / this.totalWeight;
			
		}
		else {
			return 0;
		}
	}
	
	@Override
	public boolean contains(TraceDescriptor traceDescriptor) {
		return sLog.containsKey(traceDescriptor);
	}


	@Override
	public int getAbsoluteNbrOfTraces() {
		return absoluteNbrTraces;
	}
	
	

}
