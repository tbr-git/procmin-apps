package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam;

public class SPSIVFMetaParam {
  
  /**
   * Interestingness classification:
   * Minimum metric value (e.g, 0.05 corresponds to 5% frequency difference)
   */
  private double ivfTMetric = 0.001;
  
  /**
   * Interestingness classification:
   * Upper bound coefficient of occurrence (if approx. 0, independent)
   */
  private double ivfTPhi = 0.05;

  /**
   * Backward pass, gathering interesting vertices:
   * More specific vertex dominates less specific vertex if interesting and EMD 
   * is similar - dominating if EMD_specific > 0.9 * EMD_lessspecific)
   */
  private double ivfTDom = 0.9;
  
  public SPSIVFMetaParam() {};

  public SPSIVFMetaParam(double ivfTMetric, double ivfTPhi, double ivfTDom) {
    super();
    this.ivfTMetric = ivfTMetric;
    this.ivfTPhi = ivfTPhi;
    this.ivfTDom = ivfTDom;
  }

  /**
   * @return the ivfTMetric
   */
  public double getIvfTMetric() {
    return ivfTMetric;
  }

  /**
   * @param ivfTMetric the ivfTMetric to set
   */
  public void setIvfTMetric(double ivfTMetric) {
    this.ivfTMetric = ivfTMetric;
  }

  /**
   * @return the ivfTPhi
   */
  public double getIvfTPhi() {
    return ivfTPhi;
  }

  /**
   * @param ivfTPhi the ivfTPhi to set
   */
  public void setIvfTPhi(double ivfTPhi) {
    this.ivfTPhi = ivfTPhi;
  }

  /**
   * @return the ivfTDom
   */
  public double getIvfTDom() {
    return ivfTDom;
  }

  /**
   * @param ivfTDom the ivfTDom to set
   */
  public void setIvfTDom(double ivfTDom) {
    this.ivfTDom = ivfTDom;
  }

}
