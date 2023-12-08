package de.rwth.processmining.tb.core.data.variantlog.miscinfo;

import java.util.Arrays;
import java.util.BitSet;

import de.rwth.processmining.tb.core.data.variantlog.base.CCCVariantImpl;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantDescriptionConstants;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantKeys;
import de.rwth.processmining.tb.core.data.variantlog.util.CVariantUtil;

public class CVariantMiscTS 
    extends CCCVariantImpl
    implements CVariantMiscInfo {

	/**
	 * Timestamps
	 * (variant x nbr "events")
	 */
  protected long[][] timestamps;
  
  public CVariantMiscTS(int[] variant, int support, long[][] timestamps) {
    super(variant, support);
    this.timestamps = timestamps;
  }
  
  public CVariantMiscTS(CVariantMiscTS variant) {
    super(
        Arrays.copyOf(variant.getTraceCategories(), variant.getVariantLength()), 
        variant.getSupport());
		this.timestamps = Arrays.stream(timestamps).map(long[]::clone).toArray(long[][]::new);
  }

  @Override
  public CVariantMiscTS projectOnCategories(BitSet projectionCategories) {
		int[] projIndices = CVariantUtil.getMatchingEventIndices(this.variant, projectionCategories);
		if(projIndices[0] > 0) {
		  // TODO Real copy?
			// Already allocate space for the variant count
			int[] vProjected = new int[projIndices[0]];
			long[][] timestampsProjected = new long[projIndices[0]][];
			for(int i = 0; i < projIndices[0]; i++) {
				vProjected[i] = variant[projIndices[i + 1]];
        timestampsProjected[i] = timestamps[projIndices[i + 1]];
			}
			return new CVariantMiscTS(vProjected, this.support, timestampsProjected);
		}
		else {
			// TODO empty variant
			return null;
		}
  }

  @Override
  public CVariantMiscTS extractSubtrace(int from, int to, boolean inplace) {
		from = Math.max(0, from);
		to = Math.min(this.variant.length, from);

    BitSet indices = new BitSet(this.variant.length);
    for (int i = from; i < to; i++) {
      indices.set(i);
    }
    return extractSubtrace(indices, inplace);
  }
  
	@Override
	public CVariantMiscTS extractSubtrace(BitSet indices, boolean inplace) {
	  // Extract categories at index positions
		int[] subVariant = indices.stream().map(i -> this.variant[i]).toArray();
		long[][] subTimestamps = indices.stream()
		    .mapToObj(i -> Arrays.copyOf(this.timestamps[i], this.timestamps[i].length))
		    .toArray(long[][]::new);
		
		if (inplace) {
			this.variant = subVariant;
			this.timestamps = subTimestamps;
			return this;
		}
		else {
			return new CVariantMiscTS(subVariant, this.support, subTimestamps);
		}
	}


  ////////////////////////////////////////////////////////////
  // Comparison
  ////////////////////////////////////////////////////////////
	/**
	 * Hashing the variant.
	 * Hash code considers trace variant <b>neither</b> counts <b>nor</b> miscellaneous information!
	 */
	@Override
	public int hashCode() {
    //////////////////////////////
    // !!!!! Only CF !!!!!
    //////////////////////////////
	  return super.hashCode();
	}
	
	/**
	 * Is this variant equal to the given variant.
	 * Equality considers trace equality <b>not</b> equal number count!
	 */
	@Override
	public boolean equals(Object obj) {
	  return super.equals(obj);
	}

  ////////////////////////////////////////////////////////////
  // General Util
  ////////////////////////////////////////////////////////////
  @Override
  public CVariantMiscTS copyVariant() {
    return new CVariantMiscTS(this);
  }

  ////////////////////////////////////////////////////////////
  // Getter and Setter
  ////////////////////////////////////////////////////////////
  @Override
  public VariantKeys getVariantKey() {
		return new VariantKeys(VariantDescriptionConstants.ACTIVITY, VariantDescriptionConstants.MISC_TS);
  }

  public long[][] getTimestamps() {
    return this.timestamps;
  }

  @Override
  public String getMiscInfoDescription() {
    return "timestamps";
  }
}
