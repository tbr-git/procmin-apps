package de.rwth.processmining.tb.core.data.variantlog.base;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Optional;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.data.variantlog.util.CVariantUtil;

public class CCCVariantImpl implements CVariantAct {
	
	/**
	 * Variant as category code array.
	 */
	protected int[] variant;
	
	/**
	 * Total support of this variant (i.e., number of cases that 
	 * exhibit this variant)
	 */
	protected int support; 

	public CCCVariantImpl(int[] variant, int support) {
		this.variant = variant;
		this.support = support;
	}
	
	/**
	 * Copy constructor.
	 * @param eccVar
	 */
	public CCCVariantImpl(CCCVariantImpl eccVar) {
		int[] v = eccVar.getVariant();
		this.variant = Arrays.copyOf(v, v.length);
		this.support = eccVar.getSupport();
	}

	@Override
	public Iterator<Integer> iteratorVariantCategorical() {
		return new Iterator<Integer>() {
			
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < variant.length;
			}

			@Override
			public Integer next() {
				int res = variant[i];
				i++;
				return res;
			}
		};
	}

	@Override
	public int getVariantLength() {
		return variant.length;
	}

	@Override
	public CCCVariantImpl projectOnCategories(BitSet projectionCategories) {
//		int[] projIndices = getMatchingEventIndices(projectionCategories);
		int[] projIndices = CVariantUtil.getMatchingEventIndices(this.variant, projectionCategories);
		if(projIndices[0] > 0) {
			// Already allocate space for the variant count
			int[] vProjected = new int[projIndices[0]];
			for(int i = 0; i < projIndices[0]; i++) {
				vProjected[i] = variant[projIndices[i + 1]];
			}
			return new CCCVariantImpl(vProjected, this.support);
		}
		else {
			// TODO empty variant
			return null;
		}
	}
	
	@Override
	public CCCVariantImpl extractSubtrace(int from, int to, boolean inplace) {
		from = Math.max(0, from);
		to = Math.min(this.variant.length, to);

		int[] subVariant = new int[to - from];
		
		for (int i = 0, j = from; j < to; i++, j++) {
			subVariant[i] = this.variant[j];
		}
	
		if (inplace) {
			this.variant = subVariant;
			return this;
		}
		else {
			return new CCCVariantImpl(subVariant, this.support);
		}
	}

	@Override
	public CCCVariantImpl extractSubtrace(BitSet indices, boolean inplace) {
	  // Extract categories at index positions
		int[] subVariant = indices.stream().map(i -> this.variant[i]).toArray();
		
		if (inplace) {
			this.variant = subVariant;
			return this;
		}
		else {
			return new CCCVariantImpl(subVariant, this.support);
		}
	}

	@Override
	public boolean containsAllCategories(int[] categories) {
		return CVariantUtil.isContained(categories, variant);
	}

	@Override
	public boolean containsAllCategories(BitSet categories) {
		return CVariantUtil.isContained(categories, variant);
	}

	@Override
	public int getSupport() {
		return support;
	}

	@Override
	public void setSupport(int support) {
		this.support = support;
	}

	public int[] getVariant() {
		return variant;
	}

	@Override
	public CCCVariantImpl copyVariant() {
		return new CCCVariantImpl(this);
	}

	/**
	 * Hashing the variant.
	 * Hash code considers trace variant <b>not</b> count!
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.variant);
	}
	
	/**
	 * Is this variant equal to the given variant.
	 * Equality considers trace equality <b>not</b> equal number count!
	 */
	@Override
	public boolean equals(Object obj) {
	    if(this == obj) {
	    	return true;
	    }
	    if(obj == null || getClass() != obj.getClass()) {
	    	return false;
	    }
	    CCCVariantImpl variantComp = (CCCVariantImpl) obj;
	    if(variant.length != variantComp.variant.length) {
	    	return false;
	    }
	    else {
	      return Arrays.equals(variant, variantComp.variant);
	    }
	}

	@Override
	public VariantKeys getVariantKey() {
		return new VariantKeys(VariantDescriptionConstants.ACTIVITY);
	}

	@Override
	public int[] getTraceCategories() {
		return this.variant;
	}

	@Override
	public boolean containsCategory(int category) {
		return CVariantUtil.containsCategory(category, variant);
	}

	@Override
	public boolean containsAnyCategory(int[] categories) {
		return CVariantUtil.isAnyContained(categories, variant);
	}

	@Override
	public XTrace variant2XTrace(String traceName, Optional<CategoryMapper> categoryMapper) {
		final XFactory factory = XFactoryRegistry.instance().currentDefault();
		final XConceptExtension conceptInstance = XConceptExtension.instance();
		////////////////////////////////////////
		// New Trace
		////////////////////////////////////////
		XTrace trace = factory.createTrace();
		// Assign name
		if (traceName != null) {
			conceptInstance.assignName(trace, traceName);
		}
		// Add frequency attribute
		XAttribute attrFreq = factory.createAttributeDiscrete("Frequency", this.support, null);
		trace.getAttributes().put("Frequency", attrFreq);
		////////////////////////////////////////
		// Add Events
		////////////////////////////////////////
		XEvent xEvent;
		Iterator<Integer> itCat = this.iteratorVariantCategorical();
		while(itCat.hasNext()) {
			int c = itCat.next();
			
			xEvent = factory.createEvent();
			// Translate if category mapper specified
			conceptInstance.assignName(xEvent, categoryMapper.isPresent() ? 
							categoryMapper.get().getActivity4Category(c) : Integer.toString(c));
			
			// Add to trace
			trace.add(xEvent);
		}
	
		return trace;
	}

}
