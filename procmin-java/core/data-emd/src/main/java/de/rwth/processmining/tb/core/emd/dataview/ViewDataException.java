package de.rwth.processmining.tb.core.emd.dataview;

public class ViewDataException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3284131689617827093L;
	
	public ViewDataException(RealizabilityInfo realInfo) {
		super(realInfo.getProblemType().toString() + ": " + realInfo.getInfo());
	}

}
