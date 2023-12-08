package de.rwth.processmining.tb.core.data.xlogutil;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.XEventCondition;
import org.processmining.plugins.log.logfilters.XTraceCondition;
import org.processmining.plugins.log.logfilters.impl.EventLogFilter;

public class FilterUtil {
	
	public static XLog filterActivities(XLog log, List<?> selectedObjects, XEventClassifier classifier) {
		final Collection<String> ids = new HashSet<String>();
		selectedObjects.stream().forEach(o -> ids.add(o.toString()));

		return LogFilter.filter(null, 100, log, null,
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						return ids.contains(classifier.getClassIdentity(event));
					}
				}, 
				new XTraceCondition() {
					
					@Override
					public boolean keepTrace(XTrace trace) {
						return trace.size() > 0;
					}
				});
	}

	public static XLog keepAndProjectTracesContaining(XLog log, List<?> selectedObjects, XEventClassifier classifier) {
		final Collection<String> ids = new HashSet<String>();
		selectedObjects.stream().forEach(o -> ids.add(o.toString()));

		return LogFilter.filter(null, 100, log, null,
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						return ids.contains(classifier.getClassIdentity(event));
					}
				}, 
				new XTraceCondition() {
					
					@Override
					public boolean keepTrace(XTrace trace) {
						Set<String> uniqueEvents = new HashSet<>(selectedObjects.size());
						for(XEvent e : trace) {
							uniqueEvents.add(classifier.getClassIdentity(e));
						}
						
						return uniqueEvents.size() == selectedObjects.size();
						
					}
				});
	}
	
	public static XLog filterLifecycleStart(XLog log) {
		String[] selected = {XLifecycleExtension.StandardModel.COMPLETE.getEncoding()};
		EventLogFilter filter = new EventLogFilter();
		return filter.filterWithClassifier(null, log, XLogInfoImpl.LIFECYCLE_TRANSITION_CLASSIFIER, selected);
	}
	

}
