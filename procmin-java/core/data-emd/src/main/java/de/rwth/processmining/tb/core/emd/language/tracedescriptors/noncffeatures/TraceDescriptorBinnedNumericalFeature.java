package de.rwth.processmining.tb.core.emd.language.tracedescriptors.noncffeatures;

import java.util.Arrays;
import java.util.Optional;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class TraceDescriptorBinnedNumericalFeature extends TraceDescriptor {
	
	/**
	 * Bin indices.
	 */
	private final int[] binIndices;
	
	/**
	 * Feature extractors that were used to extract this descriptor.
	 */
	private final Optional<BinnedContinuousFeatureExtractor[]> refFeaturedExtractors;
	
	
	/**
	 * Constructor
	 * @param binIndices Bin indices
	 * @param refFeaturedExtractor Optional references to 
	 * 		the featured extractors used to construct this descriptor
	 */
	public TraceDescriptorBinnedNumericalFeature(int[] binIndices, 
			Optional<BinnedContinuousFeatureExtractor[]> refFeaturedExtractors) {
		super();
		this.binIndices = binIndices;
		this.refFeaturedExtractors = refFeaturedExtractors;
	}


	@Override
	public int hashCode() {
		return Arrays.hashCode(binIndices);
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		else if (this == obj) {
			return true;
		}
		else if (obj instanceof TraceDescriptorBinnedNumericalFeature objCast) {
			return Arrays.equals(this.binIndices, objCast.binIndices);
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		if (this.refFeaturedExtractors.isPresent()) {
			assert this.refFeaturedExtractors.get().length == this.binIndices.length;

			StringBuilder builder = new StringBuilder();
			// Multi-variate
			if (this.binIndices.length > 1) {
				builder.append("{");
			}
			int i = 0;
			for (BinnedContinuousFeatureExtractor extractor : this.refFeaturedExtractors.get()) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append(extractor.getBinDescription(this.binIndices[i]));
				i++;
			}
			// Multi-variate
			if (this.binIndices.length > 1) {
				builder.append("}");
			}
			return builder.toString();
		}
		else {
			return Arrays.toString(this.binIndices);
		}
	}

	@Override
	public String toString(int index) {
		return this.toString();
	}


	@Override
	public int length() {
		return 1;
	}

	public int[] getBins() {
		return this.binIndices;
	}

}
