package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

/**
 * Edge that visualizes the reallocation of probability mass in the 
 * EMD flow 
 * @author brockhoff
 *
 */
public final class DDGEdgeEMD extends DDGEdge {
	
	/**
	 * Probability associated with the edge.
	 * Can be thought of as "probability flow".
	 */

	private final double probability;
	/**
	 * Cost associated with this Edge.
	 * 
	 * Cost used in EMD calculation.
	 */
	private final double cost;

	public DDGEdgeEMD(int id, double probability, double cost) {
		super(id);
		this.cost = cost;
		this.probability = probability;
	}

	public double getProbability() {
		return probability;
	}
	
	public double getCost() {
		return this.cost;
	}

	@Override
	public DDGEdgeType getEdgeType() {
		return DDGEdgeType.EMDFLOW;
	}
}
