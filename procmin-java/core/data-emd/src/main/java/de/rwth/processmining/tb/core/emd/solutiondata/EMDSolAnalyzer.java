package de.rwth.processmining.tb.core.emd.solutiondata;

import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.StochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.StochasticLanguageIterator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class EMDSolAnalyzer {
  
  /**
   * Given an EMD solution, get the total non-empty trace probability mass
   * @param <F> Feature type
   * @param emdSol Solution
   * @return Probability non-empty trace left, right
   */
	public static<F extends TraceDescriptor> Pair<Double, Double> getProbabilityNonEmpty(EMDSolContainer<F> emdSol) {

	  double probNonZeroLeft = getProbabilityNonEmpty(emdSol.getLanguageLeft());
	  double probNonZeroRight = getProbabilityNonEmpty(emdSol.getLanguageRight());
	  
	  return Pair.of(probNonZeroLeft, probNonZeroRight);
	}

  /**
   * Given a stochastic language solution, get the total non-empty trace probability mass
   * 
   * @param <F> Feature type
   * @param stochLang Stochastic language
   * @return Total probability non-empty trace 
   */
	public static<F extends TraceDescriptor> double getProbabilityNonEmpty(StochasticLanguage<F> stochLang) {
		double probNonZero = 1;
		StochasticLanguageIterator<F>  itL = stochLang.iterator();
		while (itL.hasNext()) {
			if(itL.next().length() == 0) {
				probNonZero -= itL.getProbability();
				break;
			}
		}
		return probNonZero;
	}
  
  public static<F extends TraceDescriptor> double flowInvolvingEmpty(EMDSolContainer<F> emdSol) {
		int indexEmptyTraceLeft = getIndexEmptyTrace(emdSol.getLanguageLeft());
		int indexEmptyTraceRight = getIndexEmptyTrace(emdSol.getLanguageRight());
		
		if (indexEmptyTraceLeft == -1 && indexEmptyTraceRight == -1) {
			return 0;
		}

		double flowEmpty = StreamSupport.stream(emdSol.getNonZeroFlows().spliterator(), false)
			.filter(t -> (t.getLeft() == indexEmptyTraceLeft || t.getMiddle() == indexEmptyTraceRight))
			.mapToDouble(Triple::getRight)
			.sum();
		
		return flowEmpty;
  }
	
	public static<F extends TraceDescriptor> double flowEmptyCost(EMDSolContainer<F> emdSol) {
	
		int indexEmptyTraceLeft = getIndexEmptyTrace(emdSol.getLanguageLeft());
		int indexEmptyTraceRight = getIndexEmptyTrace(emdSol.getLanguageRight());
		
		if (indexEmptyTraceLeft == -1 && indexEmptyTraceRight == -1) {
			return 0;
		}
		
		
		double flow2EmptyCost = StreamSupport.stream(emdSol.getNonZeroFlows().spliterator(), false)
			.filter(t -> (t.getLeft() == indexEmptyTraceLeft || t.getMiddle() == indexEmptyTraceRight))
			.mapToDouble(t -> emdSol.getCost(t.getLeft(), t.getMiddle()) * t.getRight())
			.sum();
			
		return flow2EmptyCost;
	}
	
	private static<F extends TraceDescriptor> int getIndexEmptyTrace(OrderedStochasticLanguage<F> stochLang) {
		int indexEmptyTrace = -1;
		int i = 0;
		StochasticLanguageIterator<F>  itL = stochLang.iterator();
		while (itL.hasNext() && indexEmptyTrace == -1) {
			if(itL.next().length() == 0) {
				indexEmptyTrace = i;
			}
			i++;
		}
		
		return indexEmptyTrace;
	}
}
