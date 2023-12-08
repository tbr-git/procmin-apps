package de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph;

public final class ProbDiffDFGVertexActivity extends ProbDiffDFGVertex {

	public ProbDiffDFGVertexActivity(int categoryCode, String name, float probLeft, float probRight) {
		super(categoryCode, name, probLeft, probRight);
	}
	
	/**
	 * Increase the probability in the left log.
	 * @param probLeft
	 */
	public void incProbabilityLeft(float probLeft) {
		this.probLeft += probLeft;
	}

	/**
	 * Increase the probability in the right log.
	 * @param probRight
	 */
	public void incProbabilityRight(float probRight) {
		this.probRight += probRight;
	}

}
