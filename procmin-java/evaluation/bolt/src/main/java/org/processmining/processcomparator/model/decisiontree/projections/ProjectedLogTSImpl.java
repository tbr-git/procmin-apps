package org.processmining.processcomparator.model.decisiontree.projections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.datadiscovery.ProjectedLog;
import org.processmining.datadiscovery.ProjectedTrace;

public class ProjectedLogTSImpl implements ProjectedLog {

	private XLog log;
	private List<ProjectedTrace> traces;

	public ProjectedLogTSImpl(XLog log, List<String> attributeList) {
		this.log = log;
		traces = new ArrayList<ProjectedTrace>();		
		for (XTrace t : log)
			traces.add(new ProjectedTraceTSImpl(t, attributeList));
	}

	public Iterator<ProjectedTrace> iterator() {
		return traces.iterator();
	}

	public Set<String> getAttributes() {
		return log.getAttributes().keySet();
	}

	public Object getInitialValue(String attributeName) {
		return log.getAttributes().get(attributeName);
	}
}
