package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public class SLDSFilterVariantAnyActivityBuilder<E extends CVariant> extends SLDSAbstractTransformerFactory<E> { 
	private final static Logger logger = LogManager.getLogger( SLDSFilterVariantAnyActivityBuilder.class );
	
	/**
	 * Used event classifier.
	 */
	private XEventClassifier classifier;

	/**
	 * Activities that a trace should / should not contain (at least one)
	 */
	private ArrayList<String> activities;
	
	/**
	 * Keep the traces that contain any activity (true) or drop them (false).
	 * Defaults to true
	 */
	private boolean keep;
	
	public SLDSFilterVariantAnyActivityBuilder() {
		super();
		classifier = null;
		activities = new ArrayList<>();
		keep = true;
	}

	/**
	 * Add to {@link this#activities}.
	 * @param activity Activity that a trace must / must not contain.
	 * @return Factory
	 */
	public SLDSFilterVariantAnyActivityBuilder<E> addActivity(String activity) {
		this.activities.add(activity);
		return this;
	}

	/**
	 * Add to {@link this#activities}.
	 * @param activity Activity that a trace must / must not contain.
	 * @return Factory
	 */
	public SLDSFilterVariantAnyActivityBuilder<E> addActivities(String... activities) {
		for(String activity : activities) {
			this.activities.add(activity);
		}
		return this;
	}

	/**
	 * Set the {@link this#classifier}.
	 * @param classifier Event classifier to be used.
	 * @return Factory
	 */
	public SLDSFilterVariantAnyActivityBuilder<E> setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
		return this;
	}

	/**
	 * Set the {@link this#keep}.
	 * @param keep Keep traces (true); or drop them (false)
	 * @return Factory
	 */
	public SLDSFilterVariantAnyActivityBuilder<E> keepTraces(boolean keep) {
		this.keep = keep;
		return this;
	}


	@Override
	public StochasticLanguageDataSource<E> build() throws SLDSTransformerBuildingException {
		if(classifier == null || this.parentDataSource == null) {
			logger.error("Missing mandatory parameters.");
			throw new SLDSTransformerBuildingException("Could not instantiate the variant contains activity filter "
					+ "due to misssing arguments");
		}
		if(activities.size() == 0) {
			logger.warn("Setting up a trace/variant contains any activity filter with empty activity set");
		}
		String[] activityArray = new String[activities.size()];
		activities.toArray(activityArray);
		return new SLDSFilterVariantAnyActivity<>(this.parentDataSource, activityArray, 
				this.classifier, this.keep);
	}

}
