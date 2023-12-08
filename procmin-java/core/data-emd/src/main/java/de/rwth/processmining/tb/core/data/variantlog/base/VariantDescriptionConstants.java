package de.rwth.processmining.tb.core.data.variantlog.base;

public class VariantDescriptionConstants {

	/**
	 * Variant contains activity information
	 */
	public static final int ACTIVITY = 1; 

	/**
	 * Variant contains activity information
	 */
	public static final int ABSTRACTIONS = 2;

	/**
	 * Variant contains continuous time information. 
	 */
	public static final int TIME_CONT = (1 << 2);
	
	/**
	 * Variant contains binned time information. 
	 */
	public static final int TIME_BINNED = (1 << 3);
	
	/**
	 * Variant contains sojourn time information. 
	 */
	public static final int TIME_SOJ = (1 << 4);
	
	/**
	 * Variant contains service time information. 
	 */
	public static final int TIME_SERV = (1 << 5);
	
	/**
	 * Variant is, in addition to the usual sequence of activities, also defined by 
	 * more context information.
	 */
	public static final int ADD_CONTEXT = (1 << 6);

	/**
	 * Variant contains timestamps as miscellaneous (i.e., non-variant defining) information. 
	 */
	public static final int MISC_TS = (1 << 7);

}
