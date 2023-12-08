package de.rwth.processmining.tb.core.emd.language;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.ProMCanceller;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.XLogTraceFeatureExtractor;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.custom_hash.TObjectFloatCustomHashMap;

public class FreqBasedStochasticLanguageImpl extends StochasticLanguageImpl implements SlidingStochasticLanguage {

	/**
	 * Factory that generates trace descriptors and provides a HashingStrategy for these
	 */
	protected XLogTraceFeatureExtractor traceDescFac;

	private final static Logger logger = LogManager.getLogger( FreqBasedStochasticLanguageImpl.class );
	
	/**
	 * Constructor
	 * 
	 * @param sLog Mapping from traces to total frequency
	 * @param totalWeight Total weight over all traces
	 */
	protected FreqBasedStochasticLanguageImpl(TObjectFloatMap<TraceDescriptor> sLog, double totalWeight, 
	    int nbrTraces, XLogTraceFeatureExtractor traceDescFac) {
		super(sLog, totalWeight, nbrTraces);
		this.traceDescFac = traceDescFac;
	}
	
	/**
	 * Factory method based on {@link org.processmining.earthmoversstochasticconformancechecking.algorithms.XLog2StochasticLanguage#convert(XLog, XEventClassifier, ProMCanceller)}.
	 * @param log Log that is converted to a stochastic language
	 * @param canceller ProM Canceller
	 * @param traceDescFac Factory that generates trace descriptors and provides a HashingStrategy for these
	 * @return Instance of {@link FreqBasedStochasticLanguage}
	 */
	public static SlidingStochasticLanguage convert(Iterator<XTrace> log, ProMCanceller canceller, 
			XLogTraceFeatureExtractor traceDescFac) {
		TObjectFloatMap<TraceDescriptor> sLog = new TObjectFloatCustomHashMap<>(traceDescFac.getHashingStrat(), 10, 0.5f, 0);
		SlidingStochasticLanguage language = new FreqBasedStochasticLanguageImpl(sLog, 0.0, 0, traceDescFac);
		language.slideIn(log);
		
		return language;
	}
	

	@Override
	public void slideOut(XTrace trace) {
		TraceDescriptor key = traceDescFac.getTraceDescriptor(trace);
		if(sLog.adjustValue(key, -1)) {
			this.totalWeight--;
			this.absoluteNbrTraces--;
			if(sLog.get(key) == 0) {
				sLog.remove(key);
			}
		}
		else {
			throw new RuntimeException("Sliding out what is not inside!");
		}
	}

	@Override
	public void slideIn(XTrace trace) {
		TraceDescriptor key = traceDescFac.getTraceDescriptor(trace);
		sLog.adjustOrPutValue(key, 1, 1);
		this.totalWeight += 1;
		this.absoluteNbrTraces++;
	}

}
