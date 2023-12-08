package de.rwth.processmining.tb.core.emd.language.transformer;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.StochLangFactory;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased.VariantBasedFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.XLogTraceFeatureExtractor;

public class Window2SimpleNormOrdStochLangTransformer implements Window2OrderedStochLangTransformer {
	
	public Window2SimpleNormOrdStochLangTransformer() {
		
	}

	public Window2SimpleNormOrdStochLangTransformer(Window2SimpleNormOrdStochLangTransformer t) {
	}

	@Override
	public<F extends TraceDescriptor> Pair<OrderedStochasticLanguage<F>, OrderedStochasticLanguage<F>> 
      transformWindow(Iterator<XTrace> itTracesLeft, Iterator<XTrace> itTracesRight, 
        XLogTraceFeatureExtractor<F> featureExtractor) {
		OrderedStochasticLanguage<F> langL = StochLangFactory.xLogConvertOrdered(itTracesLeft, null, featureExtractor);
		OrderedStochasticLanguage<F> langR = StochLangFactory.xLogConvertOrdered(itTracesRight, null, featureExtractor);
		return Pair.of(langL, langR);
	}

	@Override
	public String getShortDescription() {
		return "Normalized";
	}

	@Override
	public<V extends CVariant, F extends TraceDescriptor> Pair<OrderedStochasticLanguage<F>, OrderedStochasticLanguage<F>> 
	  transformWindow(CVariantLog<? extends V> tracesLeft, 
			CVariantLog<? extends V> tracesRight, VariantBasedFeatureExtractor<V, F> featureExtractor) {
		OrderedStochasticLanguage<F> langL = StochLangFactory.variantLogConvertOrdered(tracesLeft, null, featureExtractor);
		OrderedStochasticLanguage<F> langR = StochLangFactory.variantLogConvertOrdered(tracesRight, null, featureExtractor);
		return Pair.of(langL, langR);
	}
	
	@Override
	public ProbMassNonEmptyTrace probabilityMassNonEmptyTraces(CVariantLog<? extends CVariant> tracesLeft,
			CVariantLog<? extends CVariant> tracesRight) {
		boolean isEmpty = (tracesLeft.sizeLog() == 0) && (tracesRight.sizeLog() == 0);
		return new ProbMassNonEmptyTrace(isEmpty ? 0. : 1.0, isEmpty ? 0. : 1.0, isEmpty);
	}
	
}
