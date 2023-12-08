package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam;

/**
 * Parameters that adjust the visualization of the PT-DFGs 
 */
public class PTDFGVisualizationMetaParam {
  
  /**
   * Attempt to remove all edges that a less frequent in both 
   * stochastic languages (e.g., 0.02 corresponds to frequency filter of 2%)
   */
  private float filterEdgeFrequency = 0.02f;
  
  /**
   * Show all edges where frequency differs more than this between the two logs.
   * 
   * => Potentially keeps edges even if {@link #filterEdgeFrequency} would apply.
   */
  private float showDifferencesAbove = 0.01f;
  
  /**
   * For color coding the edges, we use the range [-1* this value, this value]. 
   * Adjust this value to show small differences more clearly.
   * 
   * For example, the value 0.4 causes frequency differences of 0.4 to be colored brightly
   */
  private float upperRangeColorScale = 0.4f;

  /**
   * Maximum line width of edges.
   * Determined by relevance of an edge (maximum frequency of both logs).
   */
  private float maxLineWidth = 5f;

  /**
   * Minimum line width of edges.
   * Determined by relevance of an edge (maximum frequency of both logs).
   */
  private float minLineWidth = 0.5f;
  
  public PTDFGVisualizationMetaParam() {}

  public PTDFGVisualizationMetaParam(float filterEdgeFrequency, float showDifferencesAbove,
      float upperRangeColorScale, float minLineWidth, float maxLineWidth) {
    super();
    this.filterEdgeFrequency = filterEdgeFrequency;
    this.showDifferencesAbove = showDifferencesAbove;
    this.upperRangeColorScale = upperRangeColorScale;
    this.minLineWidth = minLineWidth;
    this.maxLineWidth = maxLineWidth;
  }

  /**
   * @return the filterEdgeFrequency
   */
  public float getFilterEdgeFrequency() {
    return filterEdgeFrequency;
  }

  /**
   * @param filterEdgeFrequency the filterEdgeFrequency to set
   */
  public void setFilterEdgeFrequency(float filterEdgeFrequency) {
    this.filterEdgeFrequency = filterEdgeFrequency;
  }

  /**
   * @return the showDifferencesAbove
   */
  public float getShowDifferencesAbove() {
    return showDifferencesAbove;
  }

  /**
   * @param showDifferencesAbove the showDifferencesAbove to set
   */
  public void setShowDifferencesAbove(float showDifferencesAbove) {
    this.showDifferencesAbove = showDifferencesAbove;
  }

  /**
   * @return the upperRangeColorScale
   */
  public float getUpperRangeColorScale() {
    return upperRangeColorScale;
  }

  /**
   * @param upperRangeColorScale the upperRangeColorScale to set
   */
  public void setUpperRangeColorScale(float upperRangeColorScale) {
    this.upperRangeColorScale = upperRangeColorScale;
  }

  /**
   * @return the maxLineWidth
   */
  public float getMaxLineWidth() {
    return maxLineWidth;
  }

  /**
   * @param maxLineWidth the maxLineWidth to set
   */
  public void setMaxLineWidth(float maxLineWidth) {
    this.maxLineWidth = maxLineWidth;
  }

  /**
   * @return the minLineWidth
   */
  public float getMinLineWidth() {
    return minLineWidth;
  }

  /**
   * @param minLineWidth the minLineWidth to set
   */
  public void setMinLineWidth(float minLineWidth) {
    this.minLineWidth = minLineWidth;
  }
  

}
