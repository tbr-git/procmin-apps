package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

/**
 * Edge in the difference decomposition tree visualization.
 * 
 * Edges can be:
 * <ul>
 * 	<li>Edges between item sets 
 * 	<li>Edges between trace variants in EMD reallocation
 * </ul>
 * @author brockhoff
 *
 */
public abstract sealed class DDGEdge permits DDGEdgeInterSet, DDGEdgeSplitToEMD, DDGEdgeEMD {
	
	private final int id;
	
	public DDGEdge(int id) {
		this.id = id;
	}
	
	public abstract DDGEdgeType getEdgeType();

	public int getId() {
		return this.id;
	}

}
