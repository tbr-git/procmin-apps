package de.rwth.processmining.tb.core.data.variantlog.transform;

import java.util.BitSet;
import java.util.List;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.util.CVariantUtil;

/**
 * Transform the variant based on multiple activity itemsets.
 * If an itemset is fully contained, also project on it.
 */
public class ExtractMultiCategoriesStrict<T extends CVariant> implements CVariantTransformer<T> {

	/**
	 * List of activity code sets. 
	 * If a set is fully contained, we project on it
	 */
	private final List<BitSet> activities;
	
	private final boolean complementProjection;
	
  public ExtractMultiCategoriesStrict(List<BitSet> activities, boolean complementProjection) {
    super();
    this.activities = activities;
    this.complementProjection = complementProjection;
  }

  @Override
  public boolean requiresDuplicateDetection() {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T apply(T variant, boolean inplace) {
    BitSet multiProjIndices = getProjectionIndices(variant, activities);
    
    if (complementProjection) {
      // The bitset's size -> length is usually less than the variant's length
      // (-> power of 2)
      multiProjIndices.flip(0, variant.getVariantLength());
    }
    // Apply projection
    return (T) variant.extractSubtrace(multiProjIndices, false);
  }
  
  /**
   * Get indices of activities that we extract/project on.
   * 
   * Given the list of activity sets, and index is contained IFF
   * 
   * There is an activity itemset such that every activity of this itemset is contained in the variant
   * AND this event's category belongs to this itemset.
   * 
   * @param <E>
   * @param variant Variant from which indices are extracted
   * @param activities List of itemsets to consider
   * @return Bitset of indices that should be kept by the projection
   */
  public static <E extends CVariant> BitSet getProjectionIndices(E variant, List<BitSet> activities) {
    // Projection Indices for each activity set
    // A projection set may only contribute "bits" if 
    // there is no activity that is not contained 
    BitSet multiProjIndices = new BitSet(variant.getVariantLength());
    
    ////////////////////
    // Gather indices
    ////////////////////
    for (BitSet activitySet : activities) {
      int[] projIndicesInfo = CVariantUtil.getUniqueAndMatchingEventIndices(
          variant.getTraceCategories(), activitySet);
      // !!! Each activity contained at least once !!!
      if (projIndicesInfo[0] == activitySet.cardinality()) {
        for (int i = 0; i < projIndicesInfo[1]; i++) {
          multiProjIndices.set(projIndicesInfo[i + 2]);
        }
      }
    }
    
    return multiProjIndices;
  }

}
