package de.rwth.processmining.tb.core.emd.dataview.xlogbased;

import java.util.List;
import java.util.Optional;

import de.rwth.processmining.tb.core.data.comparison.WindowDiagnosticsData;
import de.rwth.processmining.tb.core.emd.dataview.DescriptorDistancePair;
import de.rwth.processmining.tb.core.emd.dataview.ViewDataException;
import de.rwth.processmining.tb.core.emd.dataview.ViewRealizationMeta;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDistEditDiagnose;
import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.editpatterns.FreqEditOpReprSequence;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.editpatterns.MineFreqEditDiffSeqWrapper;

public class DetailedViewRealizationXLog<F extends TraceDescriptor, D extends TraceDistEditDiagnose<F>> 
    extends ViewRealizationXLog<F, D> {
	
	private Optional<List<FreqEditOpReprSequence>> freqEditSeqRepresentative;
	
	public DetailedViewRealizationXLog(ViewRealizationMeta viewDescription, 
	    DescriptorDistancePair<F, D> descDetailedDistPair, 
	    Window2OrderedStochLangTransformer langTransformer, 
	    WindowDiagnosticsData data) {
		super(viewDescription, descDetailedDistPair, langTransformer, data);
		this.freqEditSeqRepresentative = Optional.empty();
	}
	
	public List<FreqEditOpReprSequence> getFrequentEditSequenceRepresentatives() {
		if(!freqEditSeqRepresentative.isPresent()) {
			mineFrequentEditSequenceRepresentatives();
		}
		return freqEditSeqRepresentative.get();
	}
	
	public void mineFrequentEditSequenceRepresentatives() {
		EMDSolContainer<F> emdSol = null ; 
		try {
			emdSol = this.getEMDSol(); 
		} catch (ViewDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		OrderedStochasticLanguage<F> langL = emdSol.getLanguageLeft();
		OrderedStochasticLanguage<F> langR = emdSol.getLanguageRight();
		freqEditSeqRepresentative = Optional.of(MineFreqEditDiffSeqWrapper.mineFreqEditDiffSeqOnNzFlows(
		    emdSol.getNonZeroFlows(), langL, langR, getExtractorDistancePair()));
		
	}

}
