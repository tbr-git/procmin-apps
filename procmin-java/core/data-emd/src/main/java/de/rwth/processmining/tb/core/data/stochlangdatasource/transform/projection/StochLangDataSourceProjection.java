package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.projection;

import java.util.BitSet;
import java.util.Collection;

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

/**
 * 
 * Project the data data source/variants on the provides activities.
 * 
 * @author brockhoff
 *
 * @param <E> Encapsulated variant type
 */
public abstract class StochLangDataSourceProjection<E extends CVariant> extends StochasticLangDataSourceTransformer<E> {

	
	public StochLangDataSourceProjection(StochasticLanguageDataSource<E> stochLangDataSource) {
		super(stochLangDataSource);
	}
	
	/**
	 * Get the activities to project on using their real labels.
	 * @return Collection of activity labels
	 */
	protected abstract Collection<String> getActivityLabels();

	/**
	 * Get the activities to project on based on their categories.
	 * Override this method if codes are available to improve speed.
	 * If method is not overwritten, this{@link #getActivityLabels()} will be used as a fallback.
	 * @return BitSet of activity categories
	 */
	protected BitSet getActivityCodes() {
		return null;
	}
	

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		XLog log = super.getDataRawTransformed();
		Collection<String> projActivities = getActivityLabels();
		XEventClassifier classifier = getClassifier();
		log = LogFilter.filter(null, 100, log, null,
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						return projActivities.contains(classifier.getClassIdentity(event));
					}
				}, 
				new XTraceCondition() {
					
					@Override
					public boolean keepTrace(XTrace trace) {
						return trace.size() > 0;
					}
				});
		
		return log;
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		CVariantLog<E> log = super.getVariantLog();
		// Try if there are category codes available (would be faster)
		// Fallback try projection using labels.
		BitSet activityCodes = getActivityCodes();
		if(activityCodes != null) {
			log = log.project(activityCodes);
		}
		else {
			log = log.project(getActivityLabels());
		}
		return log;
	}

}
