package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.projection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

/**
 * Factory that adds an projection decorator on top of a {@link StochasticLanguageDataSource}.
 * 
 * Example usage:
 * <pre>{@code
 * SLDSProjectionLabelFactory<? extends CVariant<?>> factory = new SLDSProjectionLabelFactory();
 * // Configure basic setup
 * factory.setActivities(activities).setClassifier(classifier);
 * // Set data source and build
 * factory.setParentDataSource(dataSource).build();
 * 
 * }</pre>
 * @author brockhoff
 *
 * @param <E>
 */
public class SLDSProjectionLabelFactory <E extends CVariant> extends SLDSAbstractTransformerFactory<E> {

	private final static Logger logger = LogManager.getLogger( SLDSProjectionLabelFactory.class );

	/**
	 * Mandatory activities.
	 */
	private Set<String> activities;
	
	/**
	 * Used event classifier.
	 */
	private XEventClassifier classifier;
	
	public SLDSProjectionLabelFactory() {
		super();
		activities = null;
		classifier = null;
	}
	
	/**
	 * Set the <b>activities</b> to be <b>projected on</b>.
	 * 
	 * Resulting empty variants will be removed. 
	 * 
	 * @param activities Activities to be projected on. 
	 * @return
	 */
	public SLDSProjectionLabelFactory<E> setActivities(Set<String> activities) {
		this.activities = activities;
		return this;
	}

	/**
	 * Set the <b>activities</b> to be <b>projected on</b>.
	 * 
	 * Resulting empty variants will be removed. 
	 * 
	 * @param activities Activities to be projected on. 
	 * @return
	 */
	public SLDSProjectionLabelFactory<E> setActivities(String... activities) {
		this.activities = new HashSet<>();
		for(String s: activities) {
			this.activities.add(s);
		}
		return this;
	}

	/**
	 * Set the <b>activities</b> to be <b>projected on</b>.
	 * 
	 * Resulting empty variants will be removed. 
	 * 
	 * @param activities Activities to be projected on. 
	 * @return
	 */
	public SLDSProjectionLabelFactory<E> setActivities(Collection<String> activities) {
		this.activities = new HashSet<>();
		for(String s: activities) {
			this.activities.add(s);
		}
		return this;
	}
	
	
	/**
	 * Set the <b>single activity</b> to be <b>projected on</b>.
	 * 
	 * Resulting empty variants will be removed. 
	 * 
	 * @param activity
	 * @return
	 */
	public SLDSProjectionLabelFactory<E> setActivity(String activity) {
		this.activities = new HashSet<String>();
		this.activities.add(activity);
		return this;
	}
	
	public SLDSProjectionLabelFactory<E> setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
		return this;
	}

	@Override
	public StochasticLanguageDataSource<E> build() {
		//TODO check classifier 
		if(activities == null || classifier == null || this.parentDataSource == null) {
			logger.error("Missing mandatory parameters.");
		}
		return new StochLangDataSourceProjectionLabels<E>(this.parentDataSource, this.activities);
	}

}
