package de.rwth.processmining.tb.core.emd.grounddistances.histogramutil;

// Since we save bin indices in the trace descriptors to safe space,
// this interface it defined on top of indices rather than values.
/**
 * Interface specifying distances between individual bin (indices) of histograms.
 * 
 */
public interface BinDistance {
  
  /**
   * Get a distance value between two bins specified by their indices.
   * @param binIndex1 Index of first bin
   * @param binIndex2 Index of second bin
   * @return Distance value
   */
  public double getDistance(int binIndex1, int binIndex2);

  /**
   * Get a normalized distance value between two bins specified by their indices.
   * @param binIndex1 Index of first bin
   * @param binIndex2 Index of second bin
   * @return Distance value in [0, 1]
   */
  public double getDistanceNormalized(int binIndex1, int binIndex2);

}
