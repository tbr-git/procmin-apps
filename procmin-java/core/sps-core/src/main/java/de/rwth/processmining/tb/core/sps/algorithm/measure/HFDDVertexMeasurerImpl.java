package de.rwth.processmining.tb.core.sps.algorithm.measure;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.emd.dataview.ViewConfig;
import de.rwth.processmining.tb.core.emd.dataview.ViewDataException;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.ViewConfigVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.transformer.ProbMassNonEmptyTrace;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolAnalyzer;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.FilterTag;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.HFDDLogTransformStep;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.HFDDVertexLogTransformerOuterContext;
import de.rwth.processmining.tb.core.sps.algorithm.measure.viewsetup.ViewConfigProvider;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurement;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurementEMDSol;

public class HFDDVertexMeasurerImpl
    <V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>>
    implements HFDDVertexMeasurer<V, F> {
	private final static Logger logger = LogManager.getLogger( HFDDVertexMeasurerImpl.class );

	/**
	 * Given the graph data source, prepare it according to the outer context.
	 * For example, if we condition on another vertex, this can reduce the
	 * datasource to the conditioned variants.  
	 */
	private final Optional<List<HFDDVertexLogTransformerOuterContext>> logTransOuterCont;
	
	/**
	 * Name of the perspective that is realized using the provided {@link this#viewConfig}.
	 */
	private final PerspectiveDescriptor perspectiveDescription;
	
	private final ViewConfigProvider<V, F, D> viewConfigProvider;

	public HFDDVertexMeasurerImpl(PerspectiveDescriptor perspectiveDescription, 
			ViewConfigProvider<V, F, D> viewConfigProvider,
			Optional<List<HFDDVertexLogTransformerOuterContext>> logTransOuterCont) {
		super();
		this.viewConfigProvider = viewConfigProvider;
		this.perspectiveDescription = perspectiveDescription;
		this.logTransOuterCont = logTransOuterCont;
	}
	
	@Override
	public<B extends V> ProbMassNonEmptyTrace getProbabilityMassNonEmpty(HFDDVertex v, BiComparisonDataSource<B> biCompDS) {
		////////////////////
		// Outer Context
		////////////////////
		List<HFDDLogTransformStep<B>> vertexLogFilterStack = new LinkedList<>();
		HFDDLogTransformStep<B> lastStep = new HFDDLogTransformStep<B>(biCompDS, FilterTag.IN);
		vertexLogFilterStack.add(0, lastStep);
		if (logTransOuterCont.isPresent()) {
			// Apply all transformers
			for (HFDDVertexLogTransformerOuterContext transOutC : logTransOuterCont.get()) {
				try {
					lastStep = transOutC.getDataSourceOuterContext(v, lastStep.biCompDS());
					// Push to filter stack
					vertexLogFilterStack.add(0, lastStep);

				} catch (SLDSTransformerBuildingException e1) {
					logger.error("Error during HFDDVertex outer context transformation");
					e1.printStackTrace();
					return null;
				}
			}
		}

		////////////////////
		// Inner Context: Prepare log 
		////////////////////
		BiComparisonDataSource<B> biCompDSVertex;
		try {
			biCompDSVertex = v.getVertexInfo().createVertexLog(lastStep.biCompDS());
		} catch (SLDSTransformerBuildingException e) {
			e.printStackTrace();
			return null;
		}
		vertexLogFilterStack.add(0, new HFDDLogTransformStep<B>(biCompDSVertex, FilterTag.VERTEX));
		
		////////////////////
		// View Config
		////////////////////
		ViewConfig viewConfig = this.viewConfigProvider.provideViewConfig(v, vertexLogFilterStack);
		
		////////////////////
		// Non-empty Trace Probability Mass
		////////////////////
		// Probability mass non-empty traces
		ProbMassNonEmptyTrace vertexLogPropNonEmpty;
		try {
			vertexLogPropNonEmpty = viewConfig.getLangTransformer().probabilityMassNonEmptyTraces(
					biCompDSVertex.getDataSourceLeft().getVariantLog(),
					biCompDSVertex.getDataSourceRight().getVariantLog());
			return vertexLogPropNonEmpty;
		} catch (SLDSTransformationError e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public<B extends V> HFDDMeasurement measureVertex(HFDDVertex v, BiComparisonDataSource<B> biCompDS, boolean save) {
		////////////////////
		// Outer Context
		////////////////////
		List<HFDDLogTransformStep<B>> vertexLogFilterStack = new LinkedList<>();
		HFDDLogTransformStep<B> lastStep = new HFDDLogTransformStep<B>(biCompDS, FilterTag.IN);
		vertexLogFilterStack.add(0, lastStep);
		if (logTransOuterCont.isPresent()) {
			// Apply all transformers
			for (HFDDVertexLogTransformerOuterContext transOutC : logTransOuterCont.get()) {
				try {
					lastStep = transOutC.getDataSourceOuterContext(v, lastStep.biCompDS());
					// Push to filter stack
					vertexLogFilterStack.add(0, lastStep);

				} catch (SLDSTransformerBuildingException e1) {
					logger.error("Error during HFDDVertex outer context transformation");
					e1.printStackTrace();
					return new HFDDMeasurement(perspectiveDescription, null, 0, 0, false, true);
				}
			}
		}

		////////////////////
		// Inner Context: Prepare log 
		////////////////////
		BiComparisonDataSource<B> biCompDSVertex;
		try {
			biCompDSVertex = v.getVertexInfo().createVertexLog(lastStep.biCompDS());
		} catch (SLDSTransformerBuildingException e) {
			e.printStackTrace();
			return new HFDDMeasurement(perspectiveDescription, null, 0, 0, false, true);
		}
		vertexLogFilterStack.add(0, new HFDDLogTransformStep<B>(biCompDSVertex, FilterTag.VERTEX));
		
		////////////////////
		// View Config
		////////////////////
		ViewConfigVariant<V, F, D> viewConfig = 
		    this.viewConfigProvider.provideViewConfig(v, vertexLogFilterStack);
		
		////////////////////
		// EMD
		////////////////////
		Optional<EMDSolContainer<F>> emdSol;
		try {
			emdSol = BiDSDiffMeasure.measureEMD(biCompDSVertex, viewConfig, perspectiveDescription);
		} catch (ViewDataException e) {
			e.printStackTrace();
			return new HFDDMeasurement(perspectiveDescription, viewConfig, 0, 0, false, true);
		}

		if (emdSol.isEmpty()) {
			// logger.error("Error during HFDDVertex measurement! Could not realize EMD!");
			return new HFDDMeasurement(perspectiveDescription, viewConfig, 0, 0, false, true);
		}

		// Probability mass non-empty traces
		ProbMassNonEmptyTrace vertexLogPropNonEmpty;
		try {
			vertexLogPropNonEmpty = viewConfig.getLangTransformer().probabilityMassNonEmptyTraces(
					biCompDSVertex.getDataSourceLeft().getVariantLog(),
					biCompDSVertex.getDataSourceRight().getVariantLog());
		} catch (SLDSTransformationError e) {
			e.printStackTrace();
			return new HFDDMeasurement(perspectiveDescription, viewConfig, 0, 0, false, true);
		}
		HFDDMeasurement m = new HFDDMeasurement(perspectiveDescription, viewConfig, 
				vertexLogPropNonEmpty.left(), vertexLogPropNonEmpty.right() ,
				emdSol.get().getEMD(), EMDSolAnalyzer.flowEmptyCost(emdSol.get()), vertexLogPropNonEmpty.allZero());

		// Save
		if (save) {
			v.getVertexInfo().addMeasurement(m);
		}
		return m;
	}

	@Override
	public<B extends V> HFDDMeasurementEMDSol<F> measureVertexDetails(HFDDVertex v, BiComparisonDataSource<B> biCompDS,
			boolean save) {
		
		////////////////////
		// Outer Context
		////////////////////
		List<HFDDLogTransformStep<B>> vertexLogFilterStack = new LinkedList<>();
		HFDDLogTransformStep<B> lastStep = new HFDDLogTransformStep<B>(biCompDS, FilterTag.IN);
		vertexLogFilterStack.add(0, lastStep);
		if (logTransOuterCont.isPresent()) {
			// Apply all transformers
			for (HFDDVertexLogTransformerOuterContext transOutC : logTransOuterCont.get()) {
				try {
					lastStep = transOutC.getDataSourceOuterContext(v, lastStep.biCompDS());
					// Push to filter stack
					vertexLogFilterStack.add(0, lastStep);

				} catch (SLDSTransformerBuildingException e1) {
					logger.error("Error during HFDDVertex outer context transformation");
					e1.printStackTrace();
					return new HFDDMeasurementEMDSol<F>(
					    perspectiveDescription, null, 0, 0, Optional.empty(), false, true);
				}
			}
		}

		////////////////////
		// Inner Context: Prepare log 
		////////////////////
		BiComparisonDataSource<B> biCompDSVertex;
		try {
			biCompDSVertex = v.getVertexInfo().createVertexLog(lastStep.biCompDS());
		} catch (SLDSTransformerBuildingException e) {
			e.printStackTrace();
			return new HFDDMeasurementEMDSol<F>(perspectiveDescription, null, 0, 0, Optional.empty(), false, true);
		}
		vertexLogFilterStack.add(0, new HFDDLogTransformStep<B>(biCompDSVertex, FilterTag.VERTEX));
		
		////////////////////
		// View Config
		////////////////////
		ViewConfigVariant<V, F, D> viewConfig = 
		    this.viewConfigProvider.provideViewConfig(v, vertexLogFilterStack);

		////////////////////
		// EMD
		////////////////////
		Optional<EMDSolContainer<F>> emdSol;
		try {
			emdSol = BiDSDiffMeasure.measureEMD(biCompDSVertex, viewConfig, perspectiveDescription);
		} catch (ViewDataException e) {
			e.printStackTrace();
			return new HFDDMeasurementEMDSol<F>(
			    perspectiveDescription, viewConfig, 0, 0, Optional.empty(), false, true);
		}

		if (emdSol.isEmpty()) {
			logger.error("Error during HFDDVertex measurement! Could not realize EMD!");
			return new HFDDMeasurementEMDSol<F>(
			    perspectiveDescription, viewConfig, 0, 0, Optional.empty(), false, true);
		}

		// Probability mass non-empty traces
		ProbMassNonEmptyTrace vertexLogPropNonEmpty;
		try {
			vertexLogPropNonEmpty = viewConfig.getLangTransformer().probabilityMassNonEmptyTraces(
					biCompDSVertex.getDataSourceLeft().getVariantLog(),
					biCompDSVertex.getDataSourceRight().getVariantLog());
		} catch (SLDSTransformationError e) {
			e.printStackTrace();
			return new HFDDMeasurementEMDSol<F>(
			    perspectiveDescription, viewConfig, 0, 0, Optional.empty(), false, true);
		}

		HFDDMeasurementEMDSol<F> m = new HFDDMeasurementEMDSol<F>(perspectiveDescription, viewConfig, 
				vertexLogPropNonEmpty.left(), vertexLogPropNonEmpty.right(), Optional.of(emdSol.get()), 
				true, vertexLogPropNonEmpty.allZero());
		if (save) {
			v.getVertexInfo().addMeasurement(m);
		}
		return m;
	}

	@Override
	public PerspectiveDescriptor getMeasurementDescription() {
		return perspectiveDescription;
	}
}

