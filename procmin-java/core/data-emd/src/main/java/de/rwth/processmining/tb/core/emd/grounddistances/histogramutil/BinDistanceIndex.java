package de.rwth.processmining.tb.core.emd.grounddistances.histogramutil;

public class BinDistanceIndex implements BinDistance {

  /**
   * Number of bins in the considered histogram.
   */
  private final int nbrBins;

  public BinDistanceIndex(int nbrBins) {
    this.nbrBins = nbrBins;
  }

  @Override
  public double getDistance(int binIndex1, int binIndex2) {
    return Math.abs(binIndex1 - binIndex2);
  }

  @Override
  public double getDistanceNormalized(int binIndex1, int binIndex2) {
    if (binIndex1 == binIndex2) {
      return 0;
    }
    else {
      return Math.abs(binIndex1 - binIndex2) / ((double) nbrBins - 1);
    }
  }

}
