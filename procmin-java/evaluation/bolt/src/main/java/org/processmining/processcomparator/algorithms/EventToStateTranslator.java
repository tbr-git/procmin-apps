package org.processmining.processcomparator.algorithms;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.plugins.transitionsystem.miner.TSMinerPayload;
import org.processmining.plugins.transitionsystem.miner.TSMinerPayloadHandler;
import org.processmining.plugins.transitionsystem.miner.TSMinerTransitionSystem;
import org.processmining.processcomparator.model.TsSettingsObject;

public class EventToStateTranslator {

	private static TsSettingsObject settings;

	private static TSMinerTransitionSystem ts;

	public static void setTS(TSMinerTransitionSystem ts) {
		EventToStateTranslator.ts = ts;
	}

	public static void setSettings(TsSettingsObject settings) {
		EventToStateTranslator.settings = settings;
	}

	public static State getStatefromEvent(XEvent event, XTrace trace) {
		TSMinerPayloadHandler payloadHandler = new TSMinerPayloadHandler(settings.getObject());
		TSMinerPayload toPayload = (TSMinerPayload) payloadHandler.getTargetStateIdentifier(trace,
				trace.indexOf(event));
		return ts.getNode(toPayload);
	}

}
