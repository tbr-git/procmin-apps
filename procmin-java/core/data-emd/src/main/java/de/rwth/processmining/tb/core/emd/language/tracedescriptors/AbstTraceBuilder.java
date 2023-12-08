package de.rwth.processmining.tb.core.emd.language.tracedescriptors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapperExtensible;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.AbstTraceCC;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.XLogTraceFeatureExtractor;

public class AbstTraceBuilder extends XLogTraceFeatureExtractor<AbstTraceCC> {

	/**
	 * Logger
	 */
	private final static Logger logger = LogManager.getLogger( AbstTraceBuilder.class );

	/**
	 * Fallback mapper that is initialized on the fly.
	 */
	private CategoryMapperExtensible cmFallback;
	
	public AbstTraceBuilder(XEventClassifier classifier) {
		super(classifier);
		cmFallback = new CategoryMapperExtensible();
	}


	@Override
	public AbstTraceCC getTraceDescriptor(XTrace trace) {
		int[] catTrace = new int[trace.size()];
		int[][] abstractions = new int[trace.size()][];
		{
			int i = 0;
			for (XEvent event : trace) {
				String activity = getClassifier().getClassIdentity(event);
				catTrace[i] = cmFallback.getCategory4ActivityOrAdd(activity);
				i++;
			}
		}
		AbstTraceCC desc = new AbstTraceCC(catTrace, abstractions, this.cmFallback);
		return desc;
	}
	
	@Override
	public void complementTraceByDescAttributes(XTrace trace) {
	}

	@Override
	public boolean isProjectionInvariant() {
		return true;
	}

	@Override
	public AbstTraceCC getEmptyTrace() {
		return new AbstTraceCC(new int[0], null, null);
	}

	@Override
	public String getShortDescription() {
		return "ACF";
	}
}
