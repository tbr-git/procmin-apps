package de.rwth.processmining.tb.core.sps.algorithm.measure;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.emd.dataview.ViewDataException;
import de.rwth.processmining.tb.core.emd.dataview.ViewIdentifier;
import de.rwth.processmining.tb.core.emd.dataview.ViewRealizationSLDS;
import de.rwth.processmining.tb.core.emd.dataview.ViewRealizer;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.FeatureExtractorDistancePairVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.ViewConfigVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ContextAwareEmptyTraceBalancedTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ScalingContext;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;

public class BiDSDiffMeasure {
	private final static Logger logger = LogManager.getLogger( BiDSDiffMeasure.class );

	
	public static
	  <V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
	  Optional<EMDSolContainer<F>> measureEMDBaseLogContext(
			BiComparisonDataSource<? extends V> biCompDS, 
			FeatureExtractorDistancePairVariant<F, V, D> trDescDist, 
			PerspectiveDescriptor perspectiveDescription) throws ViewDataException {
		int sizeLogLeft;
		int sizeLogRight;
		try {
			sizeLogLeft = biCompDS.getDataSourceLeftBase().getVariantLog().sizeLog();
			sizeLogRight = biCompDS.getDataSourceRightBase().getVariantLog().sizeLog();
		} catch (SLDSTransformationError e) {
			// That should never happen
			e.printStackTrace();
			throw new IllegalStateException("Base data source is brocken!");
		}
		
		return measureEMDConditional(biCompDS, sizeLogLeft, sizeLogRight, trDescDist, perspectiveDescription);
	}
	
	/**
	 * Measure EMD on conditioned distributions.
	 * 
	 * @param <V> Minimum variant version
	 * @param <F> Feature type
	 * @param <D> Distance calculator
	 * @param biCompDSEventIntersection Variant logs that satisfy both Events (P(A and B)).
	 * @param sizeLogLeft P(B) for left log
	 * @param sizeLogRight P(B) for right log
	 * @param trDescDist Trace descriptor and trace distance
	 * @param perspectiveDescription Descriptor for the perspective on the data that is created by the measurement
	 * @return EMD Solution for conditional problem
	 * @throws ViewDataException 
	 */
	public static 
	  <V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
	  Optional<EMDSolContainer<F>> measureEMDConditional(
			BiComparisonDataSource<? extends V> biCompDSEventIntersection, int sizeLogLeft, int sizeLogRight,
			FeatureExtractorDistancePairVariant<F, V, D> trDescDist, PerspectiveDescriptor perspectiveDescription) throws ViewDataException {
		
		Window2OrderedStochLangTransformer langTransformer = new ContextAwareEmptyTraceBalancedTransformer(
					sizeLogLeft, sizeLogRight, ScalingContext.GLOBAL);
		
		ViewConfigVariant<V, F, D> viewConfig = new ViewConfigVariant<V, F, D>(
		    langTransformer, trDescDist, 
				new ViewIdentifier(trDescDist.getShortDescription() + " - " + langTransformer.getShortDescription()));
		
		return measureEMD(biCompDSEventIntersection, viewConfig, perspectiveDescription);

	}

	/**
	 * Compare the two data sources using EMD and the provided config
	 * 
	 * @param <V> Minimum variant version
	 * @param <F> Feature type
	 * @param <D> Distance calculator
	 * @param biCompDS
	 * @param viewConfig View configuration (distance and transformation into stochastic language)
	 * @param perspectiveDescription Descriptor for the perspective on the data that is created by the measurement
	 * @return EMD Solution
	 * @throws ViewDataException 
	 */
	public static 
	  <V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
	  Optional<EMDSolContainer<F>> measureEMD(BiComparisonDataSource<? extends V> biCompDS, 
			ViewConfigVariant<V, F, D> viewConfig, PerspectiveDescriptor perspectiveDescription) throws ViewDataException {
		
		////////////////////
		// Realization Sanity Check
		////////////////////
		ViewRealizationSLDS<V, F, D> real = 
		    ViewRealizer.realizeViewOnVariantLog(biCompDS, viewConfig, perspectiveDescription);
		if(hasRealizabilityProblems(real)) { 		// Check realizability
			return Optional.empty();
		}

		////////////////////
		// EMD
		////////////////////
		EMDSolContainer<F> emdSol = null;
		real.populate();
		emdSol = real.getEMDSol();
		return Optional.of(emdSol);
	}

	/**
	 * Check for realizibility problems and do some basic logging.
	 * @param real
	 * @return
	 */
	private static boolean hasRealizabilityProblems(ViewRealizationSLDS real) {
		if(!real.isRealizable()) {
			switch(real.getRealizibilityInfo().getProblemType()) {
				case DATASOURCE_BROKEN:
				case DATA_SPEC_ERROR:
					logger.error("Data source problem during measurment execution: {}!", real.getRealizibilityInfo().getInfo());
				case LITTLE_SUPPORT_LEFT:
				case LITTLE_SUPPORT_LEFTRIGHT:
				case LITTLE_SUPPORT_RIGHT:
				case NO_SUPPORT_LEFT:
				case NO_SUPPORT_LEFTRIGHT:
				case NO_SUPPORT_RIGHT:
					return true;
				default:
					logger.error("Unknown problem during measurment execution!");
					return true;
			}
		}
		return false;
	}
}
