package de.rwth.processmining.tb.core.sps.algorithm.measure.viewsetup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.abstraction.CVariantAbst;
import de.rwth.processmining.tb.core.emd.dataview.ViewIdentifier;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.FeatureExtractorDistancePairVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.ViewConfigVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDistFreeTraceDelInsWrapper;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.AbstTraceCC;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2SimpleNormOrdStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ContextAwareEmptyTraceBalancedTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ScalingContext;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexCondition;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexConditionType;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.FilterTag;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.HFDDLogTransformStep;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
//public class ViewConfigAbstFreeExtConditionedProvider<V extends CVariantAbst, 
//    F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> implements ViewConfigProvider<V, F, D> {
public class ViewConfigAbstFreeExtConditionedProvider
    implements ViewConfigProvider<CVariantAbst, AbstTraceCC, TraceDescDistCalculator<AbstTraceCC>> {
	private final static Logger logger = LogManager.getLogger( ViewConfigAbstFreeExtConditionedProvider.class );

	private final FeatureExtractorDistancePairVariant<AbstTraceCC, 
	  CVariantAbst, TraceDescDistCalculator<AbstTraceCC>> descDistPair;
	
	private final Optional<ArrayList<Set<VertexCondition>>> aggDataCBase;

	public ViewConfigAbstFreeExtConditionedProvider(
			Optional<ArrayList<Set<VertexCondition>>> aggDataCBase, 
      FeatureExtractorDistancePairVariant
        <AbstTraceCC, CVariantAbst, TraceDescDistCalculator<AbstTraceCC>> descDistPair) {
		
		this.descDistPair = descDistPair;
		this.aggDataCBase = aggDataCBase;
		
	}

	@Override
	public<B extends CVariantAbst> ViewConfigVariant<CVariantAbst, AbstTraceCC, TraceDescDistCalculator<AbstTraceCC>> provideViewConfig(
	    HFDDVertex v, List<HFDDLogTransformStep<B>> vertexLogFilterStack) {
		////////////////////
		// Find Condition Step 
		////////////////////
		Optional<HFDDLogTransformStep<B>> filterStepCond = vertexLogFilterStack.stream()
				.filter(s -> ((s.filterTag() == FilterTag.CONDITION) 
						|| (s.filterTag() == FilterTag.CONDITION_NONE) 
						|| (s.filterTag() == FilterTag.IN)))
				.findFirst();
		if (filterStepCond.isEmpty()) {
			throw new IllegalArgumentException("The filter stack does not contain any condition filter step nor a base input");
		}

		// Create language transformer
		Window2OrderedStochLangTransformer langTransformer;
		try {
			// Don't take the base log but the transformed one!
			langTransformer = new ContextAwareEmptyTraceBalancedTransformer(
					filterStepCond.get().biCompDS().getDataSourceLeft().getVariantLog().sizeLog(), 
					filterStepCond.get().biCompDS().getDataSourceRight().getVariantLog().sizeLog(), 
					ScalingContext.GLOBAL);
		} catch (SLDSTransformationError e) {
			logger.error("Failed to instantiate the measurement executor because the size of the context log could not be determined. "
					+ "Fallback to standard normalization!");
			langTransformer = new Window2SimpleNormOrdStochLangTransformer();
		}
		
		// If for this vertex, we allow for free trace delete / insert,
		// create a local wrapper around the trace distance, that enables that for all trace descriptors. 
		// -> We can enable this even if the context is not directly visible from the trace descriptors.
		// (For example, data-based subprocess dependencies)
		
		FeatureExtractorDistancePairVariant<AbstTraceCC, CVariantAbst, TraceDescDistCalculator<AbstTraceCC>> 
		  localDescDistPair = this.descDistPair;
		if (aggDataCBase.isPresent() && aggDataCBase.get().get(v.getId()) != null) {
			Set<VertexCondition> s = aggDataCBase.get().get(v.getId());
			
			boolean freeLeftDelete = s.stream()
					.anyMatch(vCond -> vCond.type() == VertexConditionType.CONDFREQIGNORELEFT);
			boolean freeRightInsert = s.stream()
					.anyMatch(vCond -> vCond.type() == VertexConditionType.CONDFREQIGNORERIGHT);
			
			if (freeLeftDelete || freeRightInsert) {
				TraceDistFreeTraceDelInsWrapper<AbstTraceCC> vertexLocalDistWrapper = 
						new TraceDistFreeTraceDelInsWrapper<>(this.descDistPair.getDistance());
				vertexLocalDistWrapper.setFreeLeftTraceDelete(freeLeftDelete);
				vertexLocalDistWrapper.setFreeRightTraceInsert(freeRightInsert);
				localDescDistPair = new FeatureExtractorDistancePairVariant<>(
				    this.descDistPair.getFeatureExtractor(), 
				    vertexLocalDistWrapper);
			}
		}

		ViewConfigVariant<CVariantAbst, AbstTraceCC, TraceDescDistCalculator<AbstTraceCC>> viewConfig = 
		    new ViewConfigVariant<CVariantAbst, AbstTraceCC, TraceDescDistCalculator<AbstTraceCC>>(
		        langTransformer, localDescDistPair, 
		        new ViewIdentifier(localDescDistPair.getShortDescription() 
		            + " - " + langTransformer.getShortDescription()));
		return viewConfig;
	}

}
