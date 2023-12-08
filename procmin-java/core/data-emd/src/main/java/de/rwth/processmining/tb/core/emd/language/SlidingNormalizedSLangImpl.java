package de.rwth.processmining.tb.core.emd.language;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.ProMCanceller;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

public class SlidingNormalizedSLangImpl <T extends TraceDescriptor> implements StochasticLanguage<T>, 
    SlidingStochasticLanguage<T> {

	private final static Logger logger = LogManager.getLogger( SlidingNormalizedSLangImpl.class );

	/**
	 * Mapping from trace to its total number
	 */
	protected TObjectIntHashMap<T> sLog;
	
	/**
	 * Absolute number of traces on which this stochastic language is derived
	 */
	protected int absoluteNbrTraces;
	
	/**
	 * Transform a trace into a descriptor
	 */
	private final XTraceToDescriptor<T> traceDigester;
	
	/**
	 * Constructor
	 * 
	 * @param catMapper Category mapper (form string -> cat code)
	 * @param classifier Classifier that fits to the provided mapper
	 */
	protected SlidingNormalizedSLangImpl(XTraceToDescriptor<T> traceDigester) {
		this.sLog = new TObjectIntHashMap<>();
		this.absoluteNbrTraces = 0;
		this.traceDigester = traceDigester;
	}
	
	/**
	 * Implementation based on {@link org.processmining.earthmoversstochasticconformancechecking.algorithms.XLog2StochasticLanguage#convert(XLog, XEventClassifier, ProMCanceller)}.
	 */
	@Override
	public StochasticLanguageIterator<T> iterator() {
		TObjectIntIterator<T> it = sLog.iterator();
		return new StochasticLanguageIterator<T>() {

			public T next() {
				it.advance();
				return it.key();
			}

			public boolean hasNext() {
				return it.hasNext();
			}

			public double getProbability() {
				return ((double) it.value()) / absoluteNbrTraces;
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
		return "FreqBasedStochasticLanguage [sLog=" + strSLog + ", numberTraces=" + absoluteNbrTraces + "]";
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
		return this.absoluteNbrTraces;
	}
	
	@Override
	public double getProbability(TraceDescriptor traceDesc) {
		if(sLog.containsKey(traceDesc)) {
			return ((double) sLog.get(traceDesc)) / this.absoluteNbrTraces;
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

  @Override
  public void slideOut(XTrace trace) {
    T f = this.traceDigester.apply(trace);
    this.sLog.adjustValue(f, -1);
    absoluteNbrTraces--;
  }

  @Override
  public void slideIn(XTrace trace) {
    T f = this.traceDigester.apply(trace);
    this.sLog.adjustValue(f, 1);
    absoluteNbrTraces++;
  }
	
	

}
