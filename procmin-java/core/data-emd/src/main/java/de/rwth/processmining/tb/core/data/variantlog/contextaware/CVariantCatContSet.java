package de.rwth.processmining.tb.core.data.variantlog.contextaware;

import java.util.BitSet;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public interface CVariantCatContSet extends CVariant {

	/**
	 * Return a bitset where entry c is true iff c is a category from the context.
	 * @return
	 */
	public BitSet getCatContext();
	
	/**
	 * Enable disable that category of activities removed during projection 
	 * (e.g., {@link CVariant#extractSubtrace(int, int, boolean)}, {@link CVariant#projectOnCategories(BitSet)})
	 * should be added to the condition set of the returned variant.
	 * @param enable
	 */
	public void setRemovedToCSet(boolean enable);

	/**
	 * Should categories of activities removed during projection 
	 * (e.g., {@link CVariant#extractSubtrace(int, int, boolean)}, {@link CVariant#projectOnCategories(BitSet)})
	 * be added to the condition set of the returned variant?
	 * 
	 * @return True iff removed categories should be added to resulting variant's 
	 * 	condition set.
	 */
	public boolean removedToCSetEnabled();
	
	/**
	 * Return a view on this variant that behaves like a control flow-only variant (e.g., hashing and equality checks).
	 * It is recommended that any operation that changes the variant (e.g., projection or copies it) raises
	 * an exception. Implementation of it would be possible but fairly dangerous and awkward.
	 * 
	 * @return A view on this variant that behaves as if it would only know the control flow (e.g., hashing and equality checks)
	 */
	 // The contract on copying would, for example, be violated or awkward.
	 // If we return the copy be an additional behaveAsCFVariant, then the contained data would always remain
	 //  hidden. If we don't call behaveAsCFVariant, then behavior would change by copying.
	public CVariant behaveAsCFVariant(); 
}
