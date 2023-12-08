package de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagement;
import de.rwth.processmining.tb.core.sps.algorithm.spsvertex.SPSVertexStatistics;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurement;


public class IVFilterIndepentDiff extends InterestingVertexFinder {
	private static final Logger logger = LogManager.getLogger(IVFilterIndepentDiff.class);
  
  
  /**
   * Threshold for the phi coefficient
   */
  private double tPhi;

  /**
   * If the independence test (phi coefficient) cannot be calculated
   * because the merged vertex (u+v) is not part of the graph
   *
   */
  private int failedIndependenceTest = 0;

  public IVFilterIndepentDiff(HFDDIterationManagement hfddItMan, int iteration, double tMetric, double tDom, double tPhi) {
    // We pass a value 1.0 as tSurprise
    // Note that the base class only uses the value in the method that is overriden here
    // => We don't need it and it does not have any effect
    super(hfddItMan, iteration, tMetric, 1.0, tDom);


    this.tPhi = tPhi;
  }


  @Override
	public Collection<DiffCandidate> findInterestingVertices() {
    this.failedIndependenceTest = 0;
    Collection<DiffCandidate> res = super.findInterestingVertices();

    if (this.failedIndependenceTest > 0) {
      logger.warn("Vertices potentially slipped through phi coefficient-based " +
        "occurrence association test because merged vertex not contained in graph " + this.failedIndependenceTest);
    }

    return res;
  }


  @Override
	protected SubSPIType getTypeBasedOnPredecessors(HFDDVertex v, boolean isVBelowT, List<HFDDVertex> subIncPredecessors) {
		PerspectiveDescriptor pDesc = 
					hfddItMan.getPerspective4Iteration(iteration);

		final HFDDMeasurement measurementV = v.getVertexInfo().getMeasurements().get(pDesc);
		
		
		// Remove ignored vertices
    ArrayList<HFDDVertex> subIncPredNonIgnored = subIncPredecessors.stream().filter(
				u -> subSPITypes[u.getId()] != SubSPIType.IGNORE)
      .collect(Collectors.toCollection(ArrayList::new));

		// Base case
		if (subIncPredNonIgnored.size() == 0) {
			// NO predecessor
			if (isVBelowT) {
				return SubSPIType.PURE_UNINTERESTING;
			}
			else {
				return SubSPIType.INTERESTING;
			}
		}
		else {
			// Count sub-SPI types 
			Map<SubSPIType, Long> subSPICounts = subIncPredNonIgnored.stream().map(u -> subSPITypes[u.getId()])
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		
			// All interesting
			if (subSPICounts.getOrDefault(SubSPIType.INTERESTING, -1L) == subIncPredNonIgnored.size()) {
				if (isVBelowT) {
					return SubSPIType.SUB_INTERESTING;
				}
				else {
          // Check if difference is sub-interesting because parents' occurrence
          // is only weakly associated
          if (check4OccurrenceIndependence(v, subIncPredNonIgnored)) {
            return SubSPIType.SUB_INTERESTING;
          }
          else {
            return SubSPIType.INTERESTING;
          }
				}
			}
			// All uninteresting
			else if (subSPICounts.getOrDefault(SubSPIType.PURE_UNINTERESTING, -1L) == subIncPredNonIgnored.size()) {
				if (isVBelowT) {
					return SubSPIType.PURE_UNINTERESTING;
				}
				else {
					return SubSPIType.INTERESTING;
				}
			}
			// Mix of interesting and uninteresting
			else if (subSPICounts.getOrDefault(SubSPIType.INTERESTING, 0L) + 
					subSPICounts.getOrDefault(SubSPIType.PURE_UNINTERESTING, -0L) == subIncPredNonIgnored.size()) {

				if (isVBelowT) {
					return SubSPIType.SUB_INTERESTING;
				}
				else {
          OptionalDouble maxPredMetric = subIncPredNonIgnored.stream().mapToDouble(
              u -> u.getVertexInfo().getMeasurements().get(pDesc).getMetric().orElse(0.0)).max();

          // Potentially interesting because score is much larger than v's parents' scores
					if (measurementV.getMetric().orElse(-1.0) > tSurprise * maxPredMetric.orElse(0.0)) {
            // Check if difference is sub-interesting because parents' occurrence
            // is only weakly associated
            if (check4OccurrenceIndependence(v, subIncPredNonIgnored)) {
              return SubSPIType.SUB_INTERESTING;
            }
            else {
              return SubSPIType.INTERESTING;
            }
					}
					else {
					  // If combining all interesting parents yields this vertex, 
					  // each activity was at least interesting in one projection
					  // => We consider this vertex interesting as well 
					  BitSet aggInterestingActivities = new BitSet(v.getVertexInfo().getActivities().size());
					  for (HFDDVertex u : subIncPredNonIgnored) {
					      if (subSPITypes[u.getId()] != SubSPIType.INTERESTING) {
					        continue;
					      }
                aggInterestingActivities.or(u.getVertexInfo().getActivities());
					  }
					  
					  if (v.getVertexInfo().getActivities().equals(aggInterestingActivities)) {
					    //TODO Test
              if (check4OccurrenceIndependence(v, subIncPredNonIgnored)) {
                return SubSPIType.SUB_INTERESTING;
              }
              else {
                return SubSPIType.INTERESTING;
              }
					  }
					  else {
              return SubSPIType.SUB_INTERESTING;
					  }
						//return SubSPIType.SUB_INTERESTING;
					}
				}
			}
			else {
				return SubSPIType.SUB_INTERESTING;
			}
		}
	}

  private boolean check4OccurrenceIndependence(HFDDVertex v, ArrayList<HFDDVertex> predecessors) {

    int nbrActV = v.getVertexInfo().getActivities().cardinality();

    // Is v a combination of two occurrence-independent, interesting predecessors?
    boolean diffIsIndependentComb = false;

    // Did we miss a phi computation because v is not the union of both parents?
    boolean missedPhiComputation = false;

    for (int i = 0; i < predecessors.size() - 1; i++){
      HFDDVertex p1 = predecessors.get(i);
      // Skip vertices that are not interesting
      if (subSPITypes[p1.getId()] != SubSPIType.INTERESTING) {
        continue;
      }
      for (int j = i + 1; j < predecessors.size(); j++){
        HFDDVertex p2 = predecessors.get(j);
        // Skip vertices that are not interesting
        if (subSPITypes[p2.getId()] != SubSPIType.INTERESTING) {
          continue;
        }

        // Check if v is the union of p1 and p2
        // Since both are predecessors, this is the case iff itemsets differ by one
        if(! ((p1.getVertexInfo().getActivities().cardinality() == nbrActV - 1) 
            && (p2.getVertexInfo().getActivities().cardinality() == nbrActV - 1))) {
          missedPhiComputation = true;
        }
        else {
          // Are p1 and p2 weakly associated in both processes
          diffIsIndependentComb =  Math.abs(SPSVertexStatistics.vertexOccPhiCoeff(p1, p2, v, true)) < tPhi 
            && Math.abs(SPSVertexStatistics.vertexOccPhiCoeff(p1, p2, v, false)) < tPhi;

          if (diffIsIndependentComb) {
            break;
          }
        }
      }
      if (diffIsIndependentComb) {
        break;
      }
    }

    // In case none of the parents are only weakly associated
    // => Maybe a pair that we could not evaluate is?
    if (!diffIsIndependentComb && missedPhiComputation) {
      this.failedIndependenceTest++;
    }
    return diffIsIndependentComb;
  }

}

