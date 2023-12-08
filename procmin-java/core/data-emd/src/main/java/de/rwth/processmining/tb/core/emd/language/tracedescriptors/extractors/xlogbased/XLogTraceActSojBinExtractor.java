package de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;

import de.rwth.processmining.tb.core.data.xlogutil.statistics.XLogTimeStatistics;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.TraceSingleBin;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.logclassifier.SojournTimeBinnedClassifier;
import de.rwth.processmining.tb.core.util.histogram.BinEdgeCalculator;

public class XLogTraceActSojBinExtractor extends XLogTimeBinTraceExtractor {
	private final static Logger logger = LogManager.getLogger( XLogTraceActSojBinExtractor.class );

	public final static String KEY_SOJOURN_TIME_BINNED = "SojournTimeBinned";

	private static final Marker TMPSELECT_MARKER = MarkerManager.getMarker("TMPSELECT");

	public XLogTraceActSojBinExtractor(XEventClassifier classifier, BinEdgeCalculator binCalc) {
		super(classifier, binCalc);
	}


	@Override
	public TraceSingleBin getTraceDescriptor(XTrace trace) {
		ArrayList<String> sTrace = new ArrayList<>(trace.size());
		ArrayList<Integer> indBinned = new ArrayList<>(trace.size());
		StringBuilder builder = new StringBuilder();
		double t, t_soj, t_last = -1;
		for (XEvent event : trace) {
			if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
				continue;
			}
			else {
				XAttributeMap attributes = event.getAttributes();
				String activity = getClassifier().getClassIdentity(event);
				sTrace.add(activity);
				if(useEventDescriptorInfo() && attributes.containsKey(KEY_SOJOURN_TIME_BINNED)) {
					int binIndex = (int) ((XAttributeDiscrete) attributes.get(KEY_SOJOURN_TIME_BINNED)).getValue();
					indBinned.add(binIndex);
					t_last = ((XAttributeTimestamp) event.getAttributes().get("time:timestamp")).getValueMillis() / 1000;
				}
				else {
					t = ((XAttributeTimestamp) event.getAttributes().get("time:timestamp")).getValueMillis() / 1000;
					if(t_last < 0) {
						t_last = t;
					}
					t_soj = t - t_last;
					builder.append(", " + t_soj);
					double[] bins = this.mapBins.get(activity);
					int j = 0;
					while (t_soj > bins[j]) // Assuming that last entry is Double.POSITVE_INFINITY
					   j++;
					indBinned.add(j);
					t_last = t;
				}
			}
		}
		TraceSingleBin res = new TraceSingleBin(sTrace.toArray(new String[sTrace.size()]), 
				indBinned.stream().mapToInt(Integer::intValue).toArray());
		return res; 
	}

	@Override
	public void complementTraceByDescAttributes(XTrace trace) {
		TraceSingleBin traceDesc = this.getTraceDescriptor(trace);
		int i = 0;
		for (XEvent event : trace) {
			if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
				continue;
			}
			else {
				int t = traceDesc.getBin(i);
				event.getAttributes().put(KEY_SOJOURN_TIME_BINNED, new XAttributeDiscreteImpl(KEY_SOJOURN_TIME_BINNED, t));
				i++;
			}
		}
	}
		
	@Override
	protected Map<String, List<Double>> getTimeMap(XLog xlog) {
		return XLogTimeStatistics.getActivitySojournTimes(xlog, getClassifier());
	}


	@Override
	public void addClassifier4DescAttributes(XLog xlog) {
		super.addClassifier4DescAttributes(xlog);
		xlog.getClassifiers().add(new SojournTimeBinnedClassifier(KEY_SOJOURN_TIME_BINNED));
	}

	@Override
	public boolean isProjectionInvariant() {
		return false;
	}

	@Override
	public boolean doesLogContainInfo4InvariantProjection(XLog xlog) {
		return xlog.getClassifiers().contains(new SojournTimeBinnedClassifier(KEY_SOJOURN_TIME_BINNED));
	}

	@Override
	public TraceSingleBin getEmptyTrace() {
		return new TraceSingleBin(new String[] {}, new int[] {});
	}

	@Override
	public String getShortDescription() {
		return "CF + Sojourn T. Binned";
	}

}
