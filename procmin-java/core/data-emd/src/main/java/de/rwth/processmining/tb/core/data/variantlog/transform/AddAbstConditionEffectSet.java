package de.rwth.processmining.tb.core.data.variantlog.transform;

import java.util.BitSet;
import java.util.Iterator;

import de.rwth.processmining.tb.core.data.variantlog.abstraction.CVariantAbst;

/**
 * Adds abstractions to a trace considering a condition category set (these categories must occur) and an effected
 * categories set (i.e., these categories will obtain a additional abstraction if the condition is met).
 * @author brockhoff
 *
 * @param <T> Type of the variant this transformer is applied on.
 */
public class AddAbstConditionEffectSet<T extends CVariantAbst> implements CVariantTransformer<T> {


	/**
	 * Categorical codes of the activities that must occur; and iff all occur,
	 * a new abstraction will be added to the effect set.
	 * 
	 * Set <b>must</b> not be empty.
	 */
	private int[] conditionCategorySet;

	/**
	 * Category set that will be affected if the abstraction is applied.
	 */
	private int[] effectCategorySet;
	
	/**
	 * Abstraction code that may, eventually, be added.
	 */
	private int abstraction2Add;
	
	/**
	 * Maximal categorical code used among activities that must occur. 
	 * Clearing an unused index in a {@link java.util.BitSet} does nothing. (See code of Java's Bitset implementation)
	 * Used to instantiate the bitsets.
	 */
	private int maxCatCode;
	
	
	/**
	 * Constructor that uses the same activities for the condition and effect set.
	 * @param activityCategories Activity categories for the condition and effect set
	 * @param abstraction2Add Abstraction id to be added
	 */
	public AddAbstConditionEffectSet(int[] activityCategories, int abstraction2Add) {
		// Use given categories for condition and effect set
		this(activityCategories, activityCategories, abstraction2Add);
	}

	/**
	 * Constructor that uses the same activities for the condition and effect set. 
	 * Should be used if the maximum category code is already known.
	 * @param activityCategories Activity categories for the condition and effect set
	 * @param abstraction2Add Abstraction id to be added
	 * @param maxCatCode Maximum category code
	 */
	public AddAbstConditionEffectSet(int[] activityCategories, int abstraction2Add, int maxCatCode) {
		// Use given categories for condition and effect set
		this(activityCategories, activityCategories, abstraction2Add, maxCatCode);
	}
	
	/**
	 * Constructor if condition and effect category set differ.
	 * @param conditionCategories Condition categories (i.e., these categories must occur)
	 * @param effectCategories Effected categories (i.e., these categories will obtain the abstraction iff condition is met)
	 * @param abstraction2Add Abstraction id that will be added
	 */
	public AddAbstConditionEffectSet(int[] conditionCategories, int[] effectCategories, int abstraction2Add) {
		super();
		this.conditionCategorySet = conditionCategories;
		this.effectCategorySet = effectCategories;
		this.abstraction2Add = abstraction2Add;
		int max = -1;
		for(int catCode : conditionCategories) {
			max = Math.max(max, catCode);
		}
		for(int catCode : effectCategories) {
			max = Math.max(max, catCode);
		}
		// Does not make sense to have empty activity Categories (or negative once).
		assert max > -1;
		this.maxCatCode = max;
	}

	/**
	 * Constructor if condition and effect category set differ and max category code is known.
	 * @param conditionCategories Condition categories (i.e., these categories must occur)
	 * @param effectCategories Effected categories (i.e., these categories will obtain the abstraction iff condition is met)
	 * @param abstraction2Add Abstraction id that will be added
	 * @param maxCatCode Maximum category code
	 */
	public AddAbstConditionEffectSet(int[] conditionCategories, int[] effectCategories, int abstraction2Add, int maxCatCode) {
		super();
		this.conditionCategorySet = conditionCategories;
		this.effectCategorySet = effectCategories;
		this.abstraction2Add = abstraction2Add;
		this.maxCatCode = maxCatCode;
	}

	@Override
	public boolean requiresDuplicateDetection() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T apply(T variant, boolean inplace) {
		// Declare and init datastructures here so that this function can be parallelized
		// Mandatory activities -> These may eventually receive the abstraction update
		BitSet conditionBitSet = new BitSet(maxCatCode);
		// Buffer containing so far unmatched mandatory activities
		BitSet effectBitSet = new BitSet(maxCatCode);
		// Index buffer containing indices of abstraction candidates
		int[] indexBuffer = new int[variant.getVariantLength()];
		int countMatches = 0;
		// Fill buffers
		for(int catCode : conditionCategorySet) {
			conditionBitSet.set(catCode);
		}
		for(int catCode : effectCategorySet) {
			effectBitSet.set(catCode);
		}
		
		// Check if all mandatory activities occur
		// and store their index (in case of loops they may occur multiple times)
		Iterator<Integer> itVariantCatCodes = variant.iteratorVariantCategorical();
		int catCode;
		int i = 0;
		while(itVariantCatCodes.hasNext()) {
			catCode = itVariantCatCodes.next();
			// Remove mandatory from open list
			conditionBitSet.clear(catCode);
			// Bookkeeping of all matches
			if(effectBitSet.get(catCode)) {
				indexBuffer[countMatches] = i;
				countMatches++;
			}
			i++;
		}
		
		// If all mandatory occur
		if(conditionBitSet.isEmpty()) {
			if(!inplace) {
				variant = (T) variant.copyVariant();
			}
			for(i = 0; i < countMatches; i++) {
				variant.addAbstractionAt(indexBuffer[i], abstraction2Add);
			}
		}
		return variant;
	}

}
