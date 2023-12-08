package de.rwth.processmining.tb.core.data.variantlog.timed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogImpl;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.data.variantlog.util.CategoricalLogBuildingUtil;
import de.rwth.processmining.tb.core.data.variantlog.util.LogBuildingException;
import de.rwth.processmining.tb.core.data.xlogutil.statistics.XLogTimeStatistics;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.TimeBinType;
import de.rwth.processmining.tb.core.util.histogram.BinEdgeCalculator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Builder that instantiates a categorical variant with additional time bins.
 * 
 * Example usage:
 * <pre>{@code
 * CVariantTBBuilder builder = new CVariantTBBuilder();
 * builder.setBinCalculationMethod(new KMeansEdgeCalculator(3))
 * 	.setClassifier(classifier)
 * 	.setReferenceLog(joinedLog)
 * 	.setTimeBinType(TimeBinType.SOJOURN);
 * }</pre>
 * @author brockhoff
 *
 */
public class CVariantTBBuilder extends CVariantLogFactory<CVariantTimeBinned> {
	private final static Logger logger = LogManager.getLogger( CVariantTBBuilder.class );
	

	/**	
	 * Time bin type (e.g., sojourn time bining, or service time binning
	 */
	private TimeBinType timeBinType = TimeBinType.SOJOURN;
	
	/**
	 * Reference log. Will be used to measure the time distribution and derive bins from.
	 */
	private XLog referenceLog;
	
	/**
	 * Bin calculation method.
	 */
	private BinEdgeCalculator binCalculationMethod;
	
	/**
	 * Map that maps duration to a bin
	 */
	private Map<String, double[]> mapBins;
	
	public CVariantTBBuilder() {
		this.referenceLog = null;
		this.binCalculationMethod = null;
		this.mapBins = null;
	}

	@Override
	public CVariantLog<CVariantTimeBinned> build() throws LogBuildingException {
		// Standard activity arguments
		if(this.log == null) {
			logger.error("No log given! Cannot build variant log!");
			throw new RuntimeException("Log is null");
		}
		if(this.classifier == null) {
			logger.error("No classifier given! Cannot build variant log!");
			throw new RuntimeException("Classifier is null");
		}
		// Time specific arguments
		if(this.mapBins == null) {
			if(referenceLog == null) {
				logger.error("Neither a reference log nor a bin map is provided. Terminating variant log creation.");
				throw new LogBuildingException("No reference log and bin map provided");
			}
			if(binCalculationMethod == null) {
				logger.error("No bin edge calculation method provided (and not bin map). Terminating variant log creation.");
				throw new LogBuildingException("No bin edge calculation method provided. No bin map provided");
			}
			// Init the activity to bin edges map
			this.mapBins = calculateBins(this.referenceLog, this.classifier, this.timeBinType, this.binCalculationMethod);
		}

		// Init the activity to category mapping if necessary
		String[] id2activity;
		if(this.catMapper == null) {
			// Get activity mappings
			this.catMapper = CategoricalLogBuildingUtil.getActivityMapping(log, classifier);
		}
		
		// Calculate variants	
		ArrayList<CVariantTimeBinned> variants = getVariants(log, classifier, catMapper , this.mapBins, this.timeBinType);
		
		return new CVariantLogImpl<CVariantTimeBinned>(classifier, catMapper, variants);
	}
	
	
	
	private static ArrayList<CVariantTimeBinned> getVariants(XLog log, XEventClassifier classifier, 
			CategoryMapper catMapper, Map<String, double[]> mapBins, TimeBinType timeBinType) {
		
		TObjectIntHashMap<CVariantTimeBinned> mapVariants = new TObjectIntHashMap<>();
	
		int invalidTraceCounter = 0;
		// Extract variants from log and count
		for(XTrace t : log) {
			CVariantTimeBinned variant = getVariantFromTrace(t, classifier, catMapper, mapBins, timeBinType);
			if(variant == null) {
				invalidTraceCounter++;
				continue;
			}
			// Insert or count
			mapVariants.adjustOrPutValue(variant, 1, 1);
		}
		if(invalidTraceCounter > 0) {
			logger.error("Could not transform {} traces!", invalidTraceCounter);
		}
		
		// Generate variant list with support
		ArrayList<CVariantTimeBinned> variantList = new ArrayList<>(mapVariants.size());
		for(TObjectIntIterator<CVariantTimeBinned> it = mapVariants.iterator(); it.hasNext(); ) {
			it.advance();
			CVariantTimeBinned v = it.key();
			v.setSupport(it.value());
			variantList.add(v);
		}
		
		return variantList;

	}

	private static CVariantTimeBinned getVariantFromTrace(XTrace trace, XEventClassifier classifier, 
			CategoryMapper catMapper, Map<String, double[]> mapBins, TimeBinType timeBinType) {
		switch(timeBinType) {
		case DURATION:
			return getVariantFromTraceService(trace, classifier, catMapper, mapBins);
		case SOJOURN:
			return getVariantFromTraceSojourn(trace, classifier, catMapper, mapBins);
		default:
			return null;
		}
	}

	private static CVariantTimeBinned getVariantFromTraceService(XTrace trace, XEventClassifier classifier, 
			CategoryMapper catMapper, Map<String, double[]> mapBins) {
		// Categories for the trace's activities
		int[] traceCategories = new int[trace.size()];
		// Bins for the trace's times
		int[] traceBins = new int[trace.size()];
		// Bin lookup array reference
		double[] binLookup;
		
		int i = 0;
		for (XEvent event : trace) {
			// Only consider complete timestamps
			if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
				continue;
			}
			else {
				String activity = classifier.getClassIdentity(event);
				traceCategories[i] = catMapper.getCategory4Activity(activity);
				binLookup = mapBins.get(activity);

				double t = ((XAttributeContinuousImpl) event.getAttributes().get("@@duration")).getValue();
				int j = 0;
				while (t > binLookup[j]) // Assuming that last entry is Double.POSITVE_INFINITY
				   j++;
				traceBins[i] = j;
			}
			i++;
		}

		// If there are other lifecyle transitions than "complete"
		if(i < traceCategories.length) {
			traceCategories = Arrays.copyOf(traceCategories, i);
			traceBins = Arrays.copyOf(traceBins, i);
		}
		
		return new CVariantTBImpl(traceCategories, traceBins, 1, TimeBinType.SOJOURN);

	}

	private static CVariantTimeBinned getVariantFromTraceSojourn(XTrace trace, XEventClassifier classifier, 
			CategoryMapper catMapper, Map<String, double[]> mapBins) {

		// Categories for the trace's activities
		int[] traceCategories = new int[trace.size()];
		// Bins for the trace's times
		int[] traceBins = new int[trace.size()];
		// Bin lookup array reference
		double[] binLookup;

		// Sojourn computation help variables
		double t, t_soj, t_last = -1;
		int i = 0;
		for (XEvent event : trace) {
			// For sojourn times, only complete events count
			if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
				continue;
			}
			else {
				String activity = classifier.getClassIdentity(event);
				traceCategories[i] = catMapper.getCategory4Activity(activity);
				binLookup = mapBins.get(activity);

				t = ((XAttributeTimestamp) event.getAttributes().get("time:timestamp")).getValueMillis() / 1000;
				if(t_last < 0) {
					t_last = t;
				}
				t_soj = t - t_last;
				int j = 0;
				while (t_soj > binLookup[j]) // Assuming that last entry is Double.POSITVE_INFINITY
				   j++;
				traceBins[i] = j;
				t_last = t;
				i++;
			}
		}
		
		// If there are other lifecyle transitions than "complete"
		if(i < traceCategories.length) {
			traceCategories = Arrays.copyOf(traceCategories, i);
			traceBins = Arrays.copyOf(traceBins, i);
		}
		
		return new CVariantTBImpl(traceCategories, traceBins, 1, TimeBinType.SOJOURN);
	}


	
	public static Map<String, double[]> calculateBins(XLog xlog, XEventClassifier classifier, 
			TimeBinType timeBinType, BinEdgeCalculator binCalculationMethod) {
		// Extract times
		Map<String, List<Double>> mapTimes = null;
		switch(timeBinType) {
			case DURATION:
				mapTimes = XLogTimeStatistics.getActivityServiceTimes(xlog, classifier);
				break;
			case SOJOURN:
				mapTimes = XLogTimeStatistics.getActivitySojournTimes(xlog, classifier);
				break;
			default:
				// Default to Sojourn time
				logger.error("Unknown bin type. Defaulting to sojourn time binning!");
				mapTimes = XLogTimeStatistics.getActivitySojournTimes(xlog, classifier);
				break;
		}
		
		Map<String, double[]> mapBins = new HashMap<>();
		// Derive bins for each activity
		for (Map.Entry<String, List<Double>> entry : mapTimes.entrySet()) {
			List<Double> l = entry.getValue();
			double[] bins = binCalculationMethod.calculateBinEdges(l);
			mapBins.put(entry.getKey(), bins);
		}
		return mapBins;
	}

	public TimeBinType getTimeBinType() {
		return timeBinType;
	}


	public CVariantTBBuilder setTimeBinType(TimeBinType timeBinType) {
		this.timeBinType = timeBinType;
		return this;
	}


	public XLog getReferenceLog() {
		return referenceLog;
	}


	public CVariantTBBuilder setReferenceLog(XLog referenceLog) {
		this.referenceLog = referenceLog;
		return this;
	}


	public BinEdgeCalculator getBinCalculationMethod() {
		return binCalculationMethod;
	}

	public CVariantTBBuilder setBinCalculationMethod(BinEdgeCalculator binCalculationMethod) {
		this.binCalculationMethod = binCalculationMethod;
		return this;
	}

	public Map<String, double[]> getMapBins() {
		return mapBins;
	}

	public CVariantTBBuilder setMapBins(Map<String, double[]> mapBins) {
		this.mapBins = mapBins;
		return this;
	}

	@Override
	public CVariantTBBuilder setClassifier(XEventClassifier classifier) {
		super.setClassifier(classifier);
		return this;
	}

	@Override
	public CVariantTBBuilder setLog(XLog log) {
		super.setLog(log);
		return this;
	}

	@Override
	public CVariantTBBuilder setCategoryMapper(CategoryMapper catMapper) {
		super.setCategoryMapper(catMapper);
		return this;
	}
	
	

}
