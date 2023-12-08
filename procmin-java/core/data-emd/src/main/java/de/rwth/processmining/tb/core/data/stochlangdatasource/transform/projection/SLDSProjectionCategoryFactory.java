package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.projection;

import java.util.BitSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;

/**
 * Factory that adds an projection decorator on top of a {@link StochasticLanguageDataSource}.
 * 
 * Example usage:
 * <pre>{@code
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
public class SLDSProjectionCategoryFactory <E extends CVariant> extends SLDSAbstractTransformerFactory<E>  {

	private final static Logger logger = LogManager.getLogger( SLDSProjectionCategoryFactory.class );

	/**
	 * Mandatory activity category codes.
	 */
	private BitSet activities;
	
	//TODO Remove not needed
	/**
	 * Used event classifier.
	 */
	private XEventClassifier classifier;
	
	/**
	 * Mapper used to translate categories into activity labels
	 */
	private CategoryMapper categoryMapper;
	
	public SLDSProjectionCategoryFactory() {
		super();
		activities = null;
		classifier = null;
		this.categoryMapper = null;
	}
	
	/**
	 * Set the <b>activities</b> to be <b>projected on</b>.
	 * 
	 * Resulting empty variants will be removed. 
	 * 
	 * @param activities Activities to be projected on. 
	 * @return
	 */
	public SLDSProjectionCategoryFactory<E> setActivities(BitSet activities) {
		this.activities = activities;
		return this;
	}

	/**
	 * Set the category mapper.
	 * 
	 * 
	 * @param categoryMapper Category Mapper
	 * @return
	 */
	public SLDSProjectionCategoryFactory<E> setCategoryMapper(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
		return this;
	}

	/**
	 * Set the classifier.
	 * @param classifier Classifier
	 * @return
	 */
	public SLDSProjectionCategoryFactory<E> setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
		return this;
	}

	@Override
	public StochasticLanguageDataSource<E> build() {
		//TODO check classifier 
		if(activities == null || classifier == null || this.parentDataSource == null || this.categoryMapper == null) {
			logger.error("Missing mandatory parameters.");
		}
		return new StochLangDataSourceProjectionCategory<E>(this.parentDataSource, this.activities, this.categoryMapper);
	}

}
