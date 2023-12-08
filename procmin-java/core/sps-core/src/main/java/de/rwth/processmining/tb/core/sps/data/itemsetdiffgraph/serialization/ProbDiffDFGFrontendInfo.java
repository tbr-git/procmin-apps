package de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.serialization;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFG;

public class ProbDiffDFGFrontendInfo {
	
	private final String dotString;
	
	@JsonSerialize(using = ProbDiffDFGAttributeInfoSerializer.class)
	private final ProbDiffDFG diffDFG;

	public ProbDiffDFGFrontendInfo(ProbDiffDFG diffDFG) {
		super();
		this.diffDFG = diffDFG;
		this.dotString = diffDFG.getDotString();
	}

	public ProbDiffDFG getDiffDFG() {
		return diffDFG;
	}

	public String getDotString() {
		return dotString;
	}
}
