package de.rwth.processmining.tb.core.emd.dataview;

import java.util.Collection;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.emd.dataview.xlogbased.MultiViewConfigXLog;
import de.rwth.processmining.tb.core.emd.dataview.xlogbased.ViewConfigXlog;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDistEditDiagnose;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.XLogTraceFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2SimpleNormOrdStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ContextAwareEmptyTraceBalancedTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ScalingContext;

public class MultiViewSpecFactory {
	private final static Logger logger = LogManager.getLogger( MultiViewSpecFactory.class.getName());

	public static<F extends TraceDescriptor, D extends TraceDistEditDiagnose<F>> MultiViewConfigXLog<F, D>
	  createConfigureMultiViewHierarchy(XLogTraceFeatureExtractor<F> langFac, D trDescDist, 
			int logSizeL, int logSizeR, XLog xlog) { 
		MultiViewConfigXLog<F, D> viewConfig = createMultiViewHierarchy(langFac, trDescDist, logSizeL, logSizeR);
		configureComponentsUsed(viewConfig, xlog, false);
		return viewConfig;
	}
	
	public static<F extends TraceDescriptor, D extends TraceDistEditDiagnose<F>> MultiViewConfigXLog<F, D> 
	  createMultiViewHierarchy(
	    XLogTraceFeatureExtractor<F> langFac, D trDescDist, int logSizeL, int logSizeR) {
//		Window2OrderedStochLangTransformer tFreqBalancedModel = new Window2EmptyTraceBalancedOrdStochLangTransformer(1.0 / logSizeL, 1.0 / logSizeR, ScalingContext.MODEL);
//		Window2OrderedStochLangTransformer tFreqBalancedGlobal = new Window2EmptyTraceBalancedOrdStochLangTransformer(1.0 / logSizeL, 1.0 / logSizeR, ScalingContext.GLOABAL);
		ContextAwareEmptyTraceBalancedTransformer tFreqBalancedModel = new ContextAwareEmptyTraceBalancedTransformer(logSizeL, logSizeR, ScalingContext.MODEL);
		ContextAwareEmptyTraceBalancedTransformer tFreqBalancedGlobal = new ContextAwareEmptyTraceBalancedTransformer(logSizeL, logSizeR, ScalingContext.GLOBAL);
		Window2OrderedStochLangTransformer tSimpNorm = new Window2SimpleNormOrdStochLangTransformer();
		
		DescriptorDistancePair<F, D> pTop = new DescriptorDistancePair<>(trDescDist, langFac);

		ViewConfigXlog<F, D> topViewBalancedGlobal = new ViewConfigXlog<F, D>(tFreqBalancedGlobal, pTop, 
				new ViewIdentifier(pTop.getShortDescription() + " - " + tFreqBalancedGlobal.getShortDescription()));
		ViewConfigXlog<F, D> topViewBalancedModel = new ViewConfigXlog<F, D>(tFreqBalancedModel, pTop, 
				new ViewIdentifier(pTop.getShortDescription() + " - " + tFreqBalancedModel.getShortDescription()));
		ViewConfigXlog<F, D> topViewSimpNorm = new ViewConfigXlog<F, D>(tSimpNorm, pTop,
				new ViewIdentifier(pTop.getShortDescription() + " - " + tSimpNorm.getShortDescription()));
		
		MultiViewConfigXLog<F, D> viewConfig = new MultiViewConfigXLog<F, D>();
		viewConfig.setTopLevelView(topViewBalancedGlobal);
		viewConfig.addSubView(topViewBalancedModel);
		viewConfig.addSubView(topViewSimpNorm);
    Collection<DescriptorDistancePair<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>>> subConfigs = 
      DescriptorDistancePairFactory.availableSubViewPairsFor(pTop);
		for(DescriptorDistancePair<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>> p : subConfigs) {
			viewConfig.addSubView(new ViewConfigXlog<>(tFreqBalancedGlobal, p, 
					new ViewIdentifier(p.getShortDescription() + " - " + tFreqBalancedGlobal.getShortDescription())));
			viewConfig.addSubView(new ViewConfigXlog<>(tFreqBalancedModel, p, 
					new ViewIdentifier(p.getShortDescription() + " - " + tFreqBalancedModel.getShortDescription())));
			viewConfig.addSubView(new ViewConfigXlog<>(tSimpNorm, p,
					new ViewIdentifier(p.getShortDescription() + " - " + tSimpNorm.getShortDescription())));
		}
		
		return viewConfig;

	}
	
	public static<F extends TraceDescriptor, D extends TraceDistEditDiagnose<F>> void configureComponentsUsed(MultiViewConfigXLog<F, D> viewConfig, XLog xlog, boolean overwrite) {
		Iterator<ViewConfigXlog<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>>> itView = viewConfig.getViewIterator();
		while(itView.hasNext()) {
			ViewConfigXlog<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>> v = itView.next();
			logger.debug("Configuring View: \n " + v.toString());
			v.getExtractorDistancePair().getDescriptorFactory().init(xlog, overwrite);
		}
			
	}
}
