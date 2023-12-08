package de.rwth.processmining.tb.core.sps.algorithm.measure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.FeatureExtractorDistancePairVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.HFDDVertexLogTransformerOuterContext;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.VertexConditioningLogTransformer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.viewsetup.ViewConfigConditionProvider;
import de.rwth.processmining.tb.core.sps.algorithm.measure.viewsetup.ViewConfigProvider;
import de.rwth.processmining.tb.core.sps.data.csgraph.CSMeasurementTypes;
import de.rwth.processmining.tb.core.sps.data.csgraph.PerspectiveCSConditioned;
import de.rwth.processmining.tb.core.sps.data.csgraph.PerspectiveCSResidual;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraph;

public class CSVertexConditionedMeasurer
  <V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
  extends CSVertexMeasurerBase<V, F, D> {

	private final ArrayList<Set<VertexCondition>> conditionSets;

	private final static PerspectiveDescriptor pDesc = new PerspectiveCSConditioned();

	/**
	 * Activity distance pair that will be used to transform the variant log into
	 * an EMD problem.
	 */
	private final FeatureExtractorDistancePairVariant<F, V, D> trDescDist;
	
	public CSVertexConditionedMeasurer(CSGraph csGraph, 
			FeatureExtractorDistancePairVariant<F, V, D> trDescDist,
			ArrayList<Set<VertexCondition>> conditionSets) {
		super(csGraph, false);
		this.trDescDist = trDescDist;
		this.conditionSets = conditionSets;
	}
	
	@Override
	public PerspectiveDescriptor getMeasurementDescription() {
		return pDesc;
	}

	@Override
	protected HFDDVertexMeasurerImpl<V, F, D> getMeasurer(BiComparisonDataSource<? extends V> biCompDS) {
		
		////////////////////////////////////////
		// Setup Measurer
		////////////////////////////////////////
		// External context log transformer
		List<HFDDVertexLogTransformerOuterContext> transformersOuterContext = new LinkedList<>();
		// Conditioning
		HFDDVertexLogTransformerOuterContext conditioningTransformer = 
				new VertexConditioningLogTransformer(Optional.of(conditionSets));
		transformersOuterContext.add(conditioningTransformer);
		
		// View provider
		ViewConfigProvider<V, F, D> viewProvider = new ViewConfigConditionProvider<V, F, D>(trDescDist);
		
		HFDDVertexMeasurerImpl<V, F, D> measurer = new HFDDVertexMeasurerImpl<>(
				new PerspectiveCSResidual(), 
				viewProvider,
				Optional.of(transformersOuterContext));
		return measurer;
	}

	@Override
	protected CSMeasurementTypes getMeasurementType() {
		return CSMeasurementTypes.CONDITIONED;
	}
}

