package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.abstractions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.StochasticLangDataSourceTransformer;
import de.rwth.processmining.tb.core.data.variantlog.abstraction.CVariantAbst;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.transform.AddAbstConditionEffectSet;

/**
 * Add Abstraction to a set of activities iff they occur in the trace.
 * Wraps around {@link AddAbstConditionEffectSet}.
 * 
 * @see AddAbstConditionEffectSet
 * 
 * @author brockhoff
 *
 * @param <E>
 */
public class SLDSAbstractConditionEffectSetCategories<E extends CVariantAbst> extends StochasticLangDataSourceTransformer<E> {
	
	private final static Logger logger = LogManager.getLogger( SLDSAbstractConditionEffectSetCategories.class );
	/**
	 * Categorical codes of the activities that must occur; and iff all occur,
	 * an addition new abstraction will be added to {@link #effectCategories}.
	 * @see AddAbstConditionEffectSet
	 */
	private final int[] conditionCategories;
	
	/**
	 * If all {@link #conditionCategories} occur, the abstraction will be added
	 * to these categories.
	 */
	private final int[] effectCategories;
	
	/**
	 * Abstraction code that may, eventually, be added.
	 * @see AddAbstConditionEffectSet
	 */
	private final int abstraction2Add;

	/**
	 * Maximal categorical code used among activities that must occur. 
	 * @see AddAbstConditionEffectSet
	 */
	private int maxCatCode;

	/**
	 * Constructor where {@link #effectCategories} will be equal to {@link #conditionCategories}.
	 * @param stochLangDataSource Data source
	 * @param activityCategories Category codes to used for condition and effect.
	 * @param abstraction2Add Abstraction code to be added
	 */
	public SLDSAbstractConditionEffectSetCategories(StochasticLanguageDataSource<E> stochLangDataSource, 
			int[] activityCategories, int abstraction2Add) {
		this(stochLangDataSource, activityCategories, null, abstraction2Add);
	}

	/**
	 * Constructor.
	 * @param stochLangDataSource Data source
	 * @param conditionCategories Category codes used for condition.
	 * @param effectCategories Category codes that will be effected by the abstraction.
	 * @param abstraction2Add Abstraction code to be added
	 */
	public SLDSAbstractConditionEffectSetCategories(StochasticLanguageDataSource<E> stochLangDataSource, 
			int[] conditionCategories, int[] effectCategories, int abstraction2Add) {
		super(stochLangDataSource);
	
		this.conditionCategories = conditionCategories;
		this.effectCategories = effectCategories;
		this.abstraction2Add = abstraction2Add;
		
		int max = -1;
		for(int catCode : conditionCategories) {
			max = Math.max(max, catCode);
		}
		// Consider effect categories as well
		if (effectCategories != null) {
			for(int catCode : effectCategories) {
				max = Math.max(max, catCode);
			}
		}
		// Does not make sense to have empty activity Categories (or negative once).
		assert max > -1;
		this.maxCatCode = max;
	}


	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		// TODO Implement the use of abstractions for the XLog as well
		return super.getDataRawTransformed();
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		CVariantLog<E> log = super.getVariantLog();
		AddAbstConditionEffectSet<E> applyAbstraction;
		if (this.effectCategories == null) {
			applyAbstraction = new AddAbstConditionEffectSet<>(this.conditionCategories, this.abstraction2Add, this.maxCatCode);
		}
		else {
			applyAbstraction = new AddAbstConditionEffectSet<>(this.conditionCategories, this.effectCategories, this.abstraction2Add, this.maxCatCode);
		}
		return log.applyVariantTransformer(applyAbstraction, false);
	}

}
