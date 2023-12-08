package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection;

import java.util.BitSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;

public class SLDSFilterMandatoryCategoryFactory<E extends CVariant> extends SLDSAbstractTransformerFactory<E> {
	private final static Logger logger = LogManager.getLogger( SLDSFilterMandatoryCategoryFactory.class );

	/**
	 * Mandatory activities (category codes).
	 */
	private BitSet activitiesCodes;

	/**
	 * Used event classifier.
	 */
	private XEventClassifier classifier;
	
	/**
	 * Category mapper required when using 
	 */
	private CategoryMapper categoryMapper;
	
	public SLDSFilterMandatoryCategoryFactory() {
		super();
		this.activitiesCodes = null;
		this.classifier = null;
		this.categoryMapper = null;
	}

	@Override
	public StochasticLanguageDataSource<E> build() throws SLDSTransformerBuildingException {
		if(this.activitiesCodes == null || classifier == null || this.parentDataSource == null) {
			logger.error("Missing mandatory parameters.");
			throw new SLDSTransformerBuildingException("Could not instantiate the filter on mandatory "
					+ "activitiesNames transformation due to misssing arguments");
		}
		
		int[] arrActivityCodes = this.activitiesCodes.stream().toArray();
		return new StochLangDataSourceFilterMandatoryCategory<E>(this.parentDataSource, arrActivityCodes, this.classifier, this.categoryMapper);
	}

	/**
	 * Set the <b>activity codes</b> that are <b>mandatory</b>.
	 * 
	 * Traces that do not contain all mandatory activitiesNames are dropped.
	 * 
	 * @param activitiesNames Mandatory activitiesNames. 
	 * @return
	 */
	public SLDSFilterMandatoryCategoryFactory<E> setActivities(BitSet activityCodes) {
		this.activitiesCodes = activityCodes;
		return this;
	}

	/**
	 * Set the {@link XEvent} classifier.
	 * @param classifier
	 * @return
	 */
	public SLDSFilterMandatoryCategoryFactory<E> setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
		return this;
	}

	/**
	 * Set the category mapper.
	 * @param categoryMapper
	 * @return
	 */
	public SLDSFilterMandatoryCategoryFactory<E> setCategoryMapper(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
		return this;
	}
	

}
