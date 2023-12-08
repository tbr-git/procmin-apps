package de.rwth.processmining.tb.core.data.comparison;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.utils.XLogBuilder;

public class WindowDiagnosticsDataLogBuilder {

	private final static Logger logger = LogManager.getLogger( WindowDiagnosticsDataLogBuilder.class );
	
	private XLog xLog;
	
	private int traceIndex;
	
	private int w_sizeL;

	private int w_sizeR;
	
	
	public WindowDiagnosticsDataLogBuilder() {
		
	}
	
	public WindowDiagnosticsDataLogBuilder addTracesFromLog(XLog xlog, int traceIndex, int w_sizeL, int w_sizeR) {
		this.xLog = xlog;
		this.traceIndex = traceIndex;
		this.w_sizeL = w_sizeL;
		this.w_sizeR = w_sizeR;
		return this;
	}
	
	public WindowDiagnosticsDataLogBuilder addTracesFromLog(XLog xlog, int traceIndex, int w_size) {
		this.xLog = xlog;
		this.traceIndex = traceIndex;
		this.w_sizeL = w_size;
		this.w_sizeR = w_size;
		return this;
	}
	
	public WindowDiagnosticsData build() {
		if(traceIndex - w_sizeL < 0) {
			logger.error("Window too big left");
			return null;
		}
		else if(traceIndex + w_sizeR > xLog.size()) {
			logger.error("Window too big right");
			return null;
		}
		else {
			XLog xlogL = XLogBuilder.newInstance().startLog("DiagLogLeft").build();
			XLog xlogR = XLogBuilder.newInstance().startLog("DiagLogRight").build();
			// traceIndex first in right window
			Stream<XTrace> streamL = IntStream.range(traceIndex - w_sizeL, traceIndex).mapToObj(xLog::get);
			Stream<XTrace> streamR = IntStream.range(traceIndex, traceIndex + w_sizeR).mapToObj(xLog::get);
			
			xlogL.addAll(streamL.collect(Collectors.toList()));
			xlogR.addAll(streamR.collect(Collectors.toList()));
			
			xlogL.getClassifiers().addAll(xLog.getClassifiers());
			xlogR.getClassifiers().addAll(xLog.getClassifiers());
			
			return new WindowDiagnosticsData(xlogL, xlogR);
		}
		
	}

}
