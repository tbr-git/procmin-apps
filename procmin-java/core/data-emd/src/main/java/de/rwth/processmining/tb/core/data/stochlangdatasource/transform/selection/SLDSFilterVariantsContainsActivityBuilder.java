package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public class SLDSFilterVariantsContainsActivityBuilder<E extends CVariant> extends SLDSAbstractTransformerFactory<E> {
	
	private final static Logger logger = LogManager.getLogger( SLDSFilterVariantsContainsActivityBuilder.class );
	
	/**
	 * Used event classifier.
	 */
	private XEventClassifier classifier;

	/**
	 * Activity that a trace should / should not contain
	 */
	private String activity;
	
	/**
	 * Keep the traces that contain activity (true) or drop them (false).
	 * Defaults to true
	 */
	private boolean keep;
	
	public SLDSFilterVariantsContainsActivityBuilder() {
		super();
		classifier = null;
		activity = null;
		keep = true;
	}

	/**
	 * Set the {@link this#activity}.
	 * @param activity Activity that a trace must / must not contain.
	 * @return Factory
	 */
	public SLDSFilterVariantsContainsActivityBuilder<E> setActivity(String activity) {
		this.activity = activity;
		return this;
	}

	/**
	 * Set the {@link this#classifier}.
	 * @param classifier Event classifier to be used.
	 * @return Factory
	 */
	public SLDSFilterVariantsContainsActivityBuilder<E> setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
		return this;
	}

	/**
	 * Set the {@link this#keep}.
	 * @param keep Keep traces (true); or drop them (false)
	 * @return Factory
	 */
	public SLDSFilterVariantsContainsActivityBuilder<E> keepTraces(boolean keep) {
		this.keep = keep;
		return this;
	}


	@Override
	public StochasticLanguageDataSource<E> build() throws SLDSTransformerBuildingException {
		//TODO Implement argument check for super type
		if(activity == null || classifier == null || this.parentDataSource == null) {
			logger.error("Missing mandatory parameters.");
			throw new SLDSTransformerBuildingException("Could not instantiate the variant contains activity filter "
					+ "due to misssing arguments");
		}
		return new SLDSFilterVariantsContainsActivity<>(this.parentDataSource, this.activity, 
				this.classifier, this.keep);
	}
	
}
