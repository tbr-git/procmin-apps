package de.rwth.processmining.tb.core.data.variantlog.contextaware;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.data.variantlog.base.CCCVariantImpl;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantDescriptionConstants;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantKeys;
import de.rwth.processmining.tb.core.data.variantlog.util.CVariantUtil;

public class CVariantCatContSetImpl extends CCCVariantImpl implements CVariantCatContSet {

	/**
	 * Categorical context set.
	 * 
	 * Entry c is set iff category c is contained in the context set.
	 */
	private final BitSet catContext;
	
	private boolean addRemovedToCSet = false;
	
	private AsCFVariant asCFVariant = null;
	
	public CVariantCatContSetImpl(int[] variant, BitSet catContext, int support) {
		super(variant, support);
		this.catContext = catContext;
	}
	
	public CVariantCatContSetImpl(CVariantCatContSetImpl variant) {
		super(variant.variant.clone(), variant.support);
		this.catContext = (BitSet) variant.getCatContext().clone();
	}

	@Override
	public CVariantCatContSetImpl projectOnCategories(BitSet projectionCategories) {
		int[] projIndices = CVariantUtil.getMatchingEventIndices(this.variant, projectionCategories);
		if(projIndices[0] > 0) {
			// Already allocate space for the variant count
			int[] vProjected = new int[projIndices[0]];
			for(int i = 0; i < projIndices[0]; i++) {
				vProjected[i] = variant[projIndices[i + 1]];
			}
			return new CVariantCatContSetImpl(vProjected, (BitSet) this.catContext.clone(), 
					this.support);
		}
		else {
			return null;
		}
	}

	@Override
	public CVariantCatContSetImpl extractSubtrace(int from, int to, boolean inplace) {
		from = Math.max(0, from);
		to = Math.min(this.variant.length, to);

		int[] subVariant = new int[to - from];
		
		for (int i = 0, j = from; j < to; i++, j++) {
			subVariant[i] = this.variant[j];
		}
	
		// Transform in place or create a fresh variant
		if (inplace) {
			this.variant = subVariant;
			// Added removed to context? 
			if (this.removedToCSetEnabled()) {
			  for (int i = 0; i < from; i++) {
					this.catContext.set(this.variant[i]);
			  }
				for (int i = to; i < this.variant.length; i++) {
					this.catContext.set(this.variant[i]);
				}
			}
			return this;
		}
		else {
			// Copy the context
			BitSet catContextCopy = (BitSet) catContext.clone();
			// Added removed to context? 
			if (this.removedToCSetEnabled()) {
			  for (int i = 0; i < from; i++) {
					catContextCopy.set(this.variant[i]);
			  }
				for (int i = to; i < this.variant.length; i++) {
					catContextCopy.set(this.variant[i]);
				}
			}
			return new CVariantCatContSetImpl(subVariant, catContextCopy, this.support);
		}
	}

	@Override
	public CVariantCatContSetImpl extractSubtrace(BitSet indices, boolean inplace) {

		int[] subVariant = indices.stream().map(i -> this.variant[i]).toArray();
		
		// Transform in place or create a fresh variant
		if (inplace) {
			this.variant = subVariant;
			// Added removed to context? 
			if (this.removedToCSetEnabled()) {
			  for (int i = 0; i < this.variant.length; i++) {
			    if (!indices.get(i) ) {
            this.catContext.set(this.variant[i]);
			    }
			  }
			}
			return this;
		}
		else {
			// Copy the context
			BitSet catContextCopy = (BitSet) catContext.clone();
			// Added removed to context? 
			if (this.removedToCSetEnabled()) {
			  for (int i = 0; i < this.variant.length; i++) {
			    if (!indices.get(i) ) {
            catContextCopy.set(this.variant[i]);
			    }
			  }
			}
			return new CVariantCatContSetImpl(subVariant, catContextCopy, this.support);
		}
	}

	@Override
	public CVariantCatContSetImpl copyVariant() {
		return new CVariantCatContSetImpl(this);
	}

	@Override
	public VariantKeys getVariantKey() {
		return new VariantKeys(VariantDescriptionConstants.ACTIVITY 
				| VariantDescriptionConstants.ADD_CONTEXT);
	}

	@Override
	public XTrace variant2XTrace(String traceName, Optional<CategoryMapper> categoryMapper) {
		// Convert to standard trace
		XTrace t = super.variant2XTrace(traceName, categoryMapper);
		
		////////////////////
		// Future
		// Add future as string of categories (sorted) to trace
		////////////////////
		final XFactory factory = XFactoryRegistry.instance().currentDefault();
		XAttribute attrFuture = factory.createAttributeLiteral("FutureSet", 
				this.catContext.stream()
					.mapToObj(Integer::toString)
					.collect(Collectors.joining(",")), 
					null);
		t.getAttributes().put("FutureSet", attrFuture);
		return t;
	}

	@Override
	public BitSet getCatContext() {
		return catContext;
	}
	
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = 31 * hash + catContext.hashCode();
		return hash;
	}
	
	/**
	 * First, compares the classical underlying categorical variant.
	 * Second, if baseline variants are equal, check for context set equality.
	 */
	@Override
	public boolean equals(Object obj) {
		boolean eq = super.equals(obj);
		
		// Check if contexts are equal
		if (eq) {
			eq = catContext.equals(((CVariantCatContSet) obj).getCatContext());
		}
		
		return eq;
		
	}

	@Override
	public void setRemovedToCSet(boolean enable) {
		this.addRemovedToCSet = true;
	}

	@Override
	public boolean removedToCSetEnabled() {
		return this.addRemovedToCSet;
	}
	
	@Override
	public AsCFVariant behaveAsCFVariant() {
		if (this.asCFVariant == null) {
			this.asCFVariant =  new AsCFVariant();
		}
		return this.asCFVariant;
	}
	

	public class AsCFVariant implements CVariant {

		@Override
		public Iterator<Integer> iteratorVariantCategorical() {
			return CVariantCatContSetImpl.this.iteratorVariantCategorical();
		}

		@Override
		public int getSupport() {
			return CVariantCatContSetImpl.this.getSupport();
		}

		@Override
		public void setSupport(int support) {
			throw new RuntimeException("Changing values of the conditional implementation "
					+ "while it pretends to be a normal variant is not supported!");
		}

		@Override
		public int getVariantLength() {
			return CVariantCatContSetImpl.this.getVariantLength();
		}

		@Override
		public CVariant projectOnCategories(BitSet projectionCategories) {
			throw new RuntimeException("Changing values of the conditional implementation "
					+ "while it pretends to be a normal variant is not supported!");
		}

		@Override
		public boolean containsAllCategories(int[] categories) {
			return CVariantCatContSetImpl.this.containsAllCategories(categories);
		}

    @Override
    public boolean containsAllCategories(BitSet categories) {
      return CVariantUtil.isContained(categories, variant);
    }

		@Override
		public boolean containsCategory(int category) {
			return CVariantCatContSetImpl.this.containsCategory(category);
		}

		@Override
		public boolean containsAnyCategory(int[] categories) {
			return CVariantCatContSetImpl.this.containsAnyCategory(categories);
		}

		@Override
		public CVariant extractSubtrace(int from, int to, boolean inplace) {
			throw new RuntimeException("Changing values of the conditional implementation "
					+ "while it pretends to be a normal variant is not supported!");
		}

		@Override
		public CVariant extractSubtrace(BitSet indices, boolean inplace) {
			throw new RuntimeException("Changing values of the conditional implementation "
					+ "while it pretends to be a normal variant is not supported!");
		}

		@Override
		public CVariant copyVariant() {
			throw new RuntimeException("Copying values of the conditional implementation "
					+ "while it pretends to be a normal variant is not supported!");
		}

		@Override
		public VariantKeys getVariantKey() {
			return CVariantCatContSetImpl.this.getVariantKey();
		}

		@Override
		public int[] getTraceCategories() {
			return CVariantCatContSetImpl.this.getTraceCategories();
		}

		@Override
		public XTrace variant2XTrace(String traceName, Optional<CategoryMapper> categoryMapper) {
			throw new RuntimeException("Copying values of the conditional implementation "
					+ "while it pretends to be a normal variant is not supported!");
		}

		@Override
		public int hashCode() {
			// Don't hash the categorical context
			// Only consider the underlying base variant
			return CVariantCatContSetImpl.super.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj == null || ! (obj instanceof CVariant)) { 
				// Explicitly don't compare class|| getClass() != obj.getClass())
				return false;
			}
			CVariant variantComp = (CVariant) obj;
			if(variant.length != variantComp.getVariantLength()) {
				return false;
			}
			else {
				for(int i = 0; i < variant.length; i++) {
					if(variant[i] != variantComp.getTraceCategories()[i]) {
						return false;
					}
				}
				return true;
			}
		}
		
	}
}
