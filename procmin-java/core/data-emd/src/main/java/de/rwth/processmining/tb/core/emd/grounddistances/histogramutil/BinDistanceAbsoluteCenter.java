package de.rwth.processmining.tb.core.emd.grounddistances.histogramutil;

import de.rwth.processmining.tb.core.util.histogram.HistogramSpecDataEdges;

public class BinDistanceAbsoluteCenter implements BinDistance {
  
  /**
   * Histogram specification
   */
  private final HistogramSpecDataEdges edgeSpec;
  
  /**
   * Centers of the bins (values)k
   */
  private final double[] binCenters;
  
  /**
   * Normalization constant
   */
  private final double normFactor;
  
  public BinDistanceAbsoluteCenter(HistogramSpecDataEdges edgeSpec) {
    this.edgeSpec = edgeSpec;

    // Derived: center and normalization
    this.binCenters = edgeSpec.bins().stream()
      .mapToDouble(p -> (p.getLeft() + p.getRight()) / 2.0) // bin centers
      .toArray();
    normFactor = this.binCenters[this.binCenters.length - 1] - this.binCenters[0];
  }

  @Override
  public double getDistance(int binIndex1, int binIndex2) {
    return Math.abs(this.binCenters[binIndex2] - this.binCenters[binIndex1]);
  }

  @Override
  public double getDistanceNormalized(int binIndex1, int binIndex2) {
    double d = Math.abs(this.binCenters[binIndex2] - this.binCenters[binIndex1]);
    return d / normFactor;
  }

}
