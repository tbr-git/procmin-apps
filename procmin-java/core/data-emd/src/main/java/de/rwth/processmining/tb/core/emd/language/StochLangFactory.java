package de.rwth.processmining.tb.core.emd.language;

import java.util.Iterator;

import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.ProMCanceller;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased.VariantBasedFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.XLogTraceFeatureExtractor;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.custom_hash.TObjectFloatCustomHashMap;

public class StochLangFactory {

	public static<F extends TraceDescriptor> OrderedStochasticLanguage<F> xLogConvertOrdered(
	    Iterator<XTrace> log, ProMCanceller canceller, 
			XLogTraceFeatureExtractor<F> traceFeatureExtractor) {
    TObjectFloatMap<F> sLog = new TObjectFloatCustomHashMap<>(traceFeatureExtractor.getHashingStrat(), 10, 0.5f, 0);
		int nbrTraces = 0;
		while(log.hasNext()) {
			traceFeatureExtractor.addTraceDescriptor(log.next(), sLog);
			nbrTraces++;

			if (canceller != null && canceller.isCancelled()) {
				return null;
			}
		}

		OrderedStochasticLanguage<F> language = new OrderedFreqBasedStochLanguageImpl<>(
		    sLog, nbrTraces, nbrTraces);
		
		return language;
	}	

	public static<V extends CVariant, F extends TraceDescriptor> OrderedStochasticLanguage<F> variantLogConvertOrdered(
	    CVariantLog<? extends V> log, ProMCanceller canceller, VariantBasedFeatureExtractor<V, F> traceDescFac) {
		TObjectFloatMap<F> sLog = new TObjectFloatCustomHashMap<>(traceDescFac.getHashingStrat(), 10, 0.5f, 0);
		int nbrTraces = 0;
		for(V variant : log) {
			traceDescFac.addTraceDescriptor(variant, log, sLog);

			if (canceller != null && canceller.isCancelled()) {
				return null;
			}

			nbrTraces += variant.getSupport();
		}

		OrderedStochasticLanguage<F> language = new OrderedFreqBasedStochLanguageImpl<>(sLog, nbrTraces, nbrTraces);
		
		return language;
	}	

}
