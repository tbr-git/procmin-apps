package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

public class SLDSFilterVariantAnyActivity<E extends CVariant> extends StochasticLangDataSourceTransformer<E> { 

	private final static Logger logger = LogManager.getLogger( SLDSFilterVariantAnyActivity.class );
	
	/**
	 * Used event classifier.
	 */
	private final XEventClassifier classifier;

	/**
	 * At least one of these activities should be contains; or
	 * None of these activities must occur.
	 */
	private final String[] activities;
	
	/**
	 * Keep the traces that contain any activity (true) or drop if any is contained (false).
	 * Defaults to true
	 */
	private final boolean keep;
	
	public SLDSFilterVariantAnyActivity(StochasticLanguageDataSource<E> stochLangDataSource, 
			String[] activities, XEventClassifier classifier, boolean keep) {
		super(stochLangDataSource);
		this.classifier = classifier;
		this.activities = activities;
		this.keep = keep;
	}
	
	
	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		XLog log = super.getDataRawTransformed();
		
		Set<String> activityOptions = Arrays.stream(activities).collect(Collectors.toSet());
		XTraceCondition traceCond;
		// Depending on whether we want to keep the traces
		traceCond = new XTraceCondition() {
			
			@Override
			public boolean keepTrace(XTrace trace) {
					for(XEvent e : trace) {
						if(activityOptions.contains(classifier.getClassIdentity(e))) {
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
		CategoryMapper catMapper = log.getCategoryMapper();
		int[] categories = Arrays.stream(this.activities) // Stream
				.map(catMapper::getCategory4Activity) // Get Code
				.filter(Objects::nonNull) // Filter null 
				.mapToInt(Integer::intValue).toArray(); // To array
		
		return SLDSFilterVariantsContainsCategory.applyCategoryFilter(log, categories, keep);
	}
}
