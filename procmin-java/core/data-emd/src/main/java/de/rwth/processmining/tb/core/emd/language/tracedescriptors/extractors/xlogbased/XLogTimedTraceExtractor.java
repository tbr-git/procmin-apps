package de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.TimeBinType;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.TimedTrace;
import de.rwth.processmining.tb.core.util.MathUtil;

public class XLogTimedTraceExtractor extends XLogTraceFeatureExtractor<TimedTrace> {
	private final static Logger logger = LogManager.getLogger( XLogTimedTraceExtractor.class );

	private final static String KEY_SOJOURN_TIME = "SojournTime";

	private final static String KEY_SERVICE_TIME = "ServiceTime";
	
	private double normAccDuration = -1;
	
	private double normSojourn = -1;
	
	private final TimeBinType timeBinType;

	public XLogTimedTraceExtractor(XEventClassifier classifier, TimeBinType timeBinType) {
		super(classifier);
		this.timeBinType = timeBinType;
		normAccDuration = 1;
	}

	@Override
	public TimedTrace getTraceDescriptor(XTrace trace) {
		if(timeBinType == TimeBinType.DURATION) {
			return getTraceDescriptorDuration(trace);
		}
		else {
			return getTraceDescriptorSojourn(trace);
		}
	}
	
	private TimedTrace getTraceDescriptorDuration(XTrace trace) {
		String[] sTrace = new String[trace.size() / 2];
		double tActivity = 0;
		double[] tStart = new double[trace.size() / 2];
		{
			int i = 0;
			for (XEvent event : trace) {
				if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
					continue;
				}
				else {
					XAttributeMap attributes = event.getAttributes();
					sTrace[i] = getClassifier().getClassIdentity(event);
					double t = ((XAttributeContinuousImpl) event.getAttributes().get("@@duration")).getValue();
					tActivity += t;
					if(useEventDescriptorInfo() && attributes.containsKey(KEY_SERVICE_TIME)) {
						tStart[i] = ((XAttributeContinuous) attributes.get(KEY_SERVICE_TIME)).getValue();
					}
					else {
						tStart[i] = tActivity / normAccDuration;
					}
					i++;
				}
			}
		}
		return new TimedTrace(sTrace, tStart);
	}

	private TimedTrace getTraceDescriptorSojourn(XTrace trace) {
		String[] sTrace = new String[trace.size() / 2];
		double tStart = -1;
		double[] tComplete = new double[trace.size() / 2];
		double t;
		{
			int i = 0;
			for (XEvent event : trace) {
				if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
					continue;
				}
				XAttributeMap attributes = event.getAttributes();
				if(tStart < 0) {
					tStart = ((XAttributeTimestamp) attributes.get("time:timestamp")).getValueMillis() / 1000;
				}
				sTrace[i] = getClassifier().getClassIdentity(event);
				if(useEventDescriptorInfo() && attributes.containsKey(KEY_SOJOURN_TIME)) {
					tComplete[i] = ((XAttributeContinuous) attributes.get(KEY_SOJOURN_TIME)).getValue();
				}
				else {
					t = ((XAttributeTimestamp) attributes.get("time:timestamp")).getValueMillis() / 1000;
					tComplete[i] = (t - tStart) / normSojourn;
				}
				i++;
			}
		}
		return new TimedTrace(sTrace, tComplete);
	}
	
	@Override
	public void init(XLog xlog, boolean forcedInit) {
		super.init(xlog);
		logger.info("Running initialization of XLogTimedTraceExtractor...");
		if(timeBinType == TimeBinType.DURATION) {
			initDuration(xlog);
		}
		else {
			initSojourn(xlog);
		}
		logger.info("Initialization done.");
	}
	
	public void initDuration(XLog xlog) {
		logger.info("Running initialization for binning based on activity duration");
		double[] arrTraceAccActDur = new double[xlog.size()];
		int i = 0;
		for (XTrace trace : xlog) {
			for (XEvent event : trace) {
				if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
					continue;
				}
				arrTraceAccActDur[i] += ((XAttributeContinuousImpl) event.getAttributes().get("@@duration")).getValue();
			}
			i++;
		}
		normAccDuration = MathUtil.getPercentile(arrTraceAccActDur, 0.975);
	}

	public void initSojourn(XLog xlog) {
		logger.info("Running initialization for binning based on activity sojourn time");
		double[] arrTraceLen = new double[xlog.size()];
		int i = 0;
		for (XTrace trace : xlog) {
			XEvent eventStart = null;
			Iterator<XEvent> itFindFirstComplete = trace.iterator();
			while(itFindFirstComplete.hasNext() &&
					XLifecycleExtension.instance().extractStandardTransition(eventStart = itFindFirstComplete.next()).compareTo(StandardModel.COMPLETE) != 0) {
			}
			//TODO Handle incomplete logs with start at the end
			XEvent eventEnd = trace.get(trace.size() - 1);
			
			double tStart = ((XAttributeTimestamp) eventStart.getAttributes().get(
					"time:timestamp")).getValueMillis() / 1000;
			double tEnd = ((XAttributeTimestamp) eventEnd.getAttributes().get(
					"time:timestamp")).getValueMillis() / 1000;
			
			arrTraceLen[i] = tEnd - tStart;
			i++;
		}
		normSojourn = MathUtil.getPercentile(arrTraceLen, 0.975);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("XLogTimedTraceExtractor");
		return builder.toString();
	}

	@Override
	public void complementTraceByDescAttributes(XTrace trace) {
		TimedTrace traceDesc = (TimedTrace) this.getTraceDescriptor(trace);
		int i = 0;
		for (XEvent event : trace) {
			if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
				continue;
			}
			else {
				double t = traceDesc.getTimes()[i];
				if(timeBinType == TimeBinType.DURATION) {
					event.getAttributes().put(KEY_SERVICE_TIME, new XAttributeContinuousImpl(KEY_SERVICE_TIME, t));
				}
				else if(timeBinType == TimeBinType.SOJOURN) {
					event.getAttributes().put(KEY_SOJOURN_TIME, new XAttributeContinuousImpl(KEY_SOJOURN_TIME, t));
				}
				else {
					logger.error("Unsupported Time Descriptor Type");
				}
				i++;
			}
		}
	}

	@Override
	public boolean isProjectionInvariant() {
		switch(timeBinType) {
			case DURATION:
				return true;
			case SOJOURN:
				return false;
			default:
				return false;
		}
	}

	@Override
	public TimedTrace getEmptyTrace() {
		return new TimedTrace(new String[] {}, new double[] {});
	}
	
	@Override
	public String getShortDescription() {
		return "CF + Cont Time";
	}

}
