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
import de.rwth.processmining.tb.core.data.variantlog.util.VariantCopyingException;


/**
 * Filter data source and retain only cases/variants that contain a given activity category.
 * @author brockhoff
 *
 * @param <E> Encapsulated variant type
 */
public class StochLangDataSourceFilterMandatoryCategory<E extends CVariant> extends StochasticLangDataSourceTransformer<E> {

	private final static Logger logger = LogManager.getLogger( StochLangDataSourceFilterMandatoryCategory.class );
	/**
	 * Mandatory activities.
	 */
	private final int[] activities;

	/**
	 * Used event classifier.
	 */
	private final XEventClassifier classifier;
	
	/**
	 * Category mapper used to translate the categories back into activity names.
	 * Required in case filtering on the XLog is needed.
	 * 
	 */
	private final CategoryMapper categoryMapper;

	public StochLangDataSourceFilterMandatoryCategory(StochasticLanguageDataSource<E> stochLangDataSource, 
			int[] activities, XEventClassifier classifier, CategoryMapper categoryMapper) {
		super(stochLangDataSource);
		this.activities = activities;
		this.classifier = classifier;
		this.categoryMapper = categoryMapper;
	}


	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		Set<String> activityNames = new HashSet<>();
		for(int a : this.activities) {
			activityNames.add(categoryMapper.getActivity4Category(a));
		}

		XLog log = super.getDataRawTransformed();
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
						Set<String> uniqueEvents = new HashSet<>(trace.size());
						for(XEvent e : trace) {
							uniqueEvents.add(classifier.getClassIdentity(e));
						}
						// Keep trace if mandatory activities are a subset of the contained 
						// (unique) activities
						return uniqueEvents.containsAll(activityNames);
					}
				});
		
		return log;
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		CVariantLog<E> log = super.getVariantLog();
		try {
			log = log.filterTracesMandatoryActivities(activities);
		} catch (VariantCopyingException e) {
			logger.error("Could not filter traces on mandatory activities");
			e.printStackTrace();
			throw new SLDSTransformationError("Failed to filter the traces on mandatory activities: \n" + e.getMessage());
		}
		return log;
	}
}
