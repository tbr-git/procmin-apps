package de.rwth.processmining.tb.core.emd.dataview;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.FeatureExtractorDistancePairVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.solver.EMDSolver;

/**
 * Realize a view on two stochastic language data sources ({@link StochasticLanguageDataSource}).
 * 
 * A view is realized based on
 * <ul>
 *  <li> Two (variant-based) datasets
 *  <li> A data-to-stochastic-language transformer
 *  <li> A features extract
 *  <li> A feature distance
 * </ul>
 * 
 * Each view realization can be populated (i.e., EMD is computed) and a description is assigned.
 * 
 */
public class ViewRealizationSLDS<V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
    extends ViewRealization<F> {

	private final static Logger logger = LogManager.getLogger( ViewRealizationSLDS.class );
	
	/**
	 * Data-to-stochastic-language transformer
	 */
	private final Window2OrderedStochLangTransformer langTransformer;
	
	/**
	 * Pair: (Features extractor, feature distance)
	 */
	private final FeatureExtractorDistancePairVariant<F, V, D> extractorDistance;

	/**
	 * Data source
	 */
	private final BiComparisonDataSource<? extends V> biDataSource;

	/**
	 * Information if the view can actually be realized
	 */
	private final RealizabilityInfo viewRealInfo;
	
	
	public ViewRealizationSLDS(ViewRealizationMeta viewDescription, 
	    FeatureExtractorDistancePairVariant<F, V, D> extractorDistance, 
			Window2OrderedStochLangTransformer langTransformer, 
			BiComparisonDataSource<? extends V> biDataSource) {
		super(viewDescription);

		this.langTransformer = langTransformer;
		this.extractorDistance = extractorDistance;
		this.biDataSource = biDataSource;
		this.viewRealInfo = RealizabilityChecker.checkRealizability(viewDescription, extractorDistance, 
				langTransformer, biDataSource);

	}

	@Override
	public boolean isRealizable() {
		return viewRealInfo.isRealizable();
	}
	
	public RealizabilityInfo getRealizibilityInfo() {
		return viewRealInfo;
	}

	@Override
	public void populate() throws ViewDataException {
		if(viewRealInfo.isRealizable()) {
			if(!emdSol.isPresent() && viewRealInfo.isRealizable()) {
				//TODO
				try {
					emdSol = Optional.of(EMDSolver.getLPSolution(biDataSource.getDataSourceLeft().getVariantLog(), biDataSource.getDataSourceRight().getVariantLog(), 
							extractorDistance.getFeatureExtractor(), extractorDistance.getDistance(), langTransformer));
				} catch (SLDSTransformationError e) {
					logger.error("A variant transformation error occured. Cannot populate view");
					String errorMessage = "Querying data from the data source failed. " 
							+ "There has been an error during the transformation: " + e.getMessage();
					ViewDataException viewDataException = new ViewDataException(new RealizabilityInfo(RealizationProblemType.DATASOURCE_BROKEN, errorMessage));
					viewDataException.setStackTrace(e.getStackTrace());
					throw viewDataException;
				}
			}
		}
		else {
			throw new ViewDataException(viewRealInfo);
		}
	}

}
