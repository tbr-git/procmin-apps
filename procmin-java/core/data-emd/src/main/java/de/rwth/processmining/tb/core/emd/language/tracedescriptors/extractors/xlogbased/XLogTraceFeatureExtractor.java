package de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.strategy.HashingStrategy;

/**
 * Abstract feature extractor class providing common functionality required to extract <b>a single</b> 
 * feature from a {@link XTrace}.
 * 
 * @param <F> Type of the extracted feature
 */
public abstract class XLogTraceFeatureExtractor<F extends TraceDescriptor> {
	
  /**
   * Can information in the trace/event be reused?
   */
	private boolean reuseEventDescriptorInfo = true;

	/**
	 * Hashing strategy for the extracted feature.
	 * Required by {@link TObjectFloatMap}.
	 */
	private HashingStrategy<F> hashStrat;

	
	/**
	 * Classifier used to map events
	 */
	private final XEventClassifier classifier;
	
	/**
	 * Constructor
	 * @param classifier Event classifier
	 */
	public XLogTraceFeatureExtractor(XEventClassifier classifier) {
		this.classifier = classifier;
		this.hashStrat = new HashingStrategy<F>() {
			private static final long serialVersionUID = 1L;

			public int computeHashCode(F object) {
			  // Just use the standard objects hash code.
			  // Note that the TraceDescriptor class enforces (abstract) its
			  // implementing subclasses to implement the method properly.
				return object.hashCode();
			}

			public boolean equals(F o1, F o2) {
				return o1.equals(o2);
			}
		};
	}
	
	/**
	 * Initialize the extractor on a log.
	 * 
	 * For example, compute bins.
	 * @param xlog Log to initialize the extractor on.
	 * @param forcedInit Even if information is already there, override it.
	 */
  public void init(XLog xlog, boolean forcedInit) {
		
	}

	/**
	 * Initialize the extractor on a log.
	 * 
	 * For example, compute bins. If information is already stored, do nothing.
	 * @param xlog Log to initialize the extractor on.
	 */
	public final void init(XLog xlog) {
		init(xlog, false);
	}
	
	/**
	 * Extract a feature and add it to the feature map (incrementing its counter)
	 * 
	 * @param trace Trace to extract the feature from
	 * @param features Feature map to add the feature to
	 * @return True iff successful
	 */
  public boolean addTraceDescriptor(XTrace trace, TObjectFloatMap<F> features) {
    F f = getTraceDescriptor(trace);
    features.adjustOrPutValue(f, 1, 1);
    return true;
  }

	/**
	 * Extract a feature and add it to the feature map (incrementing its counter by the provided
	 * scale Constant).
	 * 
	 * @param trace Trace to extract the feature from
	 * @param features Feature map to add the feature to
	 * @param scaleConstant Scale up the feature count by this value
	 * @return True iff successful
	 */
  public boolean addTraceDescriptor(XTrace trace, TObjectFloatMap<F> features, float scaleConstant) {
    F f = getTraceDescriptor(trace);
    features.adjustOrPutValue(f, scaleConstant, scaleConstant);
    return true;
  }

  /**
   * Extract and return the feature.
   * 
   * @param trace Trace from which <b>the</b> feature is extracted
   * @return The extracted feature
   */
	public abstract F getTraceDescriptor(XTrace trace);

	/**
	 * Get a hashing strategy for the features
	 * @return Hashing strategy
	 */
	public HashingStrategy<F> getHashingStrat() {
	  return this.hashStrat;
	}
	
	/**
	 * Complement a log by information that eases the feature extraction.
	 * @param xlog Log
	 */
	public void complementLogByDescAttributes(XLog xlog) {
		for(XTrace trace : xlog) {
			complementTraceByDescAttributes(trace);
		}
		addClassifier4DescAttributes(xlog);
	}

	/**
	 * Complement (add information to) a trace that eases the feature extraction.
	 * 
	 * For example, for binned features, the bin index might be added.
	 * @param trace
	 */
	public abstract void complementTraceByDescAttributes(XTrace trace);
	
	public void addClassifier4DescAttributes(XLog xlog) {
		return;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}
	

	
	public boolean useEventDescriptorInfo() {
		return reuseEventDescriptorInfo;
	}
	
	public void enableUsingEventDescriptorInfoUsage() {
		reuseEventDescriptorInfo = true;
	}

	public void disableUsingEventDescriptorInfoUsage() {
		reuseEventDescriptorInfo = false;
	}
	
	public abstract boolean isProjectionInvariant();
	
	public boolean doesLogContainInfo4InvariantProjection(XLog xlog) {
		return isProjectionInvariant();
	}
	
	public abstract F getEmptyTrace();

	public abstract String getShortDescription();
	
}
