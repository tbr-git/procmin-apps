package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection;

import java.util.HashSet;
import java.util.Set;

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
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.data.variantlog.transform.CVariantCondition;
import de.rwth.processmining.tb.core.data.variantlog.transform.ContainsAnyCategoryCondition;
import de.rwth.processmining.tb.core.data.variantlog.util.VariantCopyingException;

public class SLDSFilterVariantsContainsCategory<E extends CVariant> extends StochasticLangDataSourceTransformer<E> { 
	private final static Logger logger = LogManager.getLogger( SLDSFilterVariantsContainsCategory.class );

	/**
	 * At least one of these activities (category codes) should be contains; or
	 * None of these activities must occur.
	 */
	private int[] activityCodes;

	/**
	 * Used event classifier.
	 */
	private XEventClassifier classifier;
	
	/**
	 * Category mapper required when using 
	 */
	private CategoryMapper categoryMapper;

	/**
	 * Keep the traces that contain activity (true) or drop them (false).
	 * Defaults to true
	 */
	private final boolean keep;
	
	public SLDSFilterVariantsContainsCategory(StochasticLanguageDataSource<E> stochLangDataSource, 
			int[] activities, XEventClassifier classifier, CategoryMapper categoryMapper, boolean keep) {
		super(stochLangDataSource);
		this.activityCodes = activities;
		this.classifier = classifier;
		this.categoryMapper = categoryMapper;
		this.keep = keep;
	}

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		Set<String> activityNames = new HashSet<>();
		for(int a : activityCodes) {
			activityNames.add(categoryMapper.getActivity4Category(a));
		}

		XLog log = super.getDataRawTransformed();
		// Depending on whether we want to keep the traces
		log = LogFilter.filter(null, 100, log, null,
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						// Keep all events
						return true;
					}
				},
				new XTraceCondition() {
			
					@Override
					public boolean keepTrace(XTrace trace) {
							for(XEvent e : trace) {
								if(activityNames.contains(classifier.getClassIdentity(e))) {
									return keep;
								}
							}
						return !keep;
					}
				}
			);
		
		return log;
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		CVariantLog<E> log = super.getVariantLog();
		return SLDSFilterVariantsContainsCategory.applyCategoryFilter(log, this.activityCodes, keep);
	}
	
	protected static<T extends CVariant> CVariantLog<T> applyCategoryFilter(CVariantLog<T> log, int[] categories, boolean keep) throws SLDSTransformationError {
		// Filtering on activities that are not even in the log
		if(categories.length == 0) {
			// None of the traces will contain any of the activities
			if(keep) {
				return log.getEmptyCopy();
			}
			else {
				// No trace contains it so nothing will be dropped
				return log;
			}
		}
		else {
			CVariantCondition<T> variantCondition = new ContainsAnyCategoryCondition<>(categories);
			try {
				log = log.filterVariantByCondition(variantCondition, keep);
			}
			catch(VariantCopyingException e) {
				logger.error("Error in Variant condition filtering", e);
				throw new SLDSTransformationError("Transformation failed: "  + e.getMessage());
			}
			return log;
		}
	}
}
