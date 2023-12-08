package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.projection;

import java.util.BitSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;

/**
 * Factory that adds a multi-activity-set contained projection decorator on top of a {@link StochasticLanguageDataSource}.
 * 
 * Example usage:
 * <pre>{@code
 * List<BitSet> activities = new LinkedList<>();
 * activities.add(...);
 * SLDSProjectionCategoryFactory<? extends CVariant<?>> factory = new SLDSProjectionCategoryFactory();
 * // Configure basic setup
 * // activities is assumed to be a BitSet of categories
 * factory.setActivities(activities).setClassifier(classifier);
 * // Set data source and build
 * factory.setParentDataSource(dataSource).build();
 * 
 * }</pre>
 * @author brockhoff
 *
 * @param <E>
 */
public class SLDSContainsProjectMultiCategoriesBuilder <E extends CVariant> extends SLDSAbstractTransformerFactory<E> {

	private final static Logger logger = LogManager.getLogger( SLDSContainsProjectMultiCategoriesBuilder.class );

	/**
	 * Activity itemsets on which, if they a fully contained, the trace is projected on.
	 */
	private List<BitSet> activities;
	
	/**
	 * Mapper used to translate categories into activity labels
	 */
	private CategoryMapper categoryMapper;
	
	/**
	 * Project on the complement.
	 */
	private boolean complementProjection;
	
	public SLDSContainsProjectMultiCategoriesBuilder() {
		super();
		this.activities = null;
		this.categoryMapper = null;
		this.complementProjection = false;
	}
	
	/**
	 * Set the <b>activity-sets</b> to be <b>projected on</b> if <b>a set if fully contained</b>.
	 * 
	 * Resulting empty variants will be removed. 
	 * 
	 * @param activities Activities to be projected on. 
	 * @return
	 */
	public SLDSContainsProjectMultiCategoriesBuilder<E> setActivities(List<BitSet> activities) {
		this.activities = activities;
		return this;
	}

	/**
	 * Set the category mapper.
	 * 
	 * @param categoryMapper Category Mapper
	 * @return
	 */
	public SLDSContainsProjectMultiCategoriesBuilder<E> setCategoryMapper(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
		return this;
	}

	/**
	 * Set complement projection flag.
	 * 
	 * @param complementProjection true / false
	 * @return
	 */
	public SLDSContainsProjectMultiCategoriesBuilder<E> setComplementProjection(boolean complementProjection) {
		this.complementProjection = complementProjection;
		return this;
	}

	@Override
	public StochasticLanguageDataSource<E> build() {
		//TODO check classifier 
		if(activities == null || this.parentDataSource == null || this.categoryMapper == null) {
			logger.error("Missing mandatory parameters.");
		}
		return new SLDSContainsProjectMultiCategories<E>(this.parentDataSource, 
		    this.activities, this.categoryMapper, complementProjection);
	}

}
