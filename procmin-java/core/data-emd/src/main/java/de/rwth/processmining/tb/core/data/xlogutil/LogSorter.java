package de.rwth.processmining.tb.core.data.xlogutil;

import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;


public class LogSorter {
	
	public static XLog sortOnStartTimes(XLog xlog) {
		int nbrTraces = xlog.size();
		
		PriorityQueue<XTrace> startQueue = new PriorityQueue<XTrace>(nbrTraces, new Comparator<XTrace>() {

			@Override
			public int compare(XTrace t1, XTrace t2) {
				Date d1 = XTimeExtension.instance().extractTimestamp(t1.get(0));
				Date d2 = XTimeExtension.instance().extractTimestamp(t2.get(0));
				
				return d1.compareTo(d2);
			}
		});
		
		startQueue.addAll(xlog);
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog sortedTimeStampLog = factory.createLog();

		if(xlog.getAttributes() != null)
			sortedTimeStampLog.setAttributes(xlog.getAttributes());
		if(xlog.getExtensions() != null){
			for (XExtension extension : xlog.getExtensions())
				sortedTimeStampLog.getExtensions().add(extension);
		}
		
		XTrace t = startQueue.poll();
		while(t != null) {
			sortedTimeStampLog.add(t);
			t = startQueue.poll();
		}

		return sortedTimeStampLog;
	}

}
