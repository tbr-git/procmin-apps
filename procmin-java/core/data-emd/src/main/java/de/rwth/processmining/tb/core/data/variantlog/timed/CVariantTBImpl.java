package de.rwth.processmining.tb.core.data.variantlog.timed;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantDescriptionConstants;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantKeys;
import de.rwth.processmining.tb.core.data.variantlog.util.CVariantUtil;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.TimeBinType;

public class CVariantTBImpl implements CVariantTimeBinned {
	private final static Logger logger = LogManager.getLogger( CVariantTBImpl.class );

	/**
	 * Variant as category code array.
	 */
	private int[] variant;

	/**
	 * Array of time bins for each entry in variant array.
	 */
	private int[] timeBins;
	
	/**
	 * Total support of this variant (i.e., number of cases that 
	 * exhibit this variant)
	 */
	private int support;
	
	/**
	 * Variant key information
	 */
	private final VariantKeys variantKey;
	
	/**
	 * Time Bin Type (e.g., service time, sojourn time)
	 */
	private final TimeBinType timeBinType;

	public CVariantTBImpl(int[] variant, int[] timeBins, int support, TimeBinType binType) {
		assert timeBins.length == variant.length;

		this.variant = variant;
		this.support = support;
		this.timeBins = timeBins;
		this.timeBinType = binType; 
		int compositeKey = VariantDescriptionConstants.ACTIVITY | VariantDescriptionConstants.TIME_BINNED;
		switch(binType) {
		case DURATION:
			compositeKey |= VariantDescriptionConstants.TIME_SERV;
			break;
		case SOJOURN:
			compositeKey |= VariantDescriptionConstants.TIME_SOJ;
			break;
		default:
			logger.warn("Unknown time bin type. Skipping the more detailed description.");
			break;
		}
		this.variantKey = new VariantKeys(compositeKey);
	}

	/**
	 * Copy constructor
	 * @param eccVar
	 */
	public CVariantTBImpl(CVariantTBImpl variant) {
		this.variant = Arrays.copyOf(variant.variant, variant.variant.length);
		this.support = variant.support;
		this.timeBins = Arrays.copyOf(variant.timeBins, variant.timeBins.length);
		this.variantKey = new VariantKeys(variant.variantKey.compositeKey);
		this.timeBinType = variant.timeBinType;
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
	public Iterator<Pair<Integer, Integer>> iteratorCategoryTime() {
		return new Iterator<Pair<Integer, Integer>>() {
			
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < variant.length;
			}

			@Override
			public Pair<Integer, Integer> next() {
				Pair<Integer, Integer> res = Pair.of(variant[i], timeBins[i]);
				i++;
				return res;
			}
		};
	}

	@Override
	public int getSupport() {
		return this.support;
	}

	@Override
	public void setSupport(int support) {
		this.support = support;
	}

	@Override
	public int getVariantLength() {
		return variant.length;
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
	public boolean containsCategory(int category) {
		return CVariantUtil.containsCategory(category, variant);
	}

	@Override
	public boolean containsAnyCategory(int[] categories) {
		return CVariantUtil.isAnyContained(categories, variant);
	}

	@Override
	public VariantKeys getVariantKey() {
		return this.variantKey;
	}

	@Override
	public int[] getTraceCategories() {
		return this.variant;
	}

	@Override
	public int getTimeBinAt(int index) {
		return this.timeBins[index];
	}

	@Override
	public CVariantTimeBinned projectOnCategories(BitSet projectionCategories) {
		int[] projIndices = CVariantUtil.getMatchingEventIndices(this.variant, projectionCategories);
		if(projIndices[0] > 0) {
			// Already allocate space for the variant count
			int[] vProjected = new int[projIndices[0]];
			int[] timeBinsProjected = new int[projIndices[0]];
			for(int i = 0; i < projIndices[0]; i++) {
				vProjected[i] = this.variant[projIndices[i + 1]];
				timeBinsProjected[i] = this.timeBins[projIndices[i + 1]];
			}
			return new CVariantTBImpl(vProjected, timeBinsProjected, this.support, this.timeBinType);
		}
		else {
			return null;
		}
	}

	@Override
	public CVariantTBImpl extractSubtrace(int from, int to, boolean inplace) {
		from = Math.max(0, from);
		to = Math.min(this.variant.length, from);

		int[] subVariant = new int[to - from];
		int[] subTimeBins = new int[to - from];
		
		for (int i = 0, j = from; j < to; i++, j++) {
			subVariant[i] = this.variant[j];
			subTimeBins[i] = this.timeBins[j];
		}
		
		if (inplace) {
			this.variant = subVariant;
			this.timeBins = subTimeBins;
			return this;
		}
		else {
			return new CVariantTBImpl(subVariant, subTimeBins, this.support, this.timeBinType);
		}
	}

	@Override
	public CVariantTBImpl extractSubtrace(BitSet indices, boolean inplace) {
	  // Extract categories at index positions
		int[] subVariant = indices.stream().map(i -> this.variant[i]).toArray();
		int[] subTimeBins = indices.stream()
		    .map(i -> this.timeBins[i])
		    .toArray();
		
		if (inplace) {
			this.variant = subVariant;
			this.timeBins = subTimeBins;
			return this;
		}
		else {
			return new CVariantTBImpl(subVariant, subTimeBins, this.support, this.timeBinType);
		}
	}

	@Override
	public CVariantTimeBinned copyVariant() {
		return new CVariantTBImpl(this);
	}

	/**
	 * Hash code is composed of categories, time bins, and time bin type.
	 * <b>Support is not considered</b>
	 */
	@Override
	public int hashCode() {
		int hash = Arrays.hashCode(this.variant);
		hash = 31 * hash + Arrays.hashCode(this.timeBins);
		hash = 31 * hash + this.timeBinType.hashCode();
		return hash;
	}

	/**
	 * Equals considers categories, time bins, and time bin type.
	 * <b>Support is not considered</b>
	 */
	@Override
	public boolean equals(Object obj) {
	    if(this == obj) { // Same object
	    	return true;
	    }
	    if(obj == null || getClass() != obj.getClass()) { // Same Class
	    	return false;
	    }
	    CVariantTBImpl variantComp = (CVariantTBImpl) obj;
	    // Compare variant lengths 
	    if(variant.length != variantComp.variant.length) {
	    	return false;
	    }
	    else {
	    	// Equal bin type
	    	if(this.timeBinType != variantComp.timeBinType) {
	    		return false;
	    	}
	    	// Equal variant codes
	    	for(int i = 0; i < variant.length; i++) {
	    		if(variant[i] != variantComp.variant[i]) {
	    			return false;
	    		}
	    	}
	    	// Equal time bins
	    	for(int i = 0; i < timeBins.length; i++) {
	    		if(timeBins[i] != variantComp.timeBins[i]) {
	    			return false;
	    		}
	    	}
	    }
	    return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("CTimeVariant[ ");
		for(int i = 0; i < variant.length; i++) {
			builder.append("(");
			builder.append(variant[i]);
			builder.append(", ");
			builder.append(this.timeBins[i]);
			builder.append(") ");
		}
		builder.append("]");

		return builder.toString();
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
		Iterator<Pair<Integer, Integer>> itCatTime = this.iteratorCategoryTime();
		while(itCatTime.hasNext()) {
			Pair<Integer, Integer> pCatTime = itCatTime.next();
			
			xEvent = factory.createEvent();
			// Translate if category mapper specified
			conceptInstance.assignName(xEvent, categoryMapper.isPresent() ? 
							categoryMapper.get().getActivity4Category(pCatTime.getLeft()) : 
								Integer.toString(pCatTime.getLeft()));
			XAttribute attrTimeBin = factory.createAttributeDiscrete("TimeBin", pCatTime.getRight(), null);
			xEvent.getAttributes().put("TimeBin", attrTimeBin);
			
			// Add to trace
			trace.add(xEvent);
		}
	
		return trace;
	}

}
