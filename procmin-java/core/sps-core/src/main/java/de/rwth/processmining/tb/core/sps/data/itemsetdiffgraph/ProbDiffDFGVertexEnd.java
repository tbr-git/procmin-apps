package de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph;

/**
 * Artificial trace end vertex
 * @author brockhoff
 *
 */
public final class ProbDiffDFGVertexEnd extends ProbDiffDFGVertex {

	/**
	 */
	public ProbDiffDFGVertexEnd(int freeCatCode) {
		super(freeCatCode, "|", 1, 1);
	}

}
