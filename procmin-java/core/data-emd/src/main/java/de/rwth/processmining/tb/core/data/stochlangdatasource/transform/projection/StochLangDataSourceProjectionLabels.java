package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.projection;

import java.util.Collection;

import org.deckfour.xes.classification.XEventClassifier;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public class StochLangDataSourceProjectionLabels<E extends CVariant> extends StochLangDataSourceProjection<E> {

	/**
	 * Activity labels to project on.
	 */
	private final Collection<String> activities;
	
	public StochLangDataSourceProjectionLabels(StochasticLanguageDataSource<E> stochLangDataSource, Collection<String> activities, XEventClassifier classifier) {
		this(stochLangDataSource, activities);
	}

	public StochLangDataSourceProjectionLabels(StochasticLanguageDataSource<E> stochLangDataSource, Collection<String> activities) {
		super(stochLangDataSource);
		this.activities = activities;
	}

	@Override
	protected Collection<String> getActivityLabels() {
		return activities;
	}

}
