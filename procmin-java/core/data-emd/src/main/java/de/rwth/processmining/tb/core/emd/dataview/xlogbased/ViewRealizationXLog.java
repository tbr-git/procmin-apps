package de.rwth.processmining.tb.core.emd.dataview.xlogbased;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.comparison.WindowDiagnosticsData;
import de.rwth.processmining.tb.core.emd.dataview.DescriptorDistancePair;
import de.rwth.processmining.tb.core.emd.dataview.RealizabilityChecker;
import de.rwth.processmining.tb.core.emd.dataview.RealizabilityInfo;
import de.rwth.processmining.tb.core.emd.dataview.ViewDataException;
import de.rwth.processmining.tb.core.emd.dataview.ViewRealization;
import de.rwth.processmining.tb.core.emd.dataview.ViewRealizationMeta;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.solver.EMDSolver;

public class ViewRealizationXLog<F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
    extends ViewRealization<F> {

	private final static Logger logger = LogManager.getLogger( ViewRealizationXLog.class );
	
	/**
	 * How the features where transformed into a stochastic language
	 */
	private final Window2OrderedStochLangTransformer langTransformer;
	
	/**
	 * Pair: (Feature extractor from Xlog, Feature distance)
	 */
	private final DescriptorDistancePair<F, D> descDistPair;
	
	/**
	 * Optional: data in terms of {@link XLog}.
	 */
	private Optional<WindowDiagnosticsData> data;

	/**
	 * Information on realizability
	 */
	private final RealizabilityInfo viewRealInfo;

	public ViewRealizationXLog(ViewRealizationMeta viewDescription, 
	    DescriptorDistancePair<F, D> descDistPair, 
	    Window2OrderedStochLangTransformer langTransformer, 
			WindowDiagnosticsData data) {
		super(viewDescription);
		this.langTransformer = langTransformer;
		this.descDistPair = descDistPair;
		this.data = Optional.of(data);
		this.viewRealInfo = RealizabilityChecker.checkRealizability(viewDescription, descDistPair, langTransformer, data);
	}
	
	@Override
	public boolean isRealizable() {
		return viewRealInfo.isRealizable();
	}

	public void populate() throws ViewDataException {
		if(viewRealInfo.isRealizable()) {
			if(!emdSol.isPresent() && viewRealInfo.isRealizable()) {
				emdSol = Optional.of(EMDSolver.getLPSolution(data.get().getXLogLeft().iterator(), data.get().getXLogRight().iterator(), 
						descDistPair.getDescriptorFactory(), descDistPair.getDistance(), langTransformer));
				// Free for garabage collection -> memory management
				data = Optional.empty();
			}
		}
		else {
			throw new ViewDataException(viewRealInfo);
		}
	}
	
	protected DescriptorDistancePair<F, D> getExtractorDistancePair() {
	  return this.descDistPair;
	}

}
