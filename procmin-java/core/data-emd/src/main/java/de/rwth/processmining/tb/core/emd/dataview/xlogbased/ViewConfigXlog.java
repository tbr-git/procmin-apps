package de.rwth.processmining.tb.core.emd.dataview.xlogbased;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.emd.dataview.DescriptorDistancePair;
import de.rwth.processmining.tb.core.emd.dataview.ViewConfig;
import de.rwth.processmining.tb.core.emd.dataview.ViewIdentifier;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.XLogTraceFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;

/**
 * Class that can be used to create a view on two event logs using EMD.
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
public class ViewConfigXlog<F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> extends ViewConfig {

	private final static Logger logger = LogManager.getLogger( ViewConfigXlog.class );
	
	/**
	 * Descriptor - Distance Pair
	 * How to transform a trace variant into an EMD input feature
	 * How to measure the distance between two features
	 */
	private DescriptorDistancePair<F, D> extractorDistance;
	
	/**
	 * Constructor
	 * @param langTransformer
	 * @param descDistPair
	 * @param viewIdentifier
	 */
	public ViewConfigXlog(Window2OrderedStochLangTransformer langTransformer, 
	    DescriptorDistancePair<F, D> extractorDistance, 
			ViewIdentifier viewIdentifier) {
		super(langTransformer, viewIdentifier);
		this.extractorDistance = extractorDistance;
	}
	
	/**
	 * Copy constructor
	 * @param viewConfig
	 */
	public ViewConfigXlog(ViewConfigXlog<F, D> viewConfig) {
	  super(viewConfig);
    this.extractorDistance = viewConfig.getExtractorDistancePair();
	}

  public boolean isConsistent4LogProjection(XLog xlog) {
		XLogTraceFeatureExtractor<F> descFactory = extractorDistance.getDescriptorFactory();
		return descFactory.isProjectionInvariant() || descFactory.doesLogContainInfo4InvariantProjection(xlog);
	}

	@Override
	public String toString() {
		return "View Config (" + getViewIdentifier() +  "): " + getExtractorDistancePair().getShortDescription() + 
		    " with transformer: " + getLangTransformer().getShortDescription();
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// Getter
	////////////////////////////////////////////////////////////////////////////////
	public DescriptorDistancePair<F, D> getExtractorDistancePair() {
		return extractorDistance;
	}

}
