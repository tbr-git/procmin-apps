package de.rwth.processmining.tb.core.data.xlogutil.statistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;

public class XLogTimeStatistics {
	private final static Logger logger = LogManager.getLogger( XLogTimeStatistics.class );
	
	public static Map<String, List<Double>> getActivitySojournTimes(XLog xlog, XEventClassifier classifier) {
		logger.info("Measuring activity sojourn times...");
		HashMap<String, List<Double>> mapTimes = new HashMap<String, List<Double>>();
		for (XTrace trace : xlog) {
			double t_cur = -1;
			for(XEvent event : trace) {
				// Skip non-complete events
				if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
					continue;
				}
				// Init last complete event when encountering the first one 
				if(t_cur < 0) {
					t_cur = ((XAttributeTimestamp) event.getAttributes().get("time:timestamp")).getValueMillis() / 1000;
				}
				String activity = classifier.getClassIdentity(event);
				// Add activity to sojourn time map
				if (!mapTimes.containsKey(activity)) {
					mapTimes.put(activity, new LinkedList<Double>());				
				}
				// Measure sojourn time (first complete event will always have time 0)
				double t = ((XAttributeTimestamp) event.getAttributes().get("time:timestamp")).getValueMillis() / 1000;
				mapTimes.get(activity).add(t - t_cur);
				t_cur = t;
			}
		}
		
		return mapTimes;
	}
	
	public static Map<String, List<Double>> getActivityServiceTimes(XLog xlog, XEventClassifier classifier) {
		HashMap<String, List<Double>> mapTimes = new HashMap<String, List<Double>>();
		double t;
		for (XTrace trace : xlog) {
			for (XEvent event : trace) {
				String activity = classifier.getClassIdentity(event);
				if (!mapTimes.containsKey(activity)) {
					mapTimes.put(activity, new LinkedList<Double>());				
				}
				// TODO that is not robust
				// Assumes that service times are already there (PM4Py)
				t = ((XAttributeContinuousImpl) event.getAttributes().get("@@duration")).getValue();
				mapTimes.get(activity).add(t);
			}
		}
		return mapTimes;
	}

}
