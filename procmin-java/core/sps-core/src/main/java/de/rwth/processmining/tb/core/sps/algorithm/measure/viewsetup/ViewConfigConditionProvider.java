package de.rwth.processmining.tb.core.sps.algorithm.measure.viewsetup;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.ViewIdentifier;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.FeatureExtractorDistancePairVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.ViewConfigVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2SimpleNormOrdStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ContextAwareEmptyTraceBalancedTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ScalingContext;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.FilterTag;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.HFDDLogTransformStep;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;

public class ViewConfigConditionProvider
  <V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
  implements ViewConfigProvider<V, F, D> {
	private final static Logger logger = LogManager.getLogger( ViewConfigAbstFreeExtConditionedProvider.class );

	private final FeatureExtractorDistancePairVariant<F, V, D> descDistPair;
	
	public ViewConfigConditionProvider(
			FeatureExtractorDistancePairVariant<F, V, D> descDistPair) {
		
		this.descDistPair = descDistPair;
		
	}

	@Override
	public<B extends V> ViewConfigVariant<V, F, D> provideViewConfig(
	    HFDDVertex v, 
	    List<HFDDLogTransformStep<B>> vertexLogFilterStack) {
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
		
		
		ViewConfigVariant<V, F, D> viewConfig = new ViewConfigVariant<>(langTransformer, descDistPair,
				new ViewIdentifier(this.descDistPair.getShortDescription() 
						+ " - " + langTransformer.getShortDescription()));
		return viewConfig;
	}

}
