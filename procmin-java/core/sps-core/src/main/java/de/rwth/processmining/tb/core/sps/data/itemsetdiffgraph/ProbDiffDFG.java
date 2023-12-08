package de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph;

import java.text.DecimalFormat;

import org.jgrapht.graph.AbstractBaseGraph;

public class ProbDiffDFG {

	/**
	 * Handle to the graph structure that actually holds the data
	 */
	private final AbstractBaseGraph<ProbDiffDFGVertex, ProbDiffDFGEdge> probDFG;
	
	/**
	 * Probability of non-empty trace in left log;
	 */
	private final double probabilityNonEmptyLeft;
	
	/**
	 * Probability of non-empty trace in right log;
	 */
	private final double probabilityNonEmptyRight;
	
	private final ProbDiffDFGType type;
	
	/**
	 * Maximum line with used for the Dot String
	 */
	private float maxDotLineWidth = 5f;

	public ProbDiffDFG(AbstractBaseGraph<ProbDiffDFGVertex, ProbDiffDFGEdge> probDFG, 
	    double probabilityNonEmptyLeft, double probabilityNonEmptyRight, ProbDiffDFGType type) {
		super();
		this.probDFG = probDFG;
		this.probabilityNonEmptyLeft = probabilityNonEmptyLeft;
		this.probabilityNonEmptyRight = probabilityNonEmptyRight;
		this.type = type;
	}
	
	public AbstractBaseGraph<ProbDiffDFGVertex, ProbDiffDFGEdge> getGraph() {
		return probDFG;
	}
	
	public String getDotString() {
		// Format used to display probabilities in the picture (avoid excessive number of digits)
		DecimalFormat formatProbDisplay = new DecimalFormat("#.###");
		StringBuilder builder = new StringBuilder();
		builder.append("digraph{ ");
		builder.append("rankdir=\"LR\";");
		for (ProbDiffDFGVertex v: probDFG.vertexSet()) {
			builder.append("n" + v.getCategoryCode());
			builder.append(" [");
			builder.append("id=" + v.getCategoryCode());
			builder.append(", label=\"" + v.getName() + "(" + 
					formatProbDisplay.format(v.probLeft)  + " | " + formatProbDisplay.format(v.probRight) + ")\"");
			builder.append(", shape=box");
			//builder.append(", probLeft=" + v.probLeft);
			//builder.append(", probRight=" + v.probRight);
			builder.append(" ];");
		}
		
		for (ProbDiffDFGEdge e : probDFG.edgeSet()) {
			builder.append("n" + probDFG.getEdgeSource(e).getCategoryCode());
			builder.append(" -> ");
			builder.append("n" + probDFG.getEdgeTarget(e).getCategoryCode());
			builder.append(" [");
			builder.append("id=" + e.getId());
			builder.append(", label=\"(" + 
					formatProbDisplay.format(e.probLeft)  + " | " + formatProbDisplay.format(e.probRight) + ")\"");
			//builder.append(", probLeft=" + e.probLeft);
			//builder.append(", probRight=" + e.probRight);
			builder.append(", penwidth=" + Math.max(Math.max(e.probLeft, e.probRight) * maxDotLineWidth, 0.001));
			builder.append(" ];");
		}
		builder.append("}");
		
		return builder.toString();
	}

  public double getProbabilityNonEmptyLeft() {
    return probabilityNonEmptyLeft;
  }

  public double getProbabilityNonEmptyRight() {
    return probabilityNonEmptyRight;
  }

  public ProbDiffDFGType getType() {
    return type;
  }

  /**
   * @return the maxDotLineWidth
   */
  public float getMaxDotLineWidth() {
    return maxDotLineWidth;
  }

  /**
   * @param maxDotLineWidth the maxDotLineWidth to set
   */
  public void setMaxDotLineWidth(float maxDotLineWidth) {
    this.maxDotLineWidth = maxDotLineWidth;
  }
}
