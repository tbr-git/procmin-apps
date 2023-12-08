package de.rwth.processmining.tb.core.sps.algorithm.spsvertex;

import java.util.BitSet;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.sps.data.statistics.DataStatQueryEngine;

public class SPSVertexStatistics {
  
  /**
   * Test whether occurrences of the two SPS-Vertices depend on each other--that is, dependent in at least one log.
   * Uses a xi^2 test.
   * @param sizeLeft Size of the left log
   * @param sizeRight Size of the right log
   * @param u First vertex
   * @param v Second vertex
   * @param uv <b>Union</b> vertex. Must <b>precisely</b> correspond to the vertex for the SPS u <b>and</b> v.
   * @param alpha Confidence level for the test
   * @return true, iff occurrence dependent in at least one log
   */
  public static boolean vertexOccurrenceDependentTest(int sizeLeft, int sizeRight,
      HFDDVertex u, HFDDVertex v, HFDDVertex uv, double alpha) {
    return  vertexOccurrenceDependentTest(sizeLeft, sizeRight, u, v, uv, true, alpha) // left
        || vertexOccurrenceDependentTest(sizeLeft, sizeRight, u, v, uv, false, alpha); // right
  }

  /**
   * Test whether occurrences of the two SPS-Vertices depend on each other.
   * Uses a xi^2 test.
   * @param sizeLeft Size of the left log
   * @param sizeRight Size of the right log
   * @param u First vertex
   * @param v Second vertex
   * @param uv <b>Union</b> vertex. Must <b>precisely</b> correspond to the vertex for the SPS u <b>and</b> v.
   * @return
   */
  public static boolean vertexOccurrenceDependentTest(int sizeLeft, int sizeRight,
      HFDDVertex u, HFDDVertex v, HFDDVertex uv, boolean sideLeft, double alpha)  {
   
    int size = (sideLeft ? sizeLeft : sizeRight);
    // Occurrence count vertex u
    int occU = (int) ((sideLeft ? u.getVertexInfo().getProbabilityLeft() : u.getVertexInfo().getProbabilityRight()) * 
        size); 
    // Occurrence count vertex v
    int occV = (int) ((sideLeft ? v.getVertexInfo().getProbabilityLeft() : v.getVertexInfo().getProbabilityRight()) * 
        size); 
    // Occurrence count vertex u AND v
    int occUV = (int) ((sideLeft ? uv.getVertexInfo().getProbabilityLeft() : uv.getVertexInfo().getProbabilityRight()) *
        size); 
    
    long[][] counts = {{size - occU - occV + occUV, occV - occUV}, {occU - occUV, occUV}};
    ChiSquareTest chisquareTest = new ChiSquareTest();
    System.out.println("Left - " + sideLeft + ": " + chisquareTest.chiSquare(counts)); 
    boolean dependent = chisquareTest.chiSquareTest(counts, alpha);
    
    return dependent;
    
  }
  
  /**
   * Compute the phi coefficient of the occurrences of the two provided subprocesses.
   * 
   * <table border = "1">
   * <tr><td></td> <td>v = 1</td>   <td>v = 0</td>   <td>total</td></tr>
   * <tr><td>u = 1</td>   <td>n11</td>   <td>n10</td> <td>n1.</td></tr>
   * <tr><td>u = 0</td>   <td>n01</td>   <td>n00</td> <td>n0.</td></tr>
   * <tr><td>total</td>   <td>n.1</td>   <td>n.0</td> <td>n</td></tr>
   * </table>
   * 
   * Phi Coefficient: n11 * n00 - n10 * n01 / (sqrt(n1. * n0. * n.0 * n.1))
   * 
   * @param u First vertex
   * @param v Second vertex
   * @param uv <b>Union</b> vertex. Must <b>precisely</b> correspond to the vertex for the SPS u <b>and</b> v.
   * @param sideLeft Consider left (true) or right (false) log
   * @return Phi coefficient
   */
  public static double vertexOccPhiCoeff(HFDDVertex u, HFDDVertex v, HFDDVertex uv, boolean sideLeft)  {
    // If I use the formula with n, products will overflow
    // Use formulation only based on the vertices' probability 

    // Probability vertex u
    double pU = sideLeft ? u.getVertexInfo().getProbabilityLeft() : u.getVertexInfo().getProbabilityRight(); 
    // Probability vertex v
    double pV = sideLeft ? v.getVertexInfo().getProbabilityLeft() : v.getVertexInfo().getProbabilityRight();
    // Occurrence count vertex u AND v
    double pUV = sideLeft ? uv.getVertexInfo().getProbabilityLeft() : uv.getVertexInfo().getProbabilityRight(); 
    
    return occProbPhiCoeff(pU, pV, pUV);
  }

  /**
   * Phi coefficient of event u (occurrence vertex u), and v(occurrence vertex v); given the likelihood of u, v, and their joint occurrence.
   *
   *
   * @param u Vertex u
   * @param v Vertex v
   * @param pUV Probability of joint event
   * @param sideLeft Consider left (true) or right (false) log
   * @return Phi coefficient
   */
  public static double occProbPhiCoeff(HFDDVertex u, HFDDVertex v, double pUV, boolean sideLeft) {
    // Probability vertex u
    double pU = sideLeft ? u.getVertexInfo().getProbabilityLeft() : u.getVertexInfo().getProbabilityRight(); 
    // Probability vertex v
    double pV = sideLeft ? v.getVertexInfo().getProbabilityLeft() : v.getVertexInfo().getProbabilityRight();
    return occProbPhiCoeff(pU, pV, pUV);
  }
  
  /**
   * Phi coefficient of event u, and v; given the likelihood of u, v, and their joint occurrence.
   *
   *
   * @param pU Probability of event U
   * @param pV Probability of event V
   * @param pUV Probability of joint event
   * @return Phi coefficient
   */
  public static double occProbPhiCoeff(double pU, double pV, double pUV) {
    double nom = pUV * (1 - pU - pV + pUV) - (pU - pUV) * (pV - pUV);
    double denom = Math.sqrt(pU * (1 - pU) * (1 - pV) * pV);
    return nom / denom;
  }

  /**
   * Jaccard index of sets "occurrence vertex u" and "occurrence vertex v"; given the likelihood of u, v, and their joint occurrence.
   *
   * Index is computed based on occurrence probabilities (rather than sets).
   *
   * @param u Vertex u
   * @param v Vertex v
   * @param pUV Probability of joint event
   * @param sideLeft Consider left (true) or right (false) log
   * @return Jaccard index of occurrence of v and u 
   */
  public static double occProbJaccardIndex(HFDDVertex u, HFDDVertex v, double pUV, boolean sideLeft) {
    // Probability vertex u
    double pU = sideLeft ? u.getVertexInfo().getProbabilityLeft() : u.getVertexInfo().getProbabilityRight(); 
    // Probability vertex v
    double pV = sideLeft ? v.getVertexInfo().getProbabilityLeft() : v.getVertexInfo().getProbabilityRight();

    return pUV / (pU + pV - pUV);
  }

  /**
   * Jaccard indices of sets "occurrence vertex u" and "occurrence vertex v" for left and right log.
   *
   * J(u, v) = p(u and v occur) / (p(u occurs) + p(v occurs) - p(u and v occur))
   *
   * @param u Vertex u
   * @param u Vertex v
   * @param dataQueryEngine Data information query engine (e.g., determine joint occurrence probability).
   *
   * @return Jaccard indices of vertices' occurrences in left and right log.
   */
  public static Pair<Double, Double> occProbJaccardIndexLeftRight(HFDDVertex u, HFDDVertex v, 
      DataStatQueryEngine<?> dataQueryEngine) throws SLDSTransformationError {
    // Joint itemset Info
    BitSet bothActivities = SPSVertexOp.getJointVertexDescriptor(u, v);
    Pair<Double, Double> jointProb = dataQueryEngine.getActivitySetOccurrenceProbability(bothActivities);

    double jaccardLeft = occProbJaccardIndex(u, v, jointProb.getLeft(), true);
    double jaccardRight = occProbJaccardIndex(u, v, jointProb.getRight(), false);

    return Pair.of(jaccardLeft, jaccardRight);
  }
  
  /**
   * Overlap coefficient of sets "occurrence vertex u" and "occurrence vertex v" for left and right log.
   * <p>
   * overlap(u, v) = p(u and v occur) / min(p(u occurs), p(v occurs))
   *
   * @param u Vertex u
   * @param u Vertex v
   * @param dataQueryEngine Data information query engine (e.g., determine joint occurrence probability).
   *
   * @return Overlap coefficient of vertices' occurrences in left and right log.
   */
  public static Pair<Double, Double> occProbOverlapCoeffLeftRight(HFDDVertex u, HFDDVertex v, 
      DataStatQueryEngine<?> dataQueryEngine) throws SLDSTransformationError {
    // Joint itemset Info
    BitSet bothActivities = SPSVertexOp.getJointVertexDescriptor(u, v);
    Pair<Double, Double> jointProb = dataQueryEngine.getActivitySetOccurrenceProbability(bothActivities);

    double jaccardLeft = occProbOverlapCoeffIndex(u, v, jointProb.getLeft(), true);
    double jaccardRight = occProbOverlapCoeffIndex(u, v, jointProb.getRight(), false);

    return Pair.of(jaccardLeft, jaccardRight);
  }
  
  /**
   * Overlap coefficient of sets "occurrence vertex u" and "occurrence vertex v"; 
   * given the likelihood of u, v, and their joint occurrence.
   *
   * @param u Vertex u
   * @param v Vertex v
   * @param pUV Probability of joint event
   * @param sideLeft Consider left (true) or right (false) log
   * @return Overlap coefficient of occurrence of v and u 
   */
  public static double occProbOverlapCoeffIndex(HFDDVertex u, HFDDVertex v, double pUV, boolean sideLeft) {
    // Probability vertex u
    double pU = sideLeft ? u.getVertexInfo().getProbabilityLeft() : u.getVertexInfo().getProbabilityRight(); 
    // Probability vertex v
    double pV = sideLeft ? v.getVertexInfo().getProbabilityLeft() : v.getVertexInfo().getProbabilityRight();

    return pUV / Math.min(pU, pV);
  }

}
