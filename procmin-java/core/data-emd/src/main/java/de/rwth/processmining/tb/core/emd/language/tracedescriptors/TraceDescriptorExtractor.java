package de.rwth.processmining.tb.core.emd.language.tracedescriptors;

import gnu.trove.strategy.HashingStrategy;

public interface TraceDescriptorExtractor<F extends TraceDescriptor> {

  /**
   * Get a hashing strategy for the descriptor.
   * (to store is in a gnu trove hashmap)
   * 
   * @return Hashing strategy for a gnu trove hashmap
   */
	public default HashingStrategy<F> getHashingStrat() {
	  return new HashingStrategy<F>() {

      /**
       * 
       */
      private static final long serialVersionUID = -7649198883700103357L;

      @Override
      public int computeHashCode(F object) {
        return object.hashCode();
      }

      @Override
      public boolean equals(F o1, F o2) {
        return o1.equals(o2);
      }
    };
	}
	
	
	/**
	 * Does is matter if we project before we extract 
	 * or extract and then project?
	 * @return
	 */
	// TODO Rename to cummutive
	public boolean isProjectionInvariant();
	
	/**
	 * Get a short description of the trace descriptor this extractor extracts.
	 * @return
	 */
	public String getShortDescription();
	
}
