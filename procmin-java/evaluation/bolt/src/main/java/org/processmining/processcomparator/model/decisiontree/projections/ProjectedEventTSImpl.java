package org.processmining.processcomparator.model.decisiontree.projections;

import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.datadiscovery.ProjectedEvent;
import org.processmining.processcomparator.algorithms.EventToStateTranslator;

public class ProjectedEventTSImpl implements ProjectedEvent {
	/**
	 * An event belongs to a trace, I need it to rebuild the state.
	 */
	private XEvent event;
	private XTrace trace;
	private List<String> attributeList;

	public ProjectedEventTSImpl(XEvent event, XTrace trace, List<String> attributeList) {
		this.event = event;
		this.trace = trace;
		this.attributeList = attributeList;
	}

	public Object getActivity() {
		return EventToStateTranslator.getStatefromEvent(event, trace);
	}

	public Object getAttributeValue(String attributeName) {
		XAttribute attribute = null;
		if (attributeList.contains(attributeName))
			attribute = event.getAttributes().get(attributeName);
	
		if(attribute == null)
			return null;
		
		if (attribute instanceof XAttributeLiteral)
			return ((XAttributeLiteral) attribute).getValue();
		else if (attribute instanceof XAttributeContinuous)
			return ((XAttributeContinuous) attribute).getValue();
		else if (attribute instanceof XAttributeDiscrete)
			return ((XAttributeDiscrete) attribute).getValue();
		else if (attribute instanceof XAttributeBoolean)
			return ((XAttributeBoolean) attribute).getValue();
		else if (attribute instanceof XAttributeTimestamp)
			return ((XAttributeTimestamp) attribute).getValue();
			
		return null;
	}

	public Set<String> getAttributes() {
		return event.getAttributes().keySet();
	}
	/*
	 * public Object getActivity() { return XUtils.getConceptName(event); }
	 * 
	 * public Object getAttributeValue(String attributeName) { XAttribute
	 * attribute = event.getAttributes().get(attributeName); if (attribute !=
	 * null) { return XUtils.getAttributeValue(attribute); } else { return null;
	 * } }
	 */

}
