package de.rwth.processmining.tb.core.emd.grounddistances;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

/**
 * Interface that specifies a ground distance for EMD.
 * @param <F> Type of Feature that this distance requires
 */
public interface TraceDescDistCalculator<F extends TraceDescriptor> {

	/**
	 * Calculate the distance between the two provided trace descriptions.
	 * 
	 * Usually int [0, 1].
	 * 0 means equality, 1 means completely different
	 * @param t1 Left descriptor
	 * @param t2 Right descriptor
	 * @return Distance usually [0, 1]
	 */
	public double get_distance(F t1, F t2);
	
	/**
	 * Get a short description string for this distance.
	 * @return Description string
	 */
	public String getShortDescription();
	
}
