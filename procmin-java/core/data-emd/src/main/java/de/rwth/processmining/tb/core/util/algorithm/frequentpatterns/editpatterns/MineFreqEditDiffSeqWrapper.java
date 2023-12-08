package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.editpatterns;

import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import de.rwth.processmining.tb.core.emd.dataview.DescriptorDistancePair;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDistEditDiagnose;
import de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics.EditSequence;
import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.solutiondata.NonZeroFlows;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.prefixspan.AlgoPrefixSpan;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.prefixspan.SequentialPatterns;

public class MineFreqEditDiffSeqWrapper {
	
  /**
   * 
   * @param <F> Feature type
   * @param <D> A feature distance that extends {@link TraceDistEditDiagnose} (to provide edit operation traces)
   * @param nzFlows Non zero flow in solutions
   * @param langL Language left side
   * @param langR Language right side
   * @param extractorDistancePair 
   * @return
   */
	public static<F extends TraceDescriptor, D extends TraceDistEditDiagnose<F>> List<FreqEditOpReprSequence> 
	  mineFreqEditDiffSeqOnNzFlows(NonZeroFlows nzFlows, 
	    OrderedStochasticLanguage<F> langL, OrderedStochasticLanguage<F> langR,
			DescriptorDistancePair<F, D> extractorDistancePair) {
		
		EditDifferenceConnectorBuilder builder = new EditDifferenceConnectorBuilder();
		

		for(Triple<Integer, Integer, Double> nzFlowEdge : nzFlows) {
			int indL = nzFlowEdge.getLeft();
			int indR = nzFlowEdge.getMiddle();
			double flow = nzFlowEdge.getRight();
			EditSequence editSeq = extractorDistancePair.getDistance().get_distance_op(langL.get(indL), langR.get(indR));
			builder.addToDataset(editSeq, langL.get(indL), langR.get(indR), flow);
		}
		
		EditDifferenceConnection seqMinerConnection = builder.build();	
		AlgoPrefixSpan algo = new AlgoPrefixSpan();
		SequentialPatterns patterns = algo.runAlgorithm(seqMinerConnection.getSeqDatabase(), 0.01);
		
		List<FreqEditOpReprSequence> lFreqEditOp = seqMinerConnection.getFreqEditSequences(patterns);
		
		return lFreqEditOp;
	}

}
