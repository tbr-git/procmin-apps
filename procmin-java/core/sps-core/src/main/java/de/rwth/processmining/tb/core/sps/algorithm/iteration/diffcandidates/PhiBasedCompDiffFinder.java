package de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates;

import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagement;
import de.rwth.processmining.tb.core.sps.algorithm.spsvertex.SPSVertexOp;
import de.rwth.processmining.tb.core.sps.algorithm.spsvertex.SPSVertexStatistics;

/**
 * Find approx. k complementary difference.
 * 
 * Given a {@link DiffCandidate} and a list of candidates, find approximately k complementary differences.
 * We consider a difference complementary, if its EMD value is high and its occurrence is either 
 * strongly positive or strongly negative correlated with the occurrence of the provided candidate.
 *
 */
public class PhiBasedCompDiffFinder {
  private final static Logger logger = LogManager.getLogger( PhiBasedCompDiffFinder.class );
  
  /**
   * Handle to the iteration management (to query data etc.)
   */
  private final HFDDIterationManagement hfddItMan;
  
  /**
   * Iteration to work on
   */
  private final int iteration;

  /**
   * Consider differences such that phi-coefficient between 
   * occurrence of provided and considered vertex is less than phiLower
   */
  private final float phiLower;

  /**
   * Consider differences such that phi-coefficient between 
   * occurrence of provided and considered vertex is larger than phiUpper
   */
  private final float phiUpper;

  /**
   * Complement by k similar differences
   */
  private final int k;

  /**
   * Also accept differences with EMD larger kSlack * kth vertex
   */
  private static float kSlack = 0.8f;
  
  
  public PhiBasedCompDiffFinder(HFDDIterationManagement hfddItMan, int iteration, 
      float phiLower, float phiUpper, int k) {
    this.hfddItMan = hfddItMan;
    this.iteration = iteration;
    this.phiLower = phiLower;
    this.phiUpper = phiUpper;
    this.k = k;
  }

  public Collection<DiffCandidate> findComplementaryDifferences(Collection<DiffCandidate> candidates, 
      DiffCandidate c) {

		PerspectiveDescriptor pDesc = 
				hfddItMan.getPerspective4Iteration(iteration);
		
		// Sort by descending EMD scores
		Function<DiffCandidate, Double> extractMetric = 
		    candidate -> candidate.v().getVertexInfo().getMeasurements().get(pDesc).getMetric().orElse(0d);
		Comparator<DiffCandidate> sortCandidateMetric = 
		    Comparator.comparing(extractMetric)
		    .reversed();
    List<DiffCandidate> candidatesSorted = candidates.stream()
        .sorted(sortCandidateMetric)
        .toList();
        
    // Results
    List<DiffCandidate> compDifferences = new LinkedList<>();
		int countErrors = 0;
		// EMD associated with kth complementary difference
		double emdKCompDiff = -1;
    for (DiffCandidate cComp : candidatesSorted) {
      // Ignore c itself
      if (c.equals(cComp)) {
        continue;
      }

			// Check if EMD is still promising
			// !!! Some slack is allowed !!!
			double emdCComp = cComp.v().getVertexInfo().getMeasurements().get(pDesc).getMetric().orElse(-10d);
			if (emdCComp < kSlack * emdKCompDiff) {
				// Since we sort by EMD we are done
				// Subsequent vertices will never be added
				break;
			}

			// Joint itemset Info
			BitSet bothActivities = SPSVertexOp.getJointVertexDescriptor(c.v(), cComp.v());
			Pair<Double, Double> jointProb = null;
			try {
				jointProb = hfddItMan.getDataQueryEngine().getActivitySetOccurrenceProbability(bothActivities);
			}
			catch (SLDSTransformationError err) {
				countErrors++;
			}

			////////////////////
			// Compute Phi Coefficients
			////////////////////
			// Phi Left
			double phiLeft = SPSVertexStatistics.occProbPhiCoeff(c.v(), cComp.v(), jointProb.getLeft(), true);
			// Phi Right
      double phiRight = SPSVertexStatistics.occProbPhiCoeff(c.v(), cComp.v(), jointProb.getRight(), false);
      
      System.out.println(cComp.v().toString() + " - phiLeft=" + phiLeft + "; " + "phiRight=" + phiRight);

			////////////////////
			// Evaluate Phi Coefficients
			////////////////////
			// Within the coefficient bound ( <= lower || >= upper)
			if (Math.min(phiLeft, phiRight) <= this.phiLower || Math.max(phiLeft, phiRight) >= this.phiUpper) {
				compDifferences.add(cComp);

				// Set distance of kth complementary difference
				if (compDifferences.size() == this.k) {
					emdKCompDiff = emdCComp;
				}
			}
    }

		if (countErrors > 0) {
			logger.warn("Could not evaluate {} candidates to complement the difference found for {}", countErrors, c.v().toString());
		}

		return compDifferences;
  }
}
