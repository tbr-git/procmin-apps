package de.rwth.processmining.tb.core.emd.language.transformer.contextaware;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XTrace;

import com.google.common.collect.Multiset;

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

/**
 * Class providing functionality to transform a collection of traces (or variants) 
 * into a stochastic language given a feature extractor (one feature per case). 
 * <p>
 * More precisely, is also considers another collection of traces that constitutes 
 * the context for interpreting probabilities. 
 * In this regard, we assume that the provided event log is a "subset" of the context event log 
 * and that the extracted feature (e.g., <b>service time</b> for this <b>subprocess</b> (and relation)),
 * only occurs in the provided log.
 * <p>
 * By padding with empty traces 
 * (<b>|provided log| + |empty features| = |context log|</b>), the stochastic language models 
 * the likelihood of a feature in the the context log.
 * 
 */
public class ContextAwareEmptyTraceBalancedTransformer implements Window2OrderedStochLangTransformer {
	private final static Logger logger = LogManager.getLogger(ContextAwareEmptyTraceBalancedTransformer.class);

	/**
	 * Builder 
	 */
	public class Builder {
		private int contextLogSizeLeft;
	
		private int contextLogSizeRight;
		
		private ScalingContext scalingContext;
		
		public Builder() {
			contextLogSizeLeft = 1;
			contextLogSizeRight = 1;
			
		}

		public Builder(ContextAwareEmptyTraceBalancedTransformer t) {
			contextLogSizeLeft = t.getContextLogSizeLeft();
			contextLogSizeRight = t.getContextLogSizeRight();
			scalingContext = t.getScalingContext();
		}
		
		public Builder setContextLogSizeLeft(int contextLogSizeLeft) {
			this.contextLogSizeLeft = contextLogSizeLeft;
			return this;
		}

		public Builder setContextLogSizeRight(int contextLogSizeRight) {
			this.contextLogSizeLeft = contextLogSizeRight;
			return this;
		}
		
		public Builder setScalingContext(ScalingContext scalingContext) {
			this.scalingContext = scalingContext;
			return this;
		}
		
		public ContextAwareEmptyTraceBalancedTransformer build() {
			return new ContextAwareEmptyTraceBalancedTransformer(contextLogSizeLeft, contextLogSizeRight, scalingContext); 
		}
		
	}

  /**
   * Size of the left context log based on which empty traces are added to
   * the left log during transformation.
   */
	private int contextLogSizeLeft;

  /**
   * Size of the right context log based on which empty traces are added to
   * the right log during transformation.
   */
	private int contextLogSizeRight;
	
	/**
	 * Description of the scaling that was applied.
	 */
	private ScalingContext scalingContext;
		
	/**
	 * Constructor
	 * @param contextLogSizeLeft
	 * @param contextLogSizeRight
	 * @param scalingContext
	 */
	public ContextAwareEmptyTraceBalancedTransformer(int contextLogSizeLeft, int contextLogSizeRight, 
	    ScalingContext scalingContext) {
		this.contextLogSizeLeft = contextLogSizeLeft;
		this.contextLogSizeRight = contextLogSizeRight;
		this.scalingContext = scalingContext;
	}

	/**
	 * Copy constructor
	 * @param transformer Instance to copy
	 */
	public ContextAwareEmptyTraceBalancedTransformer(ContextAwareEmptyTraceBalancedTransformer transformer) {
		this.contextLogSizeLeft = transformer.getContextLogSizeLeft();
		this.contextLogSizeRight = transformer.getContextLogSizeRight();
		this.scalingContext = transformer.getScalingContext();
	}
	
  ////////////////////////////////////////////////////////////////////////////////
	// XLog-based Extraction
  ////////////////////////////////////////////////////////////////////////////////
	@Override
	public<F extends TraceDescriptor> Pair<OrderedStochasticLanguage<F>, OrderedStochasticLanguage<F>> 
	  transformWindow(Iterator<XTrace> itTracesLeft, Iterator<XTrace> itTracesRight, 
			XLogTraceFeatureExtractor<F> featureExtractor) {
	  
    ////////////////////////////////////////
    // Feature Extraction
    ////////////////////////////////////////
    //////////////////////////////
    // Left
    //////////////////////////////
    // Features -> weight map
		TObjectFloatMap<F> sLogL = new TObjectFloatCustomHashMap<>(
		    featureExtractor.getHashingStrat(), 100, 0.5f, 0);
		int nbrTracesL = getPaddedFeatureMap(itTracesLeft, featureExtractor, sLogL, getContextLogSizeLeft());
	  
    //////////////////////////////
    // Right
    //////////////////////////////
    // Features -> weight map
    // Features -> weight map
		TObjectFloatMap<F> sLogR = new TObjectFloatCustomHashMap<>(
		    featureExtractor.getHashingStrat(), 100, 0.5f, 0);
		int nbrTracesR = getPaddedFeatureMap(itTracesRight, featureExtractor, sLogR, getContextLogSizeRight());
		
		logger.trace("{} transforms window with {}(+{} empty) traces left and {}(+{} empty) traces right", this.toString(), 
				nbrTracesL, contextLogSizeLeft - nbrTracesL, nbrTracesR, contextLogSizeRight - nbrTracesR);
		
		OrderedStochasticLanguage<F> languageL = new OrderedFreqBasedStochLanguageImpl<>(
				sLogL, contextLogSizeLeft, contextLogSizeLeft);

		OrderedStochasticLanguage<F> languageR = new OrderedFreqBasedStochLanguageImpl<>(
				sLogR, contextLogSizeRight, contextLogSizeRight);

		return Pair.of(languageL, languageR);
	}
	
  /**
   * Extract the <b>empty-trace-padded feature map</b> from the provided xlog. 
   * 
   * @param <F> Feature type
   * @param log Log
   * @param featureExtractor Feature extractor
   * @param featureMap Feature map to add the features to
   * @param contextLogSize Context log size used for padding with empty traces
   * @return Number of features added (equals number of cases in log)
   */
  private<V extends CVariant, F extends TraceDescriptor> int getPaddedFeatureMap(Iterator<XTrace> itLog,
      XLogTraceFeatureExtractor<F> featureExtractor, TObjectFloatMap<F> featureMap, int contextLogSize) {
    // Number of traces
		int nbrTraces = 0;
		while (itLog.hasNext()) {
      XTrace trace = itLog.next();
		  // Extract and add
		  featureExtractor.addTraceDescriptor(trace, featureMap);
			nbrTraces ++;
		}

		// Empty trace padding
		if(contextLogSize - nbrTraces > 0 ) {
			featureMap.put(featureExtractor.getEmptyTrace(), contextLogSize - nbrTraces);
		}
    return nbrTraces;
  }

  ////////////////////////////////////////////////////////////////////////////////
	// Variant log-based Extraction
  ////////////////////////////////////////////////////////////////////////////////
  @Override
	public<V extends CVariant, F extends TraceDescriptor> 
      Pair<OrderedStochasticLanguage<F>, OrderedStochasticLanguage<F>> transformWindow(
        CVariantLog<? extends V> logLeft, CVariantLog<? extends V> logRight, 
        VariantBasedFeatureExtractor<V, F> variantFeatureExtractor) {

    
    ////////////////////////////////////////
    // Feature Extraction
    ////////////////////////////////////////
    //////////////////////////////
    // Left
    //////////////////////////////
    // Features -> weight map
		TObjectFloatMap<F> sLogL = new TObjectFloatCustomHashMap<>(
		    variantFeatureExtractor.getHashingStrat(), 100, 0.5f, 0);

		int nbrTracesL = getPaddedFeatureMap(logLeft, variantFeatureExtractor, sLogL, getContextLogSizeLeft());

    //////////////////////////////
    // Right
    //////////////////////////////
		TObjectFloatMap<F> sLogR = new TObjectFloatCustomHashMap<>(
		    variantFeatureExtractor.getHashingStrat(), 100, 0.5f, 0);
    // Features -> weight map
		int nbrTracesR = getPaddedFeatureMap(logRight, variantFeatureExtractor, sLogR, getContextLogSizeRight());

		logger.trace("{} transforms window with {}(+{} empty) traces left and {}(+{} empty) traces right", this.toString(), 
				nbrTracesL, getContextLogSizeLeft() - nbrTracesL, nbrTracesR, getContextLogSizeRight() - nbrTracesR);
		
		OrderedStochasticLanguage<F> languageL = new OrderedFreqBasedStochLanguageImpl<F>(
				sLogL, getContextLogSizeLeft(), getContextLogSizeLeft());

		OrderedStochasticLanguage<F> languageR = new OrderedFreqBasedStochLanguageImpl<F>(
				sLogR, getContextLogSizeRight(), getContextLogSizeRight());

		//if(languageL.getNumberOfTraceVariants() > 10 || languageR.getNumberOfTraceVariants() > 10) {
		//	logger.trace(() -> "Created stochastic languages: .... (Too big to display properly)");
		//}
		//else {
		//	logger.trace(() -> String.format("Created stochastic languages: %s \n____________________\n%s", languageL.toString(), languageR.toString()));
		//}
		
		return Pair.of(languageL, languageR);
  }

  /**
   * Extract the <b>empty-trace-padded feature map</b> from the provided log. 
   * 
   * @param <T> Type of the variants
   * @param <S> Variant type required by the feature extractor - <b>super type of the variant type</b>
   * @param log Variant log
   * @param variantFeatureExtractor Feature extractor
   * @param featureMap Feature map to add the features to
   * @param contextLogSize Context log size used for padding with empty traces
   * @return Number of features added (equals number of cases in log)
   */
  private<V extends CVariant, F extends TraceDescriptor> int getPaddedFeatureMap(CVariantLog<? extends V> log,
      VariantBasedFeatureExtractor<V, F> variantFeatureExtractor, TObjectFloatMap<F> featureMap, 
      int contextLogSize) {
    // Number of traces
		int nbrTraces = 0;
		for(V variant : log) {
		  // Extract
			Multiset<F> traceDescriptors = variantFeatureExtractor.getTraceDescriptor(variant, log);
			// One feature per trace!
			assert traceDescriptors.size() == variant.getSupport();

			// Add features 
			traceDescriptors.forEachEntry((f, c) -> featureMap.adjustOrPutValue(f, c, c));
			// Count total number of traces == total numer of features
			nbrTraces += variant.getSupport();
		}

		// Empty trace padding
		if(contextLogSize - nbrTraces > 0 ) {
			featureMap.put(variantFeatureExtractor.getEmptyCVariant(log), contextLogSize - nbrTraces);
		}
    return nbrTraces;
  }

  @Override
  public ProbMassNonEmptyTrace probabilityMassNonEmptyTraces(CVariantLog<? extends CVariant> tracesLeft,
      CVariantLog<? extends CVariant> tracesRight) {
		double probNonEmptyLeft = ((double) tracesLeft.sizeLog()) / this.getContextLogSizeLeft();
		double probNonEmptyRight = ((double) tracesRight.sizeLog()) / this.getContextLogSizeRight();

		return new ProbMassNonEmptyTrace(probNonEmptyLeft, probNonEmptyRight, 
				((tracesLeft.sizeLog() == 0) && (tracesRight.sizeLog() == 0)));
  }
	

	@Override
	public String toString() {
		return "ContextAwareEmptyTraceBalancedTransformer in mode " + scalingContext.toString() + 
		" with size left/right " + contextLogSizeLeft + "/" + contextLogSizeRight;
	}

	////////////////////////////////////////
	// Getters and Setters
	////////////////////////////////////////
	@Override
	public String getShortDescription() {
		return "Balanced by empty traces (" + scalingContext + ")";
	}

	public int getContextLogSizeLeft() {
		return contextLogSizeLeft;
	}

	public int getContextLogSizeRight() {
		return contextLogSizeRight;
	}
	
	public ScalingContext getScalingContext() {
		return scalingContext;
	}
	
	public void setContextLogSizeLeft(int contextLogSizeLeft) {
		this.contextLogSizeLeft = contextLogSizeLeft;
	}
	
	public void setContextLogSizeRight(int contextLogSizeRight) {
		this.contextLogSizeRight = contextLogSizeRight;
	}
	
}
