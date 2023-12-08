package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam;

public class SPSComplementaryDiffParam {
  
  /**
   * Upper bound on the Jaccard index of two vertices' co-occurence.
   */
  float upperBoundCoOccJaccard = 0.2f;
  
  public SPSComplementaryDiffParam() {};

  public SPSComplementaryDiffParam(float upperBoundCoOccJaccard) {
    super();
    this.upperBoundCoOccJaccard = upperBoundCoOccJaccard;
  }

  /**
   * @return the upperBoundCoOccJaccard
   */
  public float getUpperBoundCoOccJaccard() {
    return upperBoundCoOccJaccard;
  }

  /**
   * @param upperBoundCoOccJaccard the upperBoundCoOccJaccard to set
   */
  public void setUpperBoundCoOccJaccard(float upperBoundCoOccJaccard) {
    this.upperBoundCoOccJaccard = upperBoundCoOccJaccard;
  }

}
