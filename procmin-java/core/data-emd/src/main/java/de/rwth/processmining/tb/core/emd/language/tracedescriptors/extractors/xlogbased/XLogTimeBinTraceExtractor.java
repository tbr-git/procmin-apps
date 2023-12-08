package de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.TraceSingleBin;
import de.rwth.processmining.tb.core.util.histogram.BinEdgeCalculator;

public abstract class XLogTimeBinTraceExtractor extends XLogTraceFeatureExtractor<TraceSingleBin> {
	private final static Logger logger = LogManager.getLogger( XLogTimeBinTraceExtractor.class );
	private static final Marker TMPSELECT_MARKER = MarkerManager.getMarker("TMPSELECT");

	private boolean needInitialization;
	/**
	 * Map that maps duration to a bin
	 */
	protected HashMap<String, double[]> mapBins;

	private final BinEdgeCalculator binCalc;
	
	public XLogTimeBinTraceExtractor(XEventClassifier classifier, BinEdgeCalculator binCalc) {
		super(classifier);
		needInitialization = true;
		this.mapBins = new HashMap<String, double[]>();
		this.binCalc = binCalc;
	}

	abstract protected Map<String, List<Double>> getTimeMap(XLog xlog);
	
	@Override
	public void init(XLog xlog, boolean forcedInit) {
		if(needInitialization || forcedInit) {
			logger.info("Running initialization of XLogTimedTraceExtractor...");
			Map<String, List<Double>> mapTimes = null;
			mapTimes = getTimeMap(xlog);
			
			for (Map.Entry<String, List<Double>> entry : mapTimes.entrySet()) {
				List<Double> l = entry.getValue();
				double[] bins = binCalc.calculateBinEdges(l);
				logger.debug(entry.getKey() + ": " + Arrays.toString(bins));
				this.mapBins.put(entry.getKey(), bins);
			}
			logger.debug(TMPSELECT_MARKER, "Derived bins: " + this.toString());
			logger.info("Initialization done.");
			needInitialization = false;
		}
	}

	
	public int getNbrBuckets() {
		return this.mapBins.size();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("XLogTimedTraceExtractor");
		if(mapBins.size() > 0) {
			builder.append(mapBins.keySet().parallelStream().map(key -> key + "=" + ArrayUtils.toString(mapBins.get(key))).collect(Collectors.joining(",", "[", "]")));
		}
		return builder.toString();
	}	

}
