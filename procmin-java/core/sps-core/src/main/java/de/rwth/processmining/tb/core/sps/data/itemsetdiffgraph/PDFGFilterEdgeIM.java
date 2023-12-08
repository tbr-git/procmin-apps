package de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.AbstractBaseGraph;

/**
 * Filters the edges of the graph.
 * 
 * Filter regime:
 * <p>
 * <ol>
 * <li>Flag edges: for each vertex v: for each outgoing edge e: max(e.probL,
 * e.probR) < f * max outgoing Probability</li>
 * <li>For each vertex: keep most frequent incoming edge</li>
 * </ol>
 * 
 */
public class PDFGFilterEdgeIM implements ProbDiffDFGFilter {

  private float outFilterFactor = 0.1f;

  /**
   * Constructor
   * 
   * @param outFilterFactor Attempt to remove edges with probability less than
   *                        factor
   */
  public PDFGFilterEdgeIM(float outFilterFactor) {
    this.outFilterFactor = outFilterFactor;
  }

  @Override
  public ProbDiffDFG apply(ProbDiffDFG probDiffDfg) {
    @SuppressWarnings("unchecked")
    AbstractBaseGraph<ProbDiffDFGVertex, ProbDiffDFGEdge> gCopy = (AbstractBaseGraph<ProbDiffDFGVertex, ProbDiffDFGEdge>) probDiffDfg
        .getGraph().clone();

    Set<ProbDiffDFGEdge> edgeCandidateRemove = new HashSet<>();

    for (ProbDiffDFGVertex v : gCopy.vertexSet()) {
      ArrayList<ProbDiffDFGEdge> outEdges = new ArrayList<>(gCopy.outgoingEdgesOf(v));
      if (outEdges.size() == 0) {
        continue;
      }

      // Maximum probability outgoing edges
      Comparator<ProbDiffDFGEdge> compareEdgesByProbAscending = Comparator
          .comparing((ProbDiffDFGEdge e) -> Math.max(e.getProbLeft(), e.getProbRight()));
      ProbDiffDFGEdge maxProbEdge = Collections.max(outEdges, compareEdgesByProbAscending);
      double maxProb = Math.max(maxProbEdge.probLeft, maxProbEdge.probRight);

      // "Flag" all outgoing edges less than factor * max-outgoing-probability for
      // removal
      for (ProbDiffDFGEdge e : outEdges) {
        double probE = Math.max(e.probLeft, e.probRight);
        if (probE < outFilterFactor * maxProb) {
          edgeCandidateRemove.add(e);
        }
      }
    }

    // For each vertex, ensure that most frequent incoming edge is kept
    for (ProbDiffDFGVertex v : gCopy.vertexSet()) {
      ArrayList<ProbDiffDFGEdge> inEdges = new ArrayList<>(gCopy.incomingEdgesOf(v));
      if (inEdges.size() == 0) {
        continue;
      }

      // Maximum probability ingoing edges
      Comparator<ProbDiffDFGEdge> compareEdgesByProbAscending = Comparator
          .comparing((ProbDiffDFGEdge e) -> Math.max(e.getProbLeft(), e.getProbRight()));
      ProbDiffDFGEdge maxProbEdge = Collections.max(inEdges, compareEdgesByProbAscending);

      // "Unflag" from removal
      edgeCandidateRemove.remove(maxProbEdge);
    }

    //////////////////////////////
    // Remove Edges
    //////////////////////////////
    gCopy.removeAllEdges(edgeCandidateRemove);

    return new ProbDiffDFG(gCopy, probDiffDfg.getProbabilityNonEmptyLeft(), probDiffDfg.getProbabilityNonEmptyRight(),
        probDiffDfg.getType());
  }

}
