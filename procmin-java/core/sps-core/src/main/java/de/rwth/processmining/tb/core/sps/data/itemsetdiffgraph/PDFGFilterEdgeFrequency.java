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
 * <li>Flag edge if difference under threshold and edge not super frequent
 * <li>For each vertex: keep at least one incoming and outgoing edge, if 
 * all flagged for removal, keep the one with the biggest difference.
 * </ol>
 * 
 */
public class PDFGFilterEdgeFrequency implements ProbDiffDFGFilter {
  
  private float filterDiff = 0.01f;

  private float filterFreq = 0.02f;

  public PDFGFilterEdgeFrequency(float filterDiff, float filterFreq) {
    this.filterDiff = filterDiff;
    this.filterFreq = filterFreq;
  }

  @Override
  public ProbDiffDFG apply(ProbDiffDFG probDiffDfg) {
    @SuppressWarnings("unchecked")
    AbstractBaseGraph<ProbDiffDFGVertex, ProbDiffDFGEdge> gCopy = (AbstractBaseGraph<ProbDiffDFGVertex, ProbDiffDFGEdge>) probDiffDfg
        .getGraph().clone();

    Set<ProbDiffDFGEdge> edgeCandidateRemove = new HashSet<>();
    
    ////////////////////
    // Flag Edges
    ////////////////////
    for (ProbDiffDFGEdge e : gCopy.edgeSet()) {
      if (Math.abs(e.probLeft - e.probRight) < filterDiff 
          && Math.max(e.probLeft, e.probRight) < filterFreq) {
        edgeCandidateRemove.add(e);
      }
    }

      // "Sorts" ascending by absolute difference
      Comparator<ProbDiffDFGEdge> compareMaxDiff = Comparator
          .comparing((ProbDiffDFGEdge e) -> Math.abs(e.getProbLeft() - e.getProbRight()));

    // For each vertex, ensure one ingoing and one outgoing edge
    for (ProbDiffDFGVertex v : gCopy.vertexSet()) {
      
      //////////////////////////////
      // Ingoing Edges
      //////////////////////////////
      ArrayList<ProbDiffDFGEdge> inEdges = new ArrayList<>(gCopy.incomingEdgesOf(v));
      if (inEdges.size() != 0) {
        // If all ingoing edges would be removed
        if (edgeCandidateRemove.containsAll(inEdges)) {
          // Keep maximum difference
          ProbDiffDFGEdge maxDiffInEdge = Collections.max(inEdges, compareMaxDiff);
          edgeCandidateRemove.remove(maxDiffInEdge);
        }
      }

      //////////////////////////////
      // Outgoing Edges
      //////////////////////////////
      ArrayList<ProbDiffDFGEdge> outEdges = new ArrayList<>(gCopy.outgoingEdgesOf(v));

      if (outEdges.size() != 0) {
        // If all outgoing edges would be removed
        if (edgeCandidateRemove.containsAll(outEdges)) {
          // Keep maximum difference
          ProbDiffDFGEdge maxDiffOutEdge = Collections.max(outEdges, compareMaxDiff);
          edgeCandidateRemove.remove(maxDiffOutEdge);
        }
      }
    }


    //////////////////////////////
    // Remove Edges
    //////////////////////////////
    gCopy.removeAllEdges(edgeCandidateRemove);

    return new ProbDiffDFG(gCopy, probDiffDfg.getProbabilityNonEmptyLeft(), probDiffDfg.getProbabilityNonEmptyRight(),
        probDiffDfg.getType());
  }

}
