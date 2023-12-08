package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

/**
 * Factory for the {@link StochLangDataSourceFilterMandatory} filter.
 * @author brockhoff
 *
 * @param <E>
 */
public class SLDSFilterMandatoryFactory<E extends CVariant> extends SLDSAbstractTransformerFactory<E> {
	private final static Logger logger = LogManager.getLogger( SLDSFilterMandatoryFactory.class );

	/**
	 * Mandatory activities (names).
	 */
	private Set<String> activitiesNames;

	/**
	 * Used event classifier.
	 */
	private XEventClassifier classifier;
	
	public SLDSFilterMandatoryFactory() {
		super();
		activitiesNames = null;
		classifier = null;
	}
	
	/**
	 * Set the <b>activitiesNames</b> that are <b>mandatory</b>.
	 * 
	 * Traces that do not contain all mandatory activitiesNames are dropped.
	 * 
	 * @param activitiesNames Mandatory activitiesNames. 
	 * @return
	 */
	public SLDSFilterMandatoryFactory<E> setActivities(Set<String> activities) {
		this.activitiesNames = activities;
		return this;
		
	}

	/**
	 * Set the <b>activitiesNames</b> that are <b>mandatory</b>.
	 * 
	 * Traces that do not contain all mandatory activitiesNames are dropped.
	 * 
	 * @param activitiesNames Mandatory activitiesNames. 
	 * @return
	 */
	public SLDSFilterMandatoryFactory<E> setActivities(String... activities) {
		this.activitiesNames = new HashSet<>();
		for(String s: activities) {
			this.activitiesNames.add(s);
		}
		return this;
	}

	/**
	 * Set the <b>activitiesNames</b> that are <b>mandatory</b>.
	 * 
	 * Traces that do not contain all mandatory activitiesNames are dropped.
	 * 
	 * @param activitiesNames Mandatory activitiesNames. 
	 * @return
	 */
	public SLDSFilterMandatoryFactory<E> setActivities(Collection<String> activities) {
		this.activitiesNames = new HashSet<>();
		for(String s: activities) {
			this.activitiesNames.add(s);
		}
		return this;
	}

	/**
	 * Set the {@link XEvent} classifier.
	 * @param classifier
	 * @return
	 */
	public SLDSFilterMandatoryFactory<E> setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
		return this;
	}
	
	@Override
	public StochasticLanguageDataSource<E> build() throws SLDSTransformerBuildingException {
		if(this.activitiesNames == null || classifier == null || this.parentDataSource == null) {
			logger.error("Missing mandatory parameters.");
			throw new SLDSTransformerBuildingException("Could not instantiate the filter on mandatory "
					+ "activitiesNames transformation due to misssing arguments");
		}
		return new StochLangDataSourceFilterMandatory<E>(this.parentDataSource, this.activitiesNames, this.classifier);
	}

}
