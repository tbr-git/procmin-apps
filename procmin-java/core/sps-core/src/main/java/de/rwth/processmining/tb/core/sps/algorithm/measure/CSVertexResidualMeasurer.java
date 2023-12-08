package de.rwth.processmining.tb.core.sps.algorithm.measure;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.emd.dataview.ViewIdentifier;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.FeatureExtractorDistancePairVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.ViewConfigVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2SimpleNormOrdStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ContextAwareEmptyTraceBalancedTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ScalingContext;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.CSGraphResidualLogTransformer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.HFDDVertexLogTransformerOuterContext;
import de.rwth.processmining.tb.core.sps.algorithm.measure.viewsetup.ViewConfigConstantProvider;
import de.rwth.processmining.tb.core.sps.algorithm.measure.viewsetup.ViewConfigProvider;
import de.rwth.processmining.tb.core.sps.data.csgraph.CSMeasurementTypes;
import de.rwth.processmining.tb.core.sps.data.csgraph.PerspectiveCSResidual;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraph;

public class CSVertexResidualMeasurer
  <V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
  extends CSVertexMeasurerBase<V, F, D> {
	
	private final static Logger logger = LogManager.getLogger( CSVertexResidualMeasurer.class );
	
	private final static PerspectiveDescriptor pDesc = new PerspectiveCSResidual();
	
	/**
	 * Activity distance pair that will be used to transform the variant log into
	 * an EMD problem.
	 */
	private final FeatureExtractorDistancePairVariant<F, V, D> trDescDist;
	
	public CSVertexResidualMeasurer(CSGraph csGraph, 
	    FeatureExtractorDistancePairVariant<F, V, D> trDescDist) {
		super(csGraph);
		this.trDescDist = trDescDist;
	}
	
	@Override
	public PerspectiveDescriptor getMeasurementDescription() {
		return pDesc;
	}

	@Override
	protected HFDDVertexMeasurerImpl<V, F, D> getMeasurer(BiComparisonDataSource<? extends V> biCompDS) {
		// Create language transformer
		Window2OrderedStochLangTransformer langTransformer;
		try {
			langTransformer = new ContextAwareEmptyTraceBalancedTransformer(biCompDS.getDataSourceLeftBase().getVariantLog().sizeLog(), 
					biCompDS.getDataSourceRightBase().getVariantLog().sizeLog(), ScalingContext.GLOBAL);
		} catch (SLDSTransformationError e) {
			logger.error("Failed to instantiate the measurement executor because the size of the context log could not be determined. "
					+ "Fallback to standard normalization!");
			langTransformer = new Window2SimpleNormOrdStochLangTransformer();
		}

		// Distance + Trace descriptor factory + transformer => view
		ViewConfigVariant<V, F, D> viewConfig = new ViewConfigVariant<>(langTransformer, this.trDescDist,
				new ViewIdentifier(this.trDescDist.getShortDescription() 
						+ " - " + langTransformer.getShortDescription()));
		
		////////////////////////////////////////
		// Setup Measurer
		////////////////////////////////////////
		// External context log transformer
		HFDDVertexLogTransformerOuterContext residualTransformer = 
				new CSGraphResidualLogTransformer(getCsGraph());
		List<HFDDVertexLogTransformerOuterContext> transformersOuterContext = 
				Collections.singletonList(residualTransformer);
		
		// View provider
		ViewConfigProvider<V, F, D> viewProvider = new ViewConfigConstantProvider<>(viewConfig);
		
		HFDDVertexMeasurerImpl<V, F, D> measurer = new HFDDVertexMeasurerImpl<>(
				new PerspectiveCSResidual(), 
				viewProvider,
				Optional.of(transformersOuterContext));
		// TODO Auto-generated method stub
		return measurer;
	}

	@Override
	protected CSMeasurementTypes getMeasurementType() {
		return CSMeasurementTypes.RESIDUAL;
	}
}
