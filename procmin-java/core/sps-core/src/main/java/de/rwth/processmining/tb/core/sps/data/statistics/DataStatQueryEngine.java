package de.rwth.processmining.tb.core.sps.data.statistics;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.statistics.ActivityOccurencePosition;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraph;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;

public class DataStatQueryEngine<V extends CVariant> {
	private final static Logger logger = LogManager.getLogger( DataStatQueryEngine.class );
  
  private final Map<BitSet, Pair<Double, Double>> activitySetContainedProb;

	/**
	 * Event data to is going to be compared.
	 */
	private final BiComparisonDataSource<V> data;
	
	/**
	 * Handle to a HFDD Graph.
	 * Some information might already be retrievable from the graph.
	 */
	private Optional<HFDDGraph> hfddGraph;
	
	/**
	 * Average position of each activity's first occurrence.
	 */
	private Optional<ActivityOccurencePosition> avgFirstActivityOccurrences;

  public DataStatQueryEngine(BiComparisonDataSource<V> data) {
    this.activitySetContainedProb = new HashMap<>();
    this.data = data;
    this.avgFirstActivityOccurrences = Optional.empty();
  }
  
  public void setHFDDGraph(HFDDGraph hfddGraph) {
    this.hfddGraph = Optional.of(hfddGraph);
  }
  
  public Pair<Double, Double> getActivitySetOccurrenceProbability(BitSet activities) throws SLDSTransformationError {
   
    // Check if cached
    Pair<Double, Double> res = activitySetContainedProb.get(activities);
    if (res != null) {
      return res;
    }
    
    // Search in HFDD graph
    if (hfddGraph.isPresent()) {
      HFDDVertex v = hfddGraph.get().getVertex(activities);
    
      // Found vertex corresponding to the activity set
      if (v != null) {
        double probL = v.getVertexInfo().getBaseMeasurement().getProbLeftNonEmpty();
        double probR = v.getVertexInfo().getBaseMeasurement().getProbRightNonEmpty();
        res = Pair.of(probL, probR);
        // Cache
        this.activitySetContainedProb.put(activities, res);
        return res;
      }
    }
    
    // Neither cached nor found in HFDD graph
    // Left
    int countL = 0;
    for (V v : data.getDataSourceLeft().getVariantLog()) {
      if (v.containsAllCategories(activities)) {
        countL += v.getSupport();
      }
    }
    // Right
    int countR = 0;
    for (V v : data.getDataSourceRight().getVariantLog()) {
      if (v.containsAllCategories(activities)) {
        countR += v.getSupport();
      }
    }
    
    double probL = ((double) countL) / data.getDataSourceLeft().getVariantLog().sizeLog();
    double probR = ((double) countR) / data.getDataSourceRight().getVariantLog().sizeLog();
    res = Pair.of(probL, probR);
    
    //Cache
    this.activitySetContainedProb.put(activities, res);
    
    return res;
    
  }
  
  public ActivityOccurencePosition getAvgFirstActivityOccurrences() {
    // Not yet initialized
    if (this.avgFirstActivityOccurrences.isEmpty()) {
      logger.debug("Information on average position of first activity occurrences not initialized yet. "
          + "Attempting initialization....");
      try {
        ActivityOccurencePosition actOccPos = ActivityOccurencePosition.getActOccurenceStatistics(
            List.of(data.getDataSourceLeftBase().getVariantLog(), data.getDataSourceRightBase().getVariantLog()));
        if (actOccPos != null) {
          this.avgFirstActivityOccurrences = Optional.of(actOccPos);
          logger.debug("Average position of first activity occurrences initialization successful"); 
        }
      } catch (SLDSTransformationError e) {
        logger.error("Failed to initialize information on first activity occurrences.");
        return null;
      }
    }
    return this.avgFirstActivityOccurrences.get();
  }
  
}
