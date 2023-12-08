package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

public final class DDGEdgeSplitToEMD extends DDGEdge {
	
    /**
     * Probability of the EMD problem for the LEFT stochastic
     * language (signature).
     */
	private final double probability;

	public DDGEdgeSplitToEMD(int id, double probability) {
		super(id);
		this.probability = probability;
	}
	
	public double getProbability() {
		return this.probability;
	}

	@Override
	public DDGEdgeType getEdgeType() {
		return DDGEdgeType.FLOWSPLIT;
	}
	
}
