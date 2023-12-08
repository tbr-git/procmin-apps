package de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;

import de.rwth.processmining.tb.core.data.xlogutil.statistics.XLogTimeStatistics;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.TraceSingleBin;
import de.rwth.processmining.tb.core.util.histogram.BinEdgeCalculator;

public class XLogTraceActServBinExtractor extends XLogTimeBinTraceExtractor {
	private final static Logger logger = LogManager.getLogger( XLogTraceActServBinExtractor.class );

	private final static String KEY_SERVICE_TIME_BINNED = "ServiceTimeBinned";

	public XLogTraceActServBinExtractor(XEventClassifier classifier, BinEdgeCalculator binCalc) {
		super(classifier, binCalc);
	}

	@Override
	public TraceSingleBin getTraceDescriptor(XTrace trace) {
//		String[] sTrace = new String[trace.size() / 2];
//		int[] indBinned = new int[trace.size() / 2];
		List<String> lTrace = new LinkedList<>();
		List<Integer> lBins = new LinkedList<>();
		{
			for (XEvent event : trace) {
				if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
					continue;
				}
				else {
					XAttributeMap attributes = event.getAttributes();
					String activity = getClassifier().getClassIdentity(event);
					lTrace.add(activity); 
//					sTrace[i] = activity;
					if(useEventDescriptorInfo() && attributes.containsKey(KEY_SERVICE_TIME_BINNED)) {
						int binIndex = (int) ((XAttributeDiscrete) attributes.get(KEY_SERVICE_TIME_BINNED)).getValue();
//						indBinned[i] = binIndex;
						lBins.add(binIndex);
					}
					else {
						double t = ((XAttributeContinuousImpl) event.getAttributes().get("@@duration")).getValue();
						double[] bins = this.mapBins.get(activity);
						int j = 0;
						while (t > bins[j]) // Assuming that last entry is Double.POSITVE_INFINITY
						   j++;
//						indBinned[i] = j;
						lBins.add(j);
					}
				}
			}
		}
//		TraceDescBinnedActDur res = new TraceDescBinnedActDur(sTrace, indBinned);
		String[] sTrace = new String[lTrace.size()];
		lTrace.toArray(sTrace);
		int[] indBinned = new int[lBins.size()];
		int i = 0;
		for(int ind : lBins) {
			indBinned[i] = ind;
			i++;
		}
		TraceSingleBin res = new TraceSingleBin(sTrace, indBinned);
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
				event.getAttributes().put(KEY_SERVICE_TIME_BINNED, new XAttributeDiscreteImpl(KEY_SERVICE_TIME_BINNED, t));
				i++;
			}
		}
	}

	@Override
	protected Map<String, List<Double>> getTimeMap(XLog xlog) {
		return XLogTimeStatistics.getActivityServiceTimes(xlog, getClassifier());
	}

	@Override
	public boolean isProjectionInvariant() {
		return true;
	}

	@Override
	public TraceSingleBin getEmptyTrace() {
		return new TraceSingleBin(new String[] {}, new int[] {});
	}

	@Override
	public String getShortDescription() {
		return "CF + Service T. Binned";
	}
}
