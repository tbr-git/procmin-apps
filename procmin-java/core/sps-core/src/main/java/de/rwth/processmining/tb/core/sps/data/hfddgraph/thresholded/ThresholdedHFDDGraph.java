package de.rwth.processmining.tb.core.sps.data.hfddgraph.thresholded;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurement;

public class ThresholdedHFDDGraph {
  private static final Logger logger = LogManager.getLogger(ThresholdedHFDDGraph.class);

  /**
   * Map from vertex id to vertex.
   */
  private Map<Integer, HFDDVertex> vertices;

  /**
   * Handle to a JGraphT Graph that is used to keep the graph structure.
   */
  private Graph<HFDDVertex, DefaultEdge> g;

  /**
   * Mapping from activities to categories and vice versa.
   */
  private CategoryMapper categoryMapper;

  /**
   * Domination information. True iff the vertex is dominated in terms of
   * different score and probability child vertices.
   */
  private Map<Integer, Boolean> dominationInfo;

  /**
   * The perspective on which the thresholding has been applied (thresholding the
   * associated metric value).
   */
  private final PerspectiveDescriptor thresholdedPerspective;

  public ThresholdedHFDDGraph(Graph<HFDDVertex, DefaultEdge> g, CategoryMapper categoryMapper,
      PerspectiveDescriptor thresholdedPerspective) {
    super();
    this.g = g;
    this.categoryMapper = categoryMapper;
    this.vertices = new HashMap<>();
    this.thresholdedPerspective = thresholdedPerspective;
    for (HFDDVertex v : this.g.vertexSet()) {
      this.vertices.put(v.getId(), v);
    }
  }

  /**
   * Get the vertices that dominate w.r.t. to support and metric values
   * (considering the thresholds). Given an edge (u, v) from the filtered HFDD
   * Graph, vertex v dominates vertex u iff
   * <p>
   * <ol>
   * <li>support(v.itemset) >= relSupportThreshold * support(u.itemset) (<b>
   * support on combined log)
   * <li>v.metric >= relMetricThreshold * u.metric
   * </ol>
   * <p>
   * 
   * @param relSupportThreshold Allowed relative support decrease threshold
   * @param relMetricThreshold  Allowed relative metric decrease threshold
   */
  public Set<HFDDVertex> getDominatingVertices(double relSupportThreshold, double relMetricThreshold) {
    // The dominatED vertices
    Set<HFDDVertex> dominatedVertices = new HashSet<>();

    // May be useful if we require information of dominating children
    // GraphIterator<HFDDVertex, DefaultEdge> itGraph = new
    // TopologicalOrderIterator<>(new EdgeReversedGraph<HFDDVertex,
    // DefaultEdge>(g));
    // Check each vertex
    for (HFDDVertex v : this.g.vertexSet()) {
      // Check its predecessors -> Does v dominate u?
      for (HFDDVertex u : Graphs.predecessorListOf(this.g, v)) {
        // Support of v above threshold
        HFDDMeasurement measurementU = u.getVertexInfo().getMeasurements().get(thresholdedPerspective);
        HFDDMeasurement measurementV = v.getVertexInfo().getMeasurements().get(thresholdedPerspective);

        // Metric of v above threshold
        if (measurementU != null && measurementV != null) {
          if (measurementV.getMetric().get() >= relMetricThreshold * measurementU.getMetric().get()) {
            dominatedVertices.add(u);
          }
        } else {
          logger.error("A requested measurement during the domination testing was null! Ignoring this comparison");
        }
      }
    }

    // The dominatING vertices
    Set<HFDDVertex> dominatingVertices = new HashSet<>();
    dominatingVertices.addAll(this.g.vertexSet());
    dominatingVertices.removeAll(dominatedVertices);

    return dominatingVertices;
  }

}
