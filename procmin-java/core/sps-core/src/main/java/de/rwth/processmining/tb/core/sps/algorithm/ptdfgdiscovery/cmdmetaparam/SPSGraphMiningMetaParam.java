package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam;

/**
 * Class holding the command line hyper parameter for the SPS graph mining algorithm.
 */
public class SPSGraphMiningMetaParam {
  
  /**
   * Target number of vertices in the SPS graph.
   */
  private int targetNbrVertices = 10_000;
  
  /**
   * Margin around {@link #targetNbrVertices} that we accept.
   * -> Number of vertices in mined SPS Graph will be in
   * [(1 - {@link #targetNbrMargin}) * {@link #targetNbrVertices},  
   * (1 + {@link #targetNbrMargin}) * {@link #targetNbrVertices}]
   */
  private double targetNbrMargin = 0.1;

  /**
   * Maximum time in milliseconds for discovering the SPS graph.
   * If time expires, a partial result will be used.
   */
  private int maxMiningTimeMs = 20_000;  
  
	/**
	 * If there are many frequently occurring activities, we won't be able to 
	 * mine all frequent activity sets event for high minimum support values.
	 * 
	 * Since FP-growth is a depth first search kind of algorithm, the currently best result
	 * will likely only cover a subsets of all activities. 
	 * 
	 *  Therefore, a backup solution is computed using this support threshold in a breadth first search manner
	 *  (i.e., A-priori). 
	 *  It collects all small frequent activity itemsets having at least this support until we reach
	 *  the target number of vertices ({@link #targetActISNbr).
	 */
	private double minSupportBFSActivitySets = 0.005;
	
	/**
	 * If we can find a complete result with DFS (i.e., FP-growth) and an effective minimum 
	 * support threshold larger than this, still resort to the BFS (i.e., Apriori) partial solution.
	 * => Rather use small (e.g., size 2) itemsets than ignoring activities 
	 * that occur in less than 1% of all cases 
	 */
	private double thresholdPreferBFSOverComplete = 0.01;
	
	public SPSGraphMiningMetaParam() {};

  public SPSGraphMiningMetaParam(int targetNbrVertices, double targetNbrMargin, int maxMiningTimeMs,
      double minSupportBFSActivitySets, double thresholdPreferBFSOverComplete) {
    super();
    this.targetNbrVertices = targetNbrVertices;
    this.targetNbrMargin = targetNbrMargin;
    this.maxMiningTimeMs = maxMiningTimeMs;
    this.minSupportBFSActivitySets = minSupportBFSActivitySets;
    this.thresholdPreferBFSOverComplete = thresholdPreferBFSOverComplete;
  }

  /**
   * @return the targetNbrVertices
   */
  public int getTargetNbrVertices() {
    return targetNbrVertices;
  }

  /**
   * @param targetNbrVertices the targetNbrVertices to set
   */
  public void setTargetNbrVertices(int targetNbrVertices) {
    this.targetNbrVertices = targetNbrVertices;
  }

  /**
   * @return the targetNbrMargin
   */
  public double getTargetNbrMargin() {
    return targetNbrMargin;
  }

  /**
   * @param targetNbrMargin the targetNbrMargin to set
   */
  public void setTargetNbrMargin(double targetNbrMargin) {
    this.targetNbrMargin = targetNbrMargin;
  }

  /**
   * @return the maxMiningTimeMs
   */
  public int getMaxMiningTimeMs() {
    return maxMiningTimeMs;
  }

  /**
   * @param maxMiningTimeMs the maxMiningTimeMs to set
   */
  public void setMaxMiningTimeMs(int maxMiningTimeMs) {
    this.maxMiningTimeMs = maxMiningTimeMs;
  }

  /**
   * @return the minSupportBFSActivitySets
   */
  public double getMinSupportBFSActivitySets() {
    return minSupportBFSActivitySets;
  }

  /**
   * @param minSupportBFSActivitySets the minSupportBFSActivitySets to set
   */
  public void setMinSupportBFSActivitySets(double minSupportBFSActivitySets) {
    this.minSupportBFSActivitySets = minSupportBFSActivitySets;
  }

  /**
   * @return the thresholdPreferBFSOverComplete
   */
  public double getThresholdPreferBFSOverComplete() {
    return thresholdPreferBFSOverComplete;
  }

  /**
   * @param thresholdPreferBFSOverComplete the thresholdPreferBFSOverComplete to set
   */
  public void setThresholdPreferBFSOverComplete(double thresholdPreferBFSOverComplete) {
    this.thresholdPreferBFSOverComplete = thresholdPreferBFSOverComplete;
  }

}
