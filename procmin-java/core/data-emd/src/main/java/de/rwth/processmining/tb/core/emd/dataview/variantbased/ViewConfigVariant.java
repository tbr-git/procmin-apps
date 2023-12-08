package de.rwth.processmining.tb.core.emd.dataview.variantbased;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.ViewConfig;
import de.rwth.processmining.tb.core.emd.dataview.ViewIdentifier;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;

/**
 * Class that can be used to create a view on two variant-based event logs using EMD.
 * 
 * If fully configures this view by specifying:
 * <ul>
 * <li> A stochastic language transformer (how to transform an input event log into a stochastic language)
 * <li> How to transform an input variant into a feature </li>
 * <li> Ground distance for the features </li>
 * </ul>
 * 
 * @author brockhoff
 *
 */
public class ViewConfigVariant<V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
    extends ViewConfig {

	private final static Logger logger = LogManager.getLogger( ViewConfigVariant.class );
	
	/**
	 * Descriptor - Distance Pair
	 * How to transform a trace variant into an EMD input feature
	 * How to measure the distance between two features
	 */
	private FeatureExtractorDistancePairVariant<F, V, D> extractorDistance;
	
	/**
	 * Constructor
	 * @param langTransformer
	 * @param descDistPair
	 * @param viewIdentifier
	 */
	public ViewConfigVariant(Window2OrderedStochLangTransformer langTransformer, 
	    FeatureExtractorDistancePairVariant<F, V, D> extractorDistance, 
			ViewIdentifier viewIdentifier) {
		super(langTransformer, viewIdentifier);
		this.extractorDistance = extractorDistance;
	}
	
	/**
	 * Copy constructor
	 * @param viewConfig
	 */
	public ViewConfigVariant(ViewConfigVariant<V, F, D> viewConfig) {
	  super(viewConfig);
    this.extractorDistance = viewConfig.getExtractorDistancePair();
	}

	////////////////////////////////////////////////////////////////////////////////
	// Getter
	////////////////////////////////////////////////////////////////////////////////
	public FeatureExtractorDistancePairVariant<F, V, D> getExtractorDistancePair() {
		return extractorDistance;
	}

}
