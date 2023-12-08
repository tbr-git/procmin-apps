package de.rwth.processmining.tb.core.sps.algorithm.measure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.abstraction.CVariantAbst;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.emd.dataview.ViewIdentifier;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.FeatureExtractorDistancePairVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.ViewConfigVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.grounddistances.controlflow.AdaptiveLVS;
import de.rwth.processmining.tb.core.emd.grounddistances.controlflow.LevenshteinCCStateful;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.AbstTraceCC;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased.TraceAsFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased.TraceWAbstAsFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ContextAwareEmptyTraceBalancedTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ScalingContext;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.HFDDVertexLogTransformerOuterContext;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.VertexConditioningLogTransformer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.viewsetup.ViewConfigAbstFreeExtConditionedProvider;
import de.rwth.processmining.tb.core.sps.algorithm.measure.viewsetup.ViewConfigConstantProvider;

public class HFDDVertexMeasurerFactory {
	
	public static 
	  HFDDVertexMeasurer<CVariant, BasicTraceCC> getBaseMeasuresWithoutContext(
			BiComparisonDataSource<? extends CVariant> biCompDS, PerspectiveDescriptor pDesc) throws SLDSTransformationError {
		//////////////////////////////
		// View
		//////////////////////////////
		// Language transformer
		Window2OrderedStochLangTransformer langTransformer = null;
		langTransformer = new ContextAwareEmptyTraceBalancedTransformer(
				biCompDS.getDataSourceLeft().getVariantLog().sizeLog(), 
				biCompDS.getDataSourceRight().getVariantLog().sizeLog(), ScalingContext.GLOBAL);
		
		// Trace descriptor + distance
		FeatureExtractorDistancePairVariant<BasicTraceCC, CVariant, LevenshteinCCStateful> desDistPair = 
		    new FeatureExtractorDistancePairVariant<BasicTraceCC, CVariant, LevenshteinCCStateful>(
		        new TraceAsFeatureExtractor(), new LevenshteinCCStateful());

		ViewConfigVariant<CVariant, BasicTraceCC, LevenshteinCCStateful> viewConfig = 
		    new ViewConfigVariant<>(langTransformer, desDistPair,
				new ViewIdentifier(desDistPair.getShortDescription() + " - " + langTransformer.getShortDescription()));
		
		ViewConfigConstantProvider<CVariant, BasicTraceCC, LevenshteinCCStateful> viewConfigProvider = 
		    new ViewConfigConstantProvider<>(viewConfig);
		
		HFDDVertexMeasurerImpl<CVariant, BasicTraceCC, LevenshteinCCStateful> measurer = 
		    new HFDDVertexMeasurerImpl<>(pDesc, viewConfigProvider, Optional.empty());

		return measurer;
	}
	
	public static HFDDVertexMeasurer<CVariantAbst, AbstTraceCC> defaultMeasurerAbst(
	    BiComparisonDataSource<? extends CVariantAbst> biCompDS,
			PerspectiveDescriptor pDesc) throws SLDSTransformationError {

		// Create language transformer
		Window2OrderedStochLangTransformer langTransformer;
		langTransformer = new ContextAwareEmptyTraceBalancedTransformer(
		    biCompDS.getDataSourceLeftBase().getVariantLog().sizeLog(), 
				biCompDS.getDataSourceRightBase().getVariantLog().sizeLog(), ScalingContext.GLOBAL);

		// Distance + Trace descriptor factory + transformer => view
		// Trace descriptor + distance
		FeatureExtractorDistancePairVariant<AbstTraceCC, CVariantAbst, AdaptiveLVS> desDistPair = 
		    new FeatureExtractorDistancePairVariant<AbstTraceCC, CVariantAbst, AdaptiveLVS>(
		        new TraceWAbstAsFeatureExtractor(), new AdaptiveLVS());
		
		
		ViewConfigVariant<CVariantAbst, AbstTraceCC, AdaptiveLVS> viewConfig = 
		    new ViewConfigVariant<>(langTransformer, desDistPair,
				new ViewIdentifier(desDistPair.getShortDescription() + " - " + langTransformer.getShortDescription()));
		

		ViewConfigConstantProvider<CVariantAbst, AbstTraceCC, AdaptiveLVS> viewConfigProvider = 
		    new ViewConfigConstantProvider<>(viewConfig);
		
		HFDDVertexMeasurerImpl<CVariantAbst, AbstTraceCC, AdaptiveLVS> measurer = 
		    new HFDDVertexMeasurerImpl<>(pDesc, viewConfigProvider, Optional.empty());
		return measurer;
		
	}
	
	public static HFDDVertexMeasurer<CVariantAbst, AbstTraceCC> createMeasurerAbstContextFreeCond(
			XEventClassifier classifier, 
			AdaptiveLVS aLVS, Optional<ArrayList<Set<VertexCondition>>> condBase, 
			PerspectiveDescriptor pDesc)  {

    FeatureExtractorDistancePairVariant<AbstTraceCC, CVariantAbst, TraceDescDistCalculator<AbstTraceCC>> descDistPair = 
        new FeatureExtractorDistancePairVariant<AbstTraceCC, CVariantAbst, TraceDescDistCalculator<AbstTraceCC>>(
        new TraceWAbstAsFeatureExtractor(), aLVS);

		ViewConfigAbstFreeExtConditionedProvider viewConfigProvider = 
				new ViewConfigAbstFreeExtConditionedProvider(condBase, descDistPair);
	
		HFDDVertexLogTransformerOuterContext condTransformer = 
		    new VertexConditioningLogTransformer(condBase);
		List<HFDDVertexLogTransformerOuterContext> transformersOuterContex = 
				Collections.singletonList(condTransformer);

		HFDDVertexMeasurerImpl<CVariantAbst, AbstTraceCC, TraceDescDistCalculator<AbstTraceCC>> measurer = new HFDDVertexMeasurerImpl<>(pDesc, 
				viewConfigProvider, 
				Optional.of(transformersOuterContex));
		
		return measurer;
	}

}
