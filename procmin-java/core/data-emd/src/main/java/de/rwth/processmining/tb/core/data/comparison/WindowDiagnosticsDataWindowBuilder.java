package de.rwth.processmining.tb.core.data.comparison;

import java.util.Collection;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.utils.XLogBuilder;

public class WindowDiagnosticsDataWindowBuilder {

	private Collection<XTrace> tracesL;

	private Collection<XTrace> tracesR;
	
	private List<XEventClassifier> classifiers;
	
	public WindowDiagnosticsDataWindowBuilder() {
		tracesL = null;
		tracesR = null;
		classifiers = null;
		
	}
	
	public WindowDiagnosticsDataWindowBuilder addTracesLeft(Collection<XTrace> tracesL) {
		this.tracesL = tracesL;
		return this;
	}
	
	public WindowDiagnosticsDataWindowBuilder addTracesRight(Collection<XTrace> tracesR) {
		this.tracesR = tracesR;
		return this;
	}
	
	public WindowDiagnosticsDataWindowBuilder setClassifiers(List<XEventClassifier> classifiers) {
		return this;
	}
	
	public WindowDiagnosticsData build() {
		if(tracesL == null) {
			throw new RuntimeException("Traces left not set");
		}
		if(tracesR == null) {
			throw new RuntimeException("Traces right not set");
		}

		XLog xlogL = XLogBuilder.newInstance().startLog("DiagLogLeft").build();
		XLog xlogR = XLogBuilder.newInstance().startLog("DiagLogRight").build();
		
		xlogL.addAll(tracesL);
		xlogR.addAll(tracesR);
		
		if(classifiers != null) {
			xlogL.getClassifiers().addAll(classifiers);
			xlogR.getClassifiers().addAll(classifiers);
		}
		else {
			// TODO
		}
		
		return new WindowDiagnosticsData(xlogL, xlogR);
		
	}


}