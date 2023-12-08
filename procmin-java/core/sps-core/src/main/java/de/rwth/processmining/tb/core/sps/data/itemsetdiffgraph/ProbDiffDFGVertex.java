package de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph;

public abstract sealed class ProbDiffDFGVertex permits ProbDiffDFGVertexActivity, ProbDiffDFGVertexStart, ProbDiffDFGVertexEnd {
	
	/**
	 * Category code of the associated activity.
	 */
	private final int categoryCode;
	
	/**
	 * Probability in left log
	 */
	float probLeft;
	
	/**
	 * Probability in right log
	 */
	float probRight;
	
	/**
	 * Name (label) of the vertex
	 */
	private final String name;

	public ProbDiffDFGVertex(int categoryCode, String name, float probLeft, float probRight) {
		super();
		this.categoryCode = categoryCode;
		this.probLeft = probLeft;
		this.probRight = probRight;
		this.name = name;
	}

	public int getCategoryCode() {
		return categoryCode;
	}

	public float getProbLeft() {
		return probLeft;
	}

	public float getProbRight() {
		return probRight;
	}

	public String getName() {
		return name;
	}

  @Override
  public int hashCode() {
    int hash = categoryCode;
    hash = 31 * hash + name.hashCode();
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this 
        || (obj instanceof ProbDiffDFGVertex v 
            && this.getCategoryCode() == v.getCategoryCode()
            && this.getName().equals(v.getName()));
  }
	
	

}
