package de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagement;
import de.rwth.processmining.tb.core.sps.algorithm.spsvertex.HFDDVertexRelation;
import de.rwth.processmining.tb.core.sps.algorithm.spsvertex.SPSVertexOp;
import de.rwth.processmining.tb.core.sps.algorithm.spsvertex.SPSVertexStatistics;
import de.rwth.processmining.tb.core.util.streaming.Either;

public class SimilarityBasedCompDiffFinder {
  private final static Logger logger = LogManager.getLogger( SimilarityBasedCompDiffFinder.class );

  /**
   * Handle to the iteration management (to query data etc.)
   */
  private final HFDDIterationManagement hfddItMan;
  
  /**
   * Iteration to work on
   */
  private final int iteration;

  /**
	 * Upper bound on the Jaccard index judging
	 * "how complementary" the occurrence of two vertices is
   */
  private final float jaccardUpper;

  /**
   * Complement by k similar differences
   */
  private final int k;

  /**
   * Also accept differences with EMD larger kSlack * kth vertex
   */
  private static float kSlack = 0.8f;

  public SimilarityBasedCompDiffFinder(HFDDIterationManagement hfddItMan, int iteration, 
      float jaccardUpper, int k) {
    this.hfddItMan = hfddItMan;
    this.iteration = iteration;
    this.jaccardUpper = jaccardUpper;
    this.k = k;
  }
  

  public Collection<DiffCandidate> findComplementaryDifferences(Collection<DiffCandidate> candidates, 
      DiffCandidate candidate) {

    logger.debug("Finding complementary differences for {}", 
        () -> Arrays.toString(candidate.v().getVertexInfo().getItemsetHumanReadable()));
    logger.debug("Initial candidates: {}", candidates.size());
		PerspectiveDescriptor pDesc = 
				hfddItMan.getPerspective4Iteration(iteration);


		////////////////////////////////////////
		// Complementary Difference Pipeline 
		// 1. Filter for structurally unrelated differences
		// 2. Filter by max Jaccard 
		//	-> (complementary occurence)
		// 3. Determine vertex similarity
		//    (itemset overlap (Jaccard))
		// 4. Sort by keys:
		//    (i) high similarity (ii) low occurrence Jaccard (iii) similar EMD
		// 5. Iteratively add best difference
		//    -> Clear differences that are structurally related to the added difference
		////////////////////////////////////////
    // Step 1-3
		Map<Boolean, List<Optional<TripleComplOccScoreSimilarityCandidate>>> resPipeline1 =  
			candidates.stream()
				// Focus on "structurally unrelated" differences
				.filter(c -> {
						HFDDVertexRelation rel = SPSVertexOp.determineVertexRelation(candidate.v(), c.v());
						return rel == HFDDVertexRelation.UNRELATED;
					})
				.map(Either.lift(c -> occurrenceCompScore(candidate, c)))
				.filter(p -> p.isLeft() || (p.getRight().get().complOccScore <= this.jaccardUpper))
				.map(Either.eitherMapRightOrPass((PairComplOccScoreCandidate p) -> vertexJaccard(candidate, p)))
				.map(Either::getRight)
				.collect(
          Collectors.partitioningBy(Optional::isPresent)
        );

    logger.debug("Filtered candidates: {}", () -> resPipeline1.get(true).size());
		// Handle Errors
		if (resPipeline1.get(false).size() > 0) {
			logger.warn("Could not evaluate {} candidates to complement the difference found for {}", 
			    resPipeline1.get(false).size(), candidate.v().toString());
		}
		
		// Step 4
		Comparator<OccSimEMDCandidate> compDiffCandidateComparator = 
		    Comparator.comparing(OccSimEMDCandidate::getSimilarity).reversed()
		      .thenComparing(OccSimEMDCandidate::getComplementaryOccScore)
		      .thenComparing(OccSimEMDCandidate::getEMDDiff);

		// Later, I need a mutable collection 
		LinkedList<OccSimEMDCandidate> complementaryDifferenceCandidates = 
		    resPipeline1.get(true)
		      .stream()
		      .map(Optional::get)
		      .map(t -> extendByEMDInfo(candidate, pDesc, t) )
		      .sorted(compDiffCandidateComparator)
		      .collect(Collectors.toCollection(LinkedList::new));

    logger.debug("Complementary candidates (at most 10):");
    if (logger.isDebugEnabled()) {
      complementaryDifferenceCandidates.stream().limit(10).forEach(logger::debug);
    }
    
    List<DiffCandidate> compDifferences = new ArrayList<>(k);
    while (compDifferences.size() < k && complementaryDifferenceCandidates.size() != 0) {
      DiffCandidate c = complementaryDifferenceCandidates.pollFirst().candidate;
      compDifferences.add(c);
      
      // Clear differences that are structurally related to c
      ListIterator<OccSimEMDCandidate> itClear = complementaryDifferenceCandidates.listIterator();
      while(itClear.hasNext()) {
        if (SPSVertexOp.determineVertexRelation(c.v(), itClear.next().candidate.v()) != HFDDVertexRelation.UNRELATED) {
          itClear.remove();
        }
      }
    }

		// Result List
    //List<DiffCandidate> compDifferences = complementaryDifferenceCandidates.stream()
    //    .map(OccSimEMDCandidate::getCandidate)
    //    .limit(k)
    //    .toList();
		return compDifferences;
  }
  
  private PairComplOccScoreCandidate occurrenceCompScore (
      DiffCandidate candidate, DiffCandidate c) throws SLDSTransformationError {
    
    //Pair<Double, Double> jaccardIndices = 
    //  SPSVertexStatistics.occProbJaccardIndexLeftRight(candidate.v(), c.v(), hfddItMan.getDataQueryEngine());

    Pair<Double, Double> similarity = 
      SPSVertexStatistics.occProbOverlapCoeffLeftRight(candidate.v(), c.v(), hfddItMan.getDataQueryEngine());

    return new PairComplOccScoreCandidate(Math.min(similarity.getLeft(), similarity.getRight()), c);
  }
  
  private TripleComplOccScoreSimilarityCandidate vertexJaccard(DiffCandidate candidate, PairComplOccScoreCandidate p) {
    double jaccardVertexSim = SPSVertexOp.vertexSimilarity(candidate.v(), p.candidate.v());
    return new TripleComplOccScoreSimilarityCandidate(p.complOccScore, jaccardVertexSim, p.candidate);
  }
  
  private OccSimEMDCandidate extendByEMDInfo(DiffCandidate candidate, PerspectiveDescriptor pDesc, 
      TripleComplOccScoreSimilarityCandidate t) {
    Optional<Double> emdC = candidate.v().getVertexInfo().getMeasurements().get(pDesc).getMetric();
    Optional<Double> emdT = t.candidate.v().getVertexInfo().getMeasurements().get(pDesc).getMetric();
    
    Double emdDiff = Math.abs(emdC.orElse(0d) - emdT.orElse(0d));
    
    return new OccSimEMDCandidate(t.complOccScore, t.similarity, emdDiff, t.candidate);
    
  }
  
	/**
	* Helper container class that holds:
	* <p>
	* <ul>
	*		<li> score evaluating how "complementary" the occurrence is, 
	*		<li> the candidate
	* </ul>
	*
	* (Easier to use in streams because getters have types that are not generics) 
	*/
  class PairComplOccScoreCandidate {
		/**
		* Score evaluating how "complementary" the occurrence is
		* Lower means more complementary
		*/
    Double complOccScore;
    
		/**
		* Difference candidate
		*/
    DiffCandidate candidate;

    PairComplOccScoreCandidate(Double complOccScore, DiffCandidate candidate) {
      this.complOccScore = complOccScore;
      this.candidate = candidate;
    }
    
		////////////////////
		// Getters
		////////////////////
    Double getComplementaryOccScore() {
      return this.complOccScore;
    }
    
    DiffCandidate getCandidate() {
      return this.candidate;
    }
  }

	/**
	* Helper container class that holds:
	* <p>
	* <ul>
	*		<li> score evaluating how "complementary" the occurrence is, 
	*		<li> vertex similarity,
	*		<li> the candidate
	* </ul>
	*
	* (Easier to use in streams because getters have types that are not generics) 
	*/
  class TripleComplOccScoreSimilarityCandidate {
		/**
		* Similarity score
		*/
		Double similarity;

		/**
		* Similarity score
		*/
    Double complOccScore;
    
		/**
		* Difference candidate
		*/
		DiffCandidate candidate;

    TripleComplOccScoreSimilarityCandidate(Double complOccScore, Double similarity, DiffCandidate candidate) {
      this.complOccScore = complOccScore;
			this.similarity = similarity;
      this.candidate = candidate;
    }
    
		////////////////////
		// Getters
		////////////////////
    Double getComplementaryOccScore() {
      return this.complOccScore;
    }
    
    DiffCandidate getCandidate() {
      return this.candidate;
    }

		Double getSimilarity() {
			return this.similarity;
		}

    @Override
    public String toString() {
      return "TripleComplOccScoreSimilarityCandidate [similarity=" + similarity + ", complOccScore=" + complOccScore
          + ", candidate=" + candidate + "]";
    }
	}
  
	/**
	* Helper container class that holds:
	* <p>
	* <ul>
	*		<li> score evaluating how "complementary" the occurrence is, 
  *		<li> vertex similarity,
	*		<li> EMD,
	*		<li> the candidate
	* </ul>
	*
	* (Easier to use in streams because getters have types that are not generics) 
	*/
  class OccSimEMDCandidate {
		/**
		* Similarity score
		*/
		Double similarity;

		/**
		* Similarity score
		*/
    Double complOccScore;
    
    /**
     * EMD Difference w.r.t. to original candidate.
     */
    Double emdDiff;
    
		/**
		* Difference candidate
		*/
		DiffCandidate candidate;

    OccSimEMDCandidate(Double complOccScore, Double similarity, Double emdDiff, DiffCandidate candidate) {
      this.complOccScore = complOccScore;
			this.similarity = similarity;
      this.candidate = candidate;
      this.emdDiff = emdDiff;
    }
    
		////////////////////
		// Getters
		////////////////////
    Double getComplementaryOccScore() {
      return this.complOccScore;
    }
    
    DiffCandidate getCandidate() {
      return this.candidate;
    }

		Double getSimilarity() {
			return this.similarity;
		}
		
		Double getEMDDiff() {
		  return this.emdDiff;
		}

    @Override
    public String toString() {
      return "OccSimEMDCandidate [similarity=" + similarity + ", complOccScore=" + complOccScore + ", emdDiff=" + emdDiff
          + ", candidate=" + candidate + "]";
    }
		
	}
}

