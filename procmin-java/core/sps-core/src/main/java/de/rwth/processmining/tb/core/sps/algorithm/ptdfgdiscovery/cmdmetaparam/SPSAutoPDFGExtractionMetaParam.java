package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam;

/**
 * Parameters tuning the auto PT-DFG extraction method
 */
public class SPSAutoPDFGExtractionMetaParam {

  /**
   * Number of PT-DFGs to be created (difference strength in 
   * descending order)
   */
  private int nbrMetaDiagnostics = 5; 
  
  /**
   * Each PT-DFGs contains at most k complementary differences.
   */
  private int compDiffSetSize = 3;
  
  public SPSAutoPDFGExtractionMetaParam() {}

  public SPSAutoPDFGExtractionMetaParam(int nbrMetaDiagnostics, int compDiffSetSize) {
    super();
    this.nbrMetaDiagnostics = nbrMetaDiagnostics;
    this.compDiffSetSize = compDiffSetSize;
  }

  /**
   * @return the nbrMetaDiagnostics
   */
  public int getNbrMetaDiagnostics() {
    return nbrMetaDiagnostics;
  }

  /**
   * @param nbrMetaDiagnostics the nbrMetaDiagnostics to set
   */
  public void setNbrMetaDiagnostics(int nbrMetaDiagnostics) {
    this.nbrMetaDiagnostics = nbrMetaDiagnostics;
  }

  /**
   * @return the compDiffSetSize
   */
  public int getCompDiffSetSize() {
    return compDiffSetSize;
  }

  /**
   * @param compDiffSetSize the compDiffSetSize to set
   */
  public void setCompDiffSetSize(int compDiffSetSize) {
    this.compDiffSetSize = compDiffSetSize;
  }

}
