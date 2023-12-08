package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.abstractions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.StochasticLangDataSourceTransformer;
import de.rwth.processmining.tb.core.data.variantlog.abstraction.CVariantAbst;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
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
public class SLDSAbstractConditionEffectSetActivities<E extends CVariantAbst> extends StochasticLangDataSourceTransformer<E> {

	private final static Logger logger = LogManager.getLogger( SLDSAbstractConditionEffectSetActivities.class );
	/**
	 * Names of the activities that must occur; and iff all occur,
	 * an addition new abstraction will be added to {@link #effectActivities}.
	 * @see AddAbstConditionEffectSet
	 */
	private final String[] conditionActNames;
	
	/**
	 * If all {@link #conditionActNames} occur, the abstraction will be added
	 * to these activities.
	 */
	private final String[] effectActivities;
	
	/**
	 * Abstraction code that may, eventually, be added.
	 * @see AddAbstConditionEffectSet
	 */
	private final int abstraction2Add;

	/**
	 * Constructor where {@link #effectActivities} will be equal to {@link #conditionActNames}.
	 * @param stochLangDataSource Data source
	 * @param activityNames Activity names to used for condition and effect.
	 * @param abstraction2Add Abstraction code to be added
	 */
	public SLDSAbstractConditionEffectSetActivities(StochasticLanguageDataSource<E> stochLangDataSource, 
			String[] activityNames, int abstraction2Add) {
		this(stochLangDataSource, activityNames, null, abstraction2Add);
	}

	/**
	 * Constructor. 
	 * @param stochLangDataSource Data source
	 * @param conditionActivities Activity names to used for condition
	 * @param effectActivities Affected activity names
	 * @param abstraction2Add Abstraction code to be added
	 */
	public SLDSAbstractConditionEffectSetActivities(StochasticLanguageDataSource<E> stochLangDataSource, 
			String[] conditionActivities, String[] effectActivities, int abstraction2Add) {
		super(stochLangDataSource);
	
		this.conditionActNames = conditionActivities;
		this.effectActivities = effectActivities;
		this.abstraction2Add = abstraction2Add;
	}

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		// TODO Implement the use of abstractions for the XLog as well
		throw new RuntimeException("Transformation on XLog not implemented!");
//		return super.getDataRawTransformed();
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		CVariantLog<E> log = super.getVariantLog();
		CategoryMapper catMapper = log.getCategoryMapper();
		
		// Get the activity codes for the given mandatory activities
		int[] conditionCategories = new int[this.conditionActNames.length];
		int i = 0;
		int maxCatCode = -1;
		Integer catCode = null;
		for(String a: conditionActNames) {
			catCode = catMapper.getCategory4Activity(a);
			// If this activity does not occur in the mapping
			// return the log as it is impossible that this activity occurs in the variants at all 
			if(catCode == null) {
				return log;
			}
			else {
				conditionCategories[i] = catCode;
				maxCatCode = Math.max(maxCatCode, catCode);
				i++;
			}
		}

		int[] effectCategories = null;
		if (this.effectActivities != null) {
			effectCategories = new int[this.effectActivities.length];
			i = 0;
			catCode = null;
			for(String a: effectActivities) {
				catCode = catMapper.getCategory4Activity(a);
				// If this activity does not occur in the mapping
				// return the log as it is impossible that this activity occurs in the variants at all 
				if(catCode == null) {
					continue;
				}
				else {
					effectCategories[i] = catCode;
					maxCatCode = Math.max(maxCatCode, catCode);
					i++;
				}
			}
		}

		// Apply the log transformer
		AddAbstConditionEffectSet<E> applyAbstraction = null;
		if (effectCategories == null) {			// No effect categories
			// Use condition categories for condition AND effect
			applyAbstraction = new AddAbstConditionEffectSet<>(conditionCategories, this.abstraction2Add, maxCatCode);
		}
		else {
			applyAbstraction = new AddAbstConditionEffectSet<>(conditionCategories, effectCategories, this.abstraction2Add, maxCatCode);
		}
		return log.applyVariantTransformer(applyAbstraction, false);
	}

}
