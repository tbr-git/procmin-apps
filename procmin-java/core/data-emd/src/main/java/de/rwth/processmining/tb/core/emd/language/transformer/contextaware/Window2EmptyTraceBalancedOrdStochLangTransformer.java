package de.rwth.processmining.tb.core.emd.language.transformer.contextaware;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.emd.language.OrderedFreqBasedStochLanguageImpl;
import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased.VariantBasedFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.XLogTraceFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.transformer.ProbMassNonEmptyTrace;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.custom_hash.TObjectFloatCustomHashMap;

public class Window2EmptyTraceBalancedOrdStochLangTransformer implements Window2OrderedStochLangTransformer {
	private final static Logger logger = LogManager.getLogger( Window2EmptyTraceBalancedOrdStochLangTransformer.class );
	
	public class Builder {
		private double preScaleLeft;
	
		private double preScaleRight;
		
		private ScalingContext scalingContext;
		
		public Builder() {
			preScaleLeft = 1;
			preScaleRight = 1;
			
		}

		public Builder(Window2EmptyTraceBalancedOrdStochLangTransformer t) {
			preScaleLeft = t.getPreScaleLeft();
			preScaleRight = t.getPreScaleRight();
			
		}
		
		public Builder setPreScaleLeft(double preScaleLeft) {
			this.preScaleLeft = preScaleLeft;
			return this;
		}

		public Builder setPreScaleRight(double preScaleRight) {
			this.preScaleRight = preScaleRight;
			return this;
		}
		
		public Builder setScalingContext(ScalingContext scalingContext) {
			this.scalingContext = scalingContext;
			return this;
		}
		
		public Window2EmptyTraceBalancedOrdStochLangTransformer build() {
			return new Window2EmptyTraceBalancedOrdStochLangTransformer(preScaleLeft, preScaleRight, scalingContext); 
		}
		
	}
	
	private double preScaleLeft;
	
	private double preScaleRight;
	
	private ScalingContext scalingContext;
	
	public Window2EmptyTraceBalancedOrdStochLangTransformer(double preScaleLeft, double preScaleRight, ScalingContext scalingContext) {
		this.preScaleLeft = preScaleLeft;
		this.preScaleRight = preScaleRight;
		this.scalingContext = scalingContext;
	}

	public Window2EmptyTraceBalancedOrdStochLangTransformer(Window2EmptyTraceBalancedOrdStochLangTransformer transformer) {
		this.preScaleLeft = transformer.getPreScaleLeft();
		this.preScaleRight = transformer.getPreScaleRight();
	}
	

	@Override
	public<F extends TraceDescriptor> Pair<OrderedStochasticLanguage<F>, OrderedStochasticLanguage<F>> 
	  transformWindow(Iterator<XTrace> itTracesLeft, Iterator<XTrace> itTracesRight, 
			XLogTraceFeatureExtractor<F> featureExtractor) {
		TObjectFloatMap<F> sLogL = new TObjectFloatCustomHashMap<>(featureExtractor.getHashingStrat(), 10, 0.5f, 0);
		double totalWeightL = 0;
		int nbrTracesL = 0;
		while(itTracesLeft.hasNext()) {
			featureExtractor.addTraceDescriptor(itTracesLeft.next(), sLogL, (float) preScaleLeft);
			totalWeightL += preScaleLeft;
			nbrTracesL++;
		}

		TObjectFloatMap<F> sLogR = new TObjectFloatCustomHashMap<>(featureExtractor.getHashingStrat(), 10, 0.5f, 0);
		double totalWeightR = 0;
		int nbrTracesR = 0;
		while(itTracesRight.hasNext()) {
			featureExtractor.addTraceDescriptor(itTracesRight.next(), sLogR, (float) preScaleRight);
			totalWeightR += preScaleRight;
			nbrTracesR++;
		}
		
		if(Double.compare(totalWeightL,  totalWeightR) < 0) {
			float diff = (float) (totalWeightR - totalWeightL);
			sLogL.adjustOrPutValue(featureExtractor.getEmptyTrace(), diff, diff);
			totalWeightL += diff;
		}
		else if(Double.compare(totalWeightL,  totalWeightR) > 0) {
			float diff = (float) (totalWeightL - totalWeightR);
			sLogR.adjustOrPutValue(featureExtractor.getEmptyTrace(), diff, diff);
			totalWeightR += diff;
		}
		
		
		OrderedStochasticLanguage<F> languageL = new OrderedFreqBasedStochLanguageImpl<>(
				sLogL, totalWeightL, nbrTracesL);

		OrderedStochasticLanguage<F> languageR = new OrderedFreqBasedStochLanguageImpl<>(
				sLogR, totalWeightR, nbrTracesR);
		
		return Pair.of(languageL, languageR);
	}
	
	@Override
	public<V extends CVariant, F extends TraceDescriptor> Pair<OrderedStochasticLanguage<F>, OrderedStochasticLanguage<F>> 
	  transformWindow(CVariantLog<? extends V> logLeft, CVariantLog<? extends V> logRight, 
	      VariantBasedFeatureExtractor<V, F> featureExtractor) {

		TObjectFloatMap<F> sLogL = 
		    new TObjectFloatCustomHashMap<>(featureExtractor.getHashingStrat(), 100, 0.5f, 0);
		double totalWeightL = 0;
		int nbrTracesL = 0;
		for(V variant : logLeft) {
			float addedWeight = featureExtractor.addTraceDescriptor(variant, logLeft, sLogL, (float) preScaleLeft);

			totalWeightL += addedWeight;
			nbrTracesL += variant.getSupport();
		}

		TObjectFloatMap<F> sLogR = 
		    new TObjectFloatCustomHashMap<>(featureExtractor.getHashingStrat(), 100, 0.5f, 0);
		double totalWeightR = 0;
		int nbrTracesR = 0;
		for(V variant : logRight) {
			float addedWeight = featureExtractor.addTraceDescriptor(variant, logRight, sLogR, (float) preScaleRight);

			totalWeightR += addedWeight;
			nbrTracesR += variant.getSupport();
		}
		
		if(Double.compare(totalWeightL,  totalWeightR) < 0) {
			float diff = (float) (totalWeightR - totalWeightL);
			sLogL.adjustOrPutValue(featureExtractor.getEmptyCVariant(logLeft), diff, diff);
			totalWeightL += diff;
		}
		else if(Double.compare(totalWeightL,  totalWeightR) > 0) {
			float diff = (float) (totalWeightL - totalWeightR);
			sLogR.adjustOrPutValue(featureExtractor.getEmptyCVariant(logRight), diff, diff);
			totalWeightR += diff;
		}
		
		
		OrderedStochasticLanguage<F> languageL = new OrderedFreqBasedStochLanguageImpl<>(
				sLogL, totalWeightL, nbrTracesL);

		OrderedStochasticLanguage<F> languageR = new OrderedFreqBasedStochLanguageImpl<>(
				sLogR, totalWeightR, nbrTracesR);
		
		return Pair.of(languageL, languageR);
	}
	
	@Override
	public ProbMassNonEmptyTrace probabilityMassNonEmptyTraces(CVariantLog<? extends CVariant> tracesLeft,
			CVariantLog<? extends CVariant> tracesRight) {
		double probNonEmptyLeft = ((double) tracesLeft.sizeLog()) / this.preScaleLeft;
		double probNonEmptyRight = ((double) tracesRight.sizeLog()) / this.preScaleRight;
		
		probNonEmptyLeft = probNonEmptyLeft / Math.max(probNonEmptyLeft, probNonEmptyRight);
		probNonEmptyRight = probNonEmptyRight / Math.max(probNonEmptyLeft, probNonEmptyRight);
		// TODO Auto-generated method stub

		return new ProbMassNonEmptyTrace(probNonEmptyLeft, probNonEmptyRight, 
				((tracesLeft.sizeLog() == 0) && (tracesRight.sizeLog() == 0)));
	}
	
	public double getPreScaleLeft() {
		return preScaleLeft;
	}

	public double getPreScaleRight() {
		return preScaleRight;
	}
	
	public ScalingContext getScalingContext() {
		return scalingContext;
	}
	
	public void setPreScaleLeft(double preScaleLeft) {
		this.preScaleLeft = preScaleLeft;
	}
	
	public void setPreScaleRight(double preScaleRight) {
		this.preScaleRight = preScaleRight;
	}

	@Override
	public String getShortDescription() {
		return "Balanced by empty traces (" + scalingContext + ")";
	}

	
}
