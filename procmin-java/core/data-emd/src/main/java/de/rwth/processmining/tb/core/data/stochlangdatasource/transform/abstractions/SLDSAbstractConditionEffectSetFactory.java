package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.abstractions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.abstraction.CVariantAbst;
import de.rwth.processmining.tb.core.data.variantlog.transform.AddAbstConditionEffectSet;

public class SLDSAbstractConditionEffectSetFactory<E extends CVariantAbst> extends SLDSAbstractTransformerFactory<E> { 

	private final static Logger logger = LogManager.getLogger( SLDSAbstractConditionEffectSetFactory.class );

	/**
	 * Categorical codes of the activities that must occur; and iff all occur,
	 * a new abstraction will be added (either to {@link #effectCategories} or itself if {@link #effectCategories} is null).
	 * @see AddAbstConditionEffectSet
	 */
	private int[] conditionCategories;
	
	/**
	 * Effect categories that will receive another abstraction if the condition ({@link #conditionCategories} is fulfilled.
	 */
	private int[] effectCategories;
	
	/**
	 * Names of the activities that must occur; and iff all occur,
	 * a new abstraction will be added (either to {@link #effectActivityNames} or itself if {@link #effectActivityNames} is null).
	 * @see AddAbstConditionEffectSet
	 */
	private String[] conditionActivityNames;
	
	/**
	 * Effect activity names that will receive another abstraction if the condition ({@link #conditionActivityNames} is fulfilled.
	 */
	private String[] effectActivityNames;
	
	/**
	 * Abstraction code that may, eventually, be added.
	 * @see AddAbstConditionEffectSet
	 */
	private int abstraction2Add;

	public SLDSAbstractConditionEffectSetFactory() {
		super();
		conditionCategories = null;
		effectCategories = null;
		conditionActivityNames = null;
		effectActivityNames = null;
		abstraction2Add = -1;
	}
	
	/**
	 * Set a single activity category.
	 * @param activity
	 * @return
	 */
	public SLDSAbstractConditionEffectSetFactory<E> setConditionSet(int activity) {
		this.conditionActivityNames = null;
		conditionCategories = new int[] {activity};
		return this;
	}
	
	/**
	 * Set the activity categories.
	 * @param activities
	 * @return
	 */
	public SLDSAbstractConditionEffectSetFactory<E> setConditionSet(int[] activities) {
		this.conditionActivityNames = null;
		this.conditionCategories = activities;
		return this;
	}

	/**
	 * Set the mandatory activities by activity names.
	 * @param activities
	 * @return
	 */
	public SLDSAbstractConditionEffectSetFactory<E> setConditionSet(String[] activities) {
		this.conditionCategories = null;
		this.conditionActivityNames = activities;
		return this;
	}

	/**
	 * Set the effect activities by category codes.
	 * @param activities
	 * @return
	 */
	public SLDSAbstractConditionEffectSetFactory<E> setEffectSet(int[] activities) {
		this.effectActivityNames = null;
		this.effectCategories = activities;
		return this;
	}

	/**
	 * Set the effect activities by activity names.
	 * @param activities
	 * @return
	 */
	public SLDSAbstractConditionEffectSetFactory<E> setEffectSet(String[] activities) {
		this.effectCategories = null;
		this.effectActivityNames = activities;
		return this;
	}
	
	/**
	 * Set the abstraction code to be added iff all mandatory activities are present.
	 * @param abstraction Abstraction code to be added
	 * @return
	 */
	public SLDSAbstractConditionEffectSetFactory<E> setAbstractionCode(int abstraction) {
		this.abstraction2Add = abstraction;
		return this;
	}


	@Override
	public StochasticLanguageDataSource<E> build() throws SLDSTransformerBuildingException {
		if(conditionCategories == null && conditionActivityNames == null) {
			logger.error("Missing mandatory parameters.");
			throw new SLDSTransformerBuildingException("Could not instantiate the abstraction adding transformation!\n"
					+ "No category codes given!");
		}
		if(conditionCategories != null && conditionActivityNames != null) {
			logger.error("Overspecified mandatory parameters.");
			throw new SLDSTransformerBuildingException("Could not instantiate the abstraction adding transformation!\n"
					+ "Multiple specifications for the mandatory activities given!");
		}
		if(abstraction2Add == -1) {
			logger.warn("The abstraction code to be added seems to be uninitialized!");
		}

		if(conditionCategories != null) {
			if (effectCategories != null) {
				return new SLDSAbstractConditionEffectSetCategories<E>(this.parentDataSource, this.conditionCategories, 
						this.effectCategories, this.abstraction2Add);
			}
			else {
				return new SLDSAbstractConditionEffectSetCategories<E>(this.parentDataSource, this.conditionCategories, 
						this.abstraction2Add);
			}
		}
		else {
			if (effectActivityNames != null) {
				return new SLDSAbstractConditionEffectSetActivities<E>(this.parentDataSource, this.conditionActivityNames, 
						effectActivityNames, this.abstraction2Add);
			}
			else {
				return new SLDSAbstractConditionEffectSetActivities<E>(this.parentDataSource, this.conditionActivityNames, 
						this.abstraction2Add);
			}
		}
	}
}
