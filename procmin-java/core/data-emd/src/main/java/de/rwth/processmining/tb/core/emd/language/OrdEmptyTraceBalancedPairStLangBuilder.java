package de.rwth.processmining.tb.core.emd.language;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.XLogTraceFeatureExtractor;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.custom_hash.TObjectFloatCustomHashMap;

public class OrdEmptyTraceBalancedPairStLangBuilder {
	private final static Logger logger = LogManager.getLogger( OrdEmptyTraceBalancedPairStLangBuilder.class.getName() );

	private Collection<XTrace> tracesL;

	private Collection<XTrace> tracesR;
	
	private double preScaleLeft;

	private double preScaleRight;
	
	private XLogTraceFeatureExtractor traceDescFac;
	
	public OrdEmptyTraceBalancedPairStLangBuilder() {
		tracesL = null;
		tracesR = null;
		traceDescFac = null;
		preScaleLeft = -1;
		preScaleRight = -1;
	}
	
	public OrdEmptyTraceBalancedPairStLangBuilder setTracesLeft(Collection<XTrace> tracesLeft) {
		this.tracesL = tracesLeft;
		return this;
	}

	public OrdEmptyTraceBalancedPairStLangBuilder setTracesRight(Collection<XTrace> tracesRight) {
		this.tracesR = tracesRight;
		return this;
	}

	public OrdEmptyTraceBalancedPairStLangBuilder setPreScaleLeft(double preScaleLeft) {
		this.preScaleLeft = preScaleLeft;
		return this;
	}

	public OrdEmptyTraceBalancedPairStLangBuilder setPreScaleRight(double preScaleRight) {
		this.preScaleRight = preScaleRight;
		return this;
	}

	public OrdEmptyTraceBalancedPairStLangBuilder setTraceDesciptorFactory(XLogTraceFeatureExtractor traceDescFac) {
		this.traceDescFac = traceDescFac;
		return this;
	}
	
	public Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> build() {
		if(this.tracesL == null) {
			throw new RuntimeException("No left trace collection given");
		}
		else if(this.tracesR == null) {
			throw new RuntimeException("No right trace collection given");
		}
		else if(this.preScaleLeft <= 0) {
			throw new RuntimeException("No pre scaling left given");
		}
		else if(this.preScaleRight <= 0) {
			throw new RuntimeException("No pre scaling right given");
		}
		else if(this.traceDescFac == null) {
			throw new RuntimeException("No trace descriptor factory given");
		}
		
		TObjectFloatMap<TraceDescriptor> sLogL = new TObjectFloatCustomHashMap<>(traceDescFac.getHashingStrat(), 100, 0.5f, 0);
		double totalWeightL = 0;
		for(XTrace t : tracesL) {
			TraceDescriptor traceDesc = traceDescFac.getTraceDescriptor(t);

			sLogL.adjustOrPutValue(traceDesc, (float) preScaleLeft, (float) preScaleLeft);
			totalWeightL += preScaleLeft;
		}

		TObjectFloatMap<TraceDescriptor> sLogR = new TObjectFloatCustomHashMap<>(traceDescFac.getHashingStrat(), 100, 0.5f, 0);
		double totalWeightR = 0;
		for(XTrace t : tracesR) {
			TraceDescriptor traceDesc = traceDescFac.getTraceDescriptor(t);

			sLogR.adjustOrPutValue(traceDesc, (float) preScaleRight, (float) preScaleRight);
			totalWeightR += preScaleRight;
		}
		
		if(Double.compare(totalWeightL,  totalWeightR) < 0) {
			float diff = (float) (totalWeightR - totalWeightL);
			sLogL.adjustOrPutValue(traceDescFac.getEmptyTrace(), diff, diff);
			totalWeightL += diff;
		}
		else if(Double.compare(totalWeightL,  totalWeightR) > 0) {
			float diff = (float) (totalWeightL - totalWeightR);
			sLogR.adjustOrPutValue(traceDescFac.getEmptyTrace(), diff, diff);
			totalWeightR += diff;
		}
		
		
		OrderedStochasticLanguage languageL = new OrderedFreqBasedStochLanguageImpl(
				sLogL, totalWeightL, tracesL.size());

		OrderedStochasticLanguage languageR = new OrderedFreqBasedStochLanguageImpl(
				sLogR, totalWeightR, tracesR.size());
		
		return Pair.of(languageL, languageR);
	}

}
