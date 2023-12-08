package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.XEventCondition;
import org.processmining.plugins.log.logfilters.XTraceCondition;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.StochasticLangDataSourceTransformer;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.transform.CVariantCondition;
import de.rwth.processmining.tb.core.data.variantlog.transform.ContainsCategoryCondition;
import de.rwth.processmining.tb.core.data.variantlog.util.VariantCopyingException;

public class SLDSFilterVariantsContainsActivity<E extends CVariant> extends StochasticLangDataSourceTransformer<E> { 

	private final static Logger logger = LogManager.getLogger( SLDSFilterVariantsContainsActivity.class );

	/**
	 * Used event classifier.
	 */
	private final XEventClassifier classifier;

	/**
	 * Activity that a trace should / should not contain
	 */
	private final String activity;
	
	/**
	 * Keep the traces that contain activity (true) or drop them (false).
	 * Defaults to true
	 */
	private final boolean keep;

	public SLDSFilterVariantsContainsActivity(StochasticLanguageDataSource<E> stochLangDataSource, 
			String activity, XEventClassifier classifier, boolean keep) {
		super(stochLangDataSource);
		this.activity = activity;
		this.classifier = classifier;
		this.keep = keep;
	}
	
	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		XLog log = super.getDataRawTransformed();
		XTraceCondition traceCond;
		// Depending on whether we want to keep the traces
		traceCond = new XTraceCondition() {
			
			@Override
			public boolean keepTrace(XTrace trace) {
					for(XEvent e : trace) {
						if(activity.equals(classifier.getClassIdentity(e))) {
							return keep;
						}
					}
				return !keep;
			}
		};
		log = LogFilter.filter(null, 100, log, null,
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						// Keep all events
						return true;
					}
				}, 
				traceCond
				);
		
		return log;
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		CVariantLog<E> log = super.getVariantLog();
		Integer category = log.getCategoryMapper().getCategory4Activity(activity);
		// Filtering on an activity that is not even in the log
		if(category == null) {
			// None of the traces will contain this activity
			if(keep) {
				return log.getEmptyCopy();
			}
			else {
				// No trace contains it so nothing will be dropped
				return log;
			}
		}
		else {
			CVariantCondition<E> variantCondition = new ContainsCategoryCondition<>(category);
			try {
				log = log.filterVariantByCondition(variantCondition, keep);
			}
			catch(VariantCopyingException e) {
				logger.error("Error in Variant condition filterin", e);
				throw new SLDSTransformationError("Transformation failed: "  + e.getMessage());
			}
			return log;
				
		}
	}

}
