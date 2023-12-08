package de.rwth.processmining.tb.core.emd.dataview;

public class RealizabilityInfo {
	
	private final boolean isRealizable;
	
	private final RealizationProblemType problemType;
	
	private final String info;
	
	public RealizabilityInfo(RealizationProblemType problemType, String info) {
		this.problemType = problemType;
		this.info = info;
		isRealizable = problemType == RealizationProblemType.NONE;
	}
	
	public boolean isRealizable() {
		return isRealizable;
	}

	public RealizationProblemType getProblemType() {
		return problemType;
	}

	public String getInfo() {
		return info;
	}

}
