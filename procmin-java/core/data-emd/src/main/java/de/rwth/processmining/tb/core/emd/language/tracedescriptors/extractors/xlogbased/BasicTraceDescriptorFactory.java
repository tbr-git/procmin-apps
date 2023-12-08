package de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTrace;

public class BasicTraceDescriptorFactory extends XLogTraceFeatureExtractor<BasicTrace> {
	private final static Logger logger = LogManager.getLogger( BasicTraceDescriptorFactory.class );
	

	public BasicTraceDescriptorFactory(XEventClassifier classifier) {
		super(classifier);
	}
	
	@Override
	public BasicTrace getTraceDescriptor(XTrace trace) {
		String[] sTrace = new String[trace.size()];
		{
			int i = 0;
			for (XEvent event : trace) {
				String activity = getClassifier().getClassIdentity(event);
				sTrace[i] = activity;
				i++;
			}
		}
		BasicTrace desc = new BasicTrace(sTrace);
		return desc;
	}

	@Override
	public String toString() {
		return "BasicTraceDescriptorFactory";
	}

	@Override
	public void complementTraceByDescAttributes(XTrace trace) {
		return;
	}

	@Override
	public boolean isProjectionInvariant() {
		return true;
	}

	@Override
	public BasicTrace getEmptyTrace() {
		return new BasicTrace(new String[] {});
	}

	@Override
	public String getShortDescription() {
		return "CF";
	}

}
