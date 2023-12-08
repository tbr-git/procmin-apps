package de.rwth.processmining.tb.core.emd.grounddistances.gdextensionbound;

import org.apache.commons.lang3.tuple.Triple;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;

public class LVSPrefixExtBounds extends LVSWExtBounds {

	/**
	 * Prefixes of this length are deemed to be extendible.
	 */
	private final int maxPrefixLength;

	public LVSPrefixExtBounds(int maxPrefixLength) {
		
		this.maxPrefixLength = maxPrefixLength;
	}

	@Override
	public double get_distance(BasicTraceCC t1, BasicTraceCC t2) {
		Triple<Double, Double, Double> distances = getRightExtBoundedDistance(t1, t2);
		
		// This is a complete trace
		if(t2.length() < maxPrefixLength) {
			return distances.getLeft();
		}
		else {
			switch(currentBoundType) {
				case NONE:
					return distances.getLeft();
				case LOWER: 
					return distances.getMiddle();
				case UPPER: 
					return distances.getRight();
				default:
					return 1;
			}
		}
	}

}
