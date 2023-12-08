package de.rwth.processmining.tb.core.emd.dataview;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.comparison.WindowDiagnosticsData;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.ViewConfigVariant;
import de.rwth.processmining.tb.core.emd.dataview.xlogbased.DetailedViewRealizationXLog;
import de.rwth.processmining.tb.core.emd.dataview.xlogbased.ViewConfigXlog;
import de.rwth.processmining.tb.core.emd.dataview.xlogbased.ViewRealizationXLog;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDistEditDiagnose;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class ViewRealizer {
	private final static Logger logger = LogManager.getLogger( ViewRealizer.class );
  
	////////////////////////////////////////////////////////////////////////////////
	// XLog
	////////////////////////////////////////////////////////////////////////////////
	/**
	 * Realize a view on the provided <b>data</b> (xlogs) given the provided <b>view configuration</b> .
	 * 
	 * @param <F> Feature Type
	 * @param <D> Distance Calculator type (needs to match the feature type F)
	 * @param data Data (two XLogs)
	 * @param viewConfig View config to instantiate EMD
	 * @param description Description that is added to the created view
	 * @return Created view or null if view config is not fully specified
	 */
  public static<F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
    ViewRealizationXLog<F, D> realizeViewOnXLog(
      WindowDiagnosticsData data, ViewConfigXlog<F, D> viewConfig, PerspectiveDescriptor description) {
    
    if (checkIfXlogViewParameterized(viewConfig)) {
			return new ViewRealizationXLog<>(new ViewRealizationMeta(viewConfig.getViewIdentifier(), description), 
			    viewConfig.getExtractorDistancePair(), 
			    viewConfig.getLangTransformer(), 
			    data);
    }
		else {
      return null;
		}
  }
  
	public <F extends TraceDescriptor, D extends TraceDistEditDiagnose<F>> DetailedViewRealizationXLog<F, D> 
	  createViewOnData(
      WindowDiagnosticsData data, ViewConfigXlog<F, D> viewConfig, PerspectiveDescriptor description) {
    if (checkIfXlogViewParameterized(viewConfig)) {
      return new DetailedViewRealizationXLog<>(new ViewRealizationMeta(viewConfig.getViewIdentifier(), description), 
			    viewConfig.getExtractorDistancePair(), 
			    viewConfig.getLangTransformer(), 
			    data);
    }
		else {
      return null;
		}
	}

	////////////////////////////////////////////////////////////////////////////////
	// Variant Log
	////////////////////////////////////////////////////////////////////////////////
	/**
	 * Realize a view on the provided <b>data</b> (variant log) given the provided <b>view configuration</b> .
	 * 
	 * @param <V> Minimum Variant type required as input
	 * @param <F> Feature Type
	 * @param <D> Distance Calculator type (needs to match the feature type F)
	 * @param data Data (two XLogs)
	 * @param viewConfig View config to instantiate EMD
	 * @param description Description that is added to the created view
	 * @return Created view or null if view config is not fully specified
	 */
  public static<V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
    ViewRealizationSLDS<V, F, D> realizeViewOnVariantLog(
      BiComparisonDataSource<? extends V> data, ViewConfigVariant<V, F, D> viewConfig, 
      PerspectiveDescriptor description) {
    
    if (checkIfVariantViewParameterized(viewConfig)) {
			return new ViewRealizationSLDS<>(new ViewRealizationMeta(viewConfig.getViewIdentifier(), description), 
            viewConfig.getExtractorDistancePair(), viewConfig.getLangTransformer(), data);
    }
		else {
      return null;
		}
  }
  
  
  
  ////////////////////////////////////////////////////////////
  // Check if View Configs are Fully Specified
  ////////////////////////////////////////////////////////////
  public static<V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
    boolean checkIfVariantViewParameterized(
      ViewConfigVariant<V, F, D> viewConfig) {

    boolean b = checkIfViewParameterized(viewConfig);
    if (b) {
      if (viewConfig.getExtractorDistancePair() == null) {
        logger.error("Cannot create view realization: No extractor-distance pair specified");
        return false;
      }
      return true;
    }
    else {
      return false;
    }
  }

  public static<F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> boolean checkIfXlogViewParameterized(
      ViewConfig viewConfig) {

		if(viewConfig.getLangTransformer() == null) {
			logger.error("Cannot create view realization: No language transformer specified");
			return false;
		}
		else if(viewConfig.getViewIdentifier() == null) {
			logger.error("Cannot create view realization: No view identifier specified");
			return false;
		}
		return true;
  }
  
  public static boolean checkIfViewParameterized(ViewConfig viewConfig) {
		if(viewConfig.getLangTransformer() == null) {
			logger.error("Cannot create view realization: No language transformer specified");
			return false;
		}
		else if(viewConfig.getViewIdentifier() == null) {
			logger.error("Cannot create view realization: No view identifier specified");
			return false;
		}
		return true;
  }

}
