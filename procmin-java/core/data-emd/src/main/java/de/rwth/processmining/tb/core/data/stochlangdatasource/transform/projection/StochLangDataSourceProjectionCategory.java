package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.projection;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;

public class StochLangDataSourceProjectionCategory<E extends CVariant> extends StochLangDataSourceProjection<E> {

	/**
	 * Activity codes to project on.
	 */
	private final BitSet activities;
	
	/**
	 * Mapping from category codes to activity labels.
	 */
	private final CategoryMapper categoryMapper;
	

	public StochLangDataSourceProjectionCategory(StochasticLanguageDataSource<E> stochLangDataSource, 
			BitSet activities, CategoryMapper categoryMapper) {
		super(stochLangDataSource);
		this.activities = activities;
		this.categoryMapper = categoryMapper;
	}


	@Override
	protected Collection<String> getActivityLabels() {
		Set<String> activityLabels = new HashSet<>();
		this.activities.stream().sequential().forEach(c -> activityLabels.add(this.categoryMapper.getActivity4Category(c)));
		return activityLabels;
	}


	@Override
	protected BitSet getActivityCodes() {
		return activities;
	}
}
