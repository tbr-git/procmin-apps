package de.rwth.processmining.tb.core.data.variantlog.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapperImpl;

public class CategoricalLogBuildingUtil {

	public static CategoryMapper getActivityMapping(XLog xlog, XEventClassifier classifier) {
		Map<String, Integer> activity2id = new HashMap<>(100);
		ArrayList<String> id2activityTmp = new ArrayList<>();

		XLogInfo info = XLogInfoImpl.create(xlog, classifier);
		int i = 0;
		for(XEventClass c: info.getEventClasses().getClasses()) {
			String eventId = c.getId();
			activity2id.put(eventId, i);
			id2activityTmp.add(eventId);
			i++;
		}
		
		String[] id2activity = new String[id2activityTmp.size()];
		for(i = 0; i < id2activityTmp.size(); i++) {
			id2activity[i] = id2activityTmp.get(i);
		}
		
		return new CategoryMapperImpl(activity2id, id2activity);
	}
	
	public static Map<List<String>, Integer> getVariantInfoRaw(XLog log, XEventClassifier classifier) {
		final Map<List<String>, Integer> listTraces = new HashMap<List<String>, Integer>();

		for (final XTrace trace : log) {
			// filter out unmapped events
			final List<String> listTrace = trace.stream().map(e -> classifier.getClassIdentity(e))
					.collect(Collectors.toList());
			if (listTraces.containsKey(listTrace)) {
				listTraces.put(listTrace, listTraces.get(listTrace) + 1);
			} else {
				listTraces.put(listTrace, 1);
			}
		}
		return listTraces;
		
	}
}
