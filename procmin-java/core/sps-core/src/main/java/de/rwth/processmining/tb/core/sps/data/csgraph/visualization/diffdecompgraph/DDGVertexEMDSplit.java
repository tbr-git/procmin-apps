package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;

/**
 * Vertex in the difference decomposition graph (@link {@link DiffDecompGraph} that is used as an
 * accumulation point before splitting the probability flow for an EMD solution.
 * @author brockhoff
 *
 */
public final class DDGVertexEMDSplit extends DDGVertex {

	/**
	 * Associated probability mass left log.
	 */
	private final double probabilityMassLeft;

	/**
	 * Associated probability mass right log.
	 */
	private final double probabilityMassRight;

	public DDGVertexEMDSplit(int id, double probabilityMassLeft, 
			double probabilityMassRight, CSGraphVertex csGraphVertex) {
		super(id, "ddg-split-" + csGraphVertex.getHfddVertexRef().getId(), csGraphVertex);
		this.probabilityMassLeft = probabilityMassLeft;
		this.probabilityMassRight = probabilityMassRight;
	}

	////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters
	////////////////////////////////////////////////////////////////////////////////
	
	public double getProbabilityMassLeft() {
		return probabilityMassLeft;
	}

	public double getProbabilityMassRight() {
		return probabilityMassRight;
	}

	@Override
	public DDGVertexType getVertexType() {
		return DDGVertexType.FLOWSPLIT;
	}
}
