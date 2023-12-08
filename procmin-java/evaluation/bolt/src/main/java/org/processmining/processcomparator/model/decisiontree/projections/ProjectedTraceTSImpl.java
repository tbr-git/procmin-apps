package org.processmining.processcomparator.model.decisiontree.projections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.datadiscovery.ProjectedEvent;
import org.processmining.datadiscovery.ProjectedTrace;

public class ProjectedTraceTSImpl implements ProjectedTrace {

	private XTrace trace;
	private List<ProjectedEvent> events;
	
	public ProjectedTraceTSImpl(XTrace trace, List<String> attributeList) {
		this.trace = trace;
		this.events = new ArrayList<ProjectedEvent>();
		for(XEvent e : trace)
			events.add(new ProjectedEventTSImpl(e, trace, attributeList));
	}

	public Iterator<ProjectedEvent> iterator() {
		return events.iterator();
	}

	public Object getAttributeValue(String attributeName) {
		return trace.getAttributes().get(attributeName);
	}

	public Set<String> getAttributes() {
		return trace.getAttributes().keySet();
	}

}
