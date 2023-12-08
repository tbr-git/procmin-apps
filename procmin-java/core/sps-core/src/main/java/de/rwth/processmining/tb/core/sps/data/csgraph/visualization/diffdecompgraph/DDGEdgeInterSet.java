package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

/**
 * Edge between two sets of activities that introduce a difference.
 * @author brockhoff
 *
 */
public final class DDGEdgeInterSet extends DDGEdge {
	
	/**
	 * Associated probability in the left log.
	 */
	private final double probabilityLeft;

	/**
	 * Associated probability in the right log.
	 */
	private final double probabilityRight;
	
	private boolean isEdgeInLayoutTree;

	public DDGEdgeInterSet(int id, double probabilityLeft, 
			double probabilityRight) {
		super(id);
		this.probabilityLeft = probabilityLeft;
		this.probabilityRight = probabilityRight;
		this.isEdgeInLayoutTree = false;
	}

	////////////////////////////////////////////////////////////////////////////////
	// Getter / Setter
	////////////////////////////////////////////////////////////////////////////////

	public double getProbabilityLeft() {
		return probabilityLeft;
	}

	public double getProbabilityRight() {
		return probabilityRight;
	}

	@Override
	public DDGEdgeType getEdgeType() {
		return DDGEdgeType.INTERSET;
	}

	public boolean isEdgeInLayoutTree() {
		return isEdgeInLayoutTree;
	}

	public void setEdgeInLayoutTree(boolean isEdgeInLayoutTree) {
		this.isEdgeInLayoutTree = isEdgeInLayoutTree;
	}
	
}
