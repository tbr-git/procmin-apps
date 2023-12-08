package de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph;

public class ProbDiffDFGEdge {
	
	private final int id;
	
	/**
	 * Probability flow in the left log
	 */
	float probLeft;
	
	/**
	 * Probability flow in the right log
	 */
	float probRight;
	
	public ProbDiffDFGEdge(int id) {
		this.id = id;
		this.probLeft = 0f;
		this.probRight = 0f;
	}

	public void incProbabilityLeft(float probLeft) {
		this.probLeft += probLeft;
	}

	public void incProbabilityRight(float probRight) {
		this.probRight += probRight;
	}

	public int getId() {
		return id;
	}

	public float getProbLeft() {
		return probLeft;
	}

	public float getProbRight() {
		return probRight;
	}

}
