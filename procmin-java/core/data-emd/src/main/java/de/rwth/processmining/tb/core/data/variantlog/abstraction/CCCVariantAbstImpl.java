package de.rwth.processmining.tb.core.data.variantlog.abstraction;

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

import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantDescriptionConstants;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantKeys;
import de.rwth.processmining.tb.core.data.variantlog.util.CVariantUtil;


public class CCCVariantAbstImpl implements CVariantAbst {
	// Very similar to CCVariantImpl; however, extending this class is problematic as
	// we cannot have CVariant<CCCVariantImpl> and CVariant<CCCVariantAbstImpl>
	// Decision is that the extension of interface should be proper so that the 
	// exposed interface can be used easily (i.e., abstract variants extends standard variants)
	// Moreover, we want logs as generic containers and, therefore, require that the projection
	// yields a contained type consistent log -> We require the CVariant<...> construct.
	
	/**
	 * Variant as category code array.
	 */
	private int[] variant;

	/**
	 * Abstractions. Array of abstractions for each entry in variant array.
	 * abstractions[i][0] contains the number of abstractions.
	 * abstractions[i][1..] contains the abstractions in ascending order 
	 */
	private int[][] abstractions;
	
	/**
	 * Total support of this variant (i.e., number of cases that 
	 * exhibit this variant)
	 */
	private int support;

	public CCCVariantAbstImpl(int[] variant, int support) {
		this.variant = variant;
		this.support = support;
		this.abstractions = new int[variant.length][];
		for(int i = 0; i < abstractions.length; i++) {
			this.abstractions[i] = new int[10];
		}
	}

	public CCCVariantAbstImpl(int[] variant, int support, int[][] abstractions) {
		this.variant = variant;
		this.support = support;
		this.abstractions = abstractions;
	}

	public CCCVariantAbstImpl(CCCVariantAbstImpl eccVar) {
		this.variant = Arrays.copyOf(eccVar.variant, eccVar.variant.length);
		this.support = eccVar.support;
		this.abstractions = new int[eccVar.abstractions.length][];
		for(int i = 0; i < eccVar.abstractions.length; i++) {
			this.abstractions[i] = Arrays.copyOf(eccVar.abstractions[i], eccVar.abstractions[i].length);
		}
	}

	@Override
	public int[] getAbstractionsAt(int activityIndex) {
		return abstractions[activityIndex];
	}

	@Override
	public void addAbstractionAt(int activityIndex, int abstraction) {
		int[] currentAbstraction = this.abstractions[activityIndex];
		// Init if currently empty
		if(this.abstractions[activityIndex] == null) {
			this.abstractions[activityIndex] = new int[10];
		}
		// Initialization will put 0 at array index 0
		int nbrAbstractions = currentAbstraction[0];
		// Increase abstraction buffer if necessary
		if(nbrAbstractions == currentAbstraction.length - 1) {
			int[] incAbstractions = Arrays.copyOf(currentAbstraction, currentAbstraction.length + 10);
			this.abstractions[activityIndex] = incAbstractions;
			currentAbstraction	= incAbstractions;
		}

		// Find position of insertion
		int i = 1;
		while(i <= nbrAbstractions && currentAbstraction[i] < abstraction) {
			i++;
		}
		
		// Shift abstractions from last until position of insertion
		for(int j = nbrAbstractions; j >= i; j--) {
			currentAbstraction[j + 1] = currentAbstraction[j];
		}
		// Insert new abstraction
		currentAbstraction[i] = abstraction;
		// Increment abstraction counter
		currentAbstraction[0] = currentAbstraction[0] + 1;
	}

	@Override
	public Iterator<CVariantAbstActivityData> iteratorActAbst() {
		return new Iterator<CVariantAbstActivityData>() {
			
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < variant.length;
			}

			@Override
			public CVariantAbstActivityData next() {
				CVariantAbstActivityData res = new CVariantAbstActivityData(variant[i], abstractions[i]);
				i++;
				return res;
			}
		};
	}

	/**
	 * Composes variant hash code with abstraction array hash code.
	 */
	@Override
	public int hashCode() {
		int hash = Arrays.hashCode(this.variant);
		for(int[] a: abstractions) {
			if(a != null) {
				hash = 31 * hash + Arrays.hashCode(a);
			}
		}
		return hash;
	}

	/**
	 * Are two variants with abstraction equal.
	 * Requires variant equality (<b>not</b> respecting counts) and
	 * abstraction equality for each event:
	 * <ul>
	 * <li> Equal number of abstractions</li>
	 * <li> Equal abstraction classes</li>
	 * </ul>
	 */
	@Override
	public boolean equals(Object obj) {
	    if(this == obj) { // Same object
	    	return true;
	    }
	    if(obj == null || getClass() != obj.getClass()) { // Same Class
	    	return false;
	    }
	    CCCVariantAbstImpl variantComp = (CCCVariantAbstImpl) obj;
	    // Compare variant categorical codes
	    if(variant.length != variantComp.variant.length) {
	    	return false;
	    }
	    else {
	    	for(int i = 0; i < variant.length; i++) {
	    		if(variant[i] != variantComp.variant[i]) {
	    			return false;
	    		}
	    	}
	    }
		// Compare abstractions for each event
		// Unequal iff:
		// - Different number of abstractions
		// - One abstraction differs
		// Important: We implicitly assume a sorting of the abstractions
		CCCVariantAbstImpl other = (CCCVariantAbstImpl) obj;
		int[] aThis;
		int[] aComp;
		for(int i = 0; i < this.getVariantLength(); i++) {
			aThis = abstractions[i];
			aComp = other.abstractions[i];
			// Lengths match
			if(sizeAbstraction(aThis) != sizeAbstraction(aComp)) {
				return false;
			}
			else {
				// Check if SORTED abstractions match
				for(int j = abstractionsIndexStart(aThis); j < abstractionsIndexEnd(aThis); j++) {
					if(aThis[j] != aComp[j]) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private static final int sizeAbstraction(int[] abstraction) {
		return abstraction[0];
	}
	
	private static final int getAbstractionAt(int[] abstraction, int index) {
		return abstraction[index + 1];
	}
	
	private static final int abstractionsIndexStart(int[] abstraction) {
		return 1;
	}

	private static final int abstractionsIndexEnd(int[] abstraction) {
		return 1 + abstraction[0];
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
	public int getSupport() {
		return support;
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
	public CCCVariantAbstImpl projectOnCategories(BitSet projectionCategories) {
		int[] projIndices = CVariantUtil.getMatchingEventIndices(this.variant, projectionCategories);
		if(projIndices[0] > 0) {
			// Already allocate space for the variant count
			int[] vProjected = new int[projIndices[0]];
			int[][] abstProjected = new int[projIndices[0]][];
			int[] atmp;
			for(int i = 0; i < projIndices[0]; i++) {
				vProjected[i] = variant[projIndices[i + 1]];
				atmp = abstractions[projIndices[i+1]];
				abstProjected[i] = Arrays.copyOf(atmp, atmp.length);
			}
			return new CCCVariantAbstImpl(vProjected, this.support, abstProjected);
		}
		else {
			return null;
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
	public CCCVariantAbstImpl extractSubtrace(int from, int to, boolean inplace) {
		from = Math.max(0, from);
		to = Math.min(this.variant.length, from);

		int[] subVariant = new int[to - from];
		int[][] subAbstractions = new int[to - from][];
		
		for (int i = 0, j = from; j < to; i++, j++) {
			subVariant[i] = this.variant[j];
			subAbstractions[i] = Arrays.copyOf(this.abstractions[j], this.abstractions[j].length);
		}
		
		if (inplace) {
			this.variant = subVariant;
			this.abstractions = subAbstractions;
			return this;
		}
		else {
			return new CCCVariantAbstImpl(subVariant, this.support, subAbstractions);
		}
	}

	@Override
	public CCCVariantAbstImpl extractSubtrace(BitSet indices, boolean inplace) {
	  // Extract categories at index positions
		int[] subVariant = indices.stream().map(i -> this.variant[i]).toArray();
		int[][] subAbstractions = indices.stream()
		    .mapToObj(i -> Arrays.copyOf(this.abstractions[i], this.abstractions[i].length))
		    .toArray(int[][]::new);
		
		if (inplace) {
			this.variant = subVariant;
			this.abstractions = subAbstractions;
			return this;
		}
		else {
			return new CCCVariantAbstImpl(subVariant, this.support, subAbstractions);
		}
	}

	@Override
	public CCCVariantAbstImpl copyVariant() {
		return new CCCVariantAbstImpl(this);
	}

	@Override
	public int getNbrAbstractionsAt(int activityIndex) {
		return this.abstractions[activityIndex][0];
	}

	@Override
	public VariantKeys getVariantKey() {
		return new VariantKeys(VariantDescriptionConstants.ACTIVITY
				| VariantDescriptionConstants.ABSTRACTIONS);
	}

	@Override
	public int[] getTraceCategories() {
		return this.variant;
	}

	@Override
	public int[][] getAbstractions() {
		return this.abstractions;
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
		Iterator<CVariantAbstActivityData> itActInst = this.iteratorActAbst();
		while(itActInst.hasNext()) {
			CVariantAbstActivityData actInst = itActInst.next();
			
			xEvent = factory.createEvent();
			// Translate if category mapper specified
			conceptInstance.assignName(xEvent, categoryMapper.isPresent() ? 
							categoryMapper.get().getActivity4Category(actInst.getActivityCategory()) : 
								Integer.toString(actInst.getActivityCategory()));
			
			for (int i = 0; i < sizeAbstraction(actInst.getAbstractions()); i++) {
				XAttribute attrAbst = factory.createAttributeDiscrete("Abstraction" + i, 
						getAbstractionAt(actInst.getAbstractions(), i), null);
				xEvent.getAttributes().put("Abstraction" + i, attrAbst);
			}
			// Add to trace
			trace.add(xEvent);
		}
	
		return trace;
	}


}
