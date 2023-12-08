package de.rwth.processmining.tb.core.sps.data.abstraction;

import java.util.List;

import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexConditionType;

public class VertexConditionLVSAbst {
	
	private List<ComparisonAbstraction> abstractions;

	private VertexConditionType vertCondType;

	private List<Integer> condVertex;

	private Double condMaxPropCoverLoss;

	public VertexConditionLVSAbst() {
		abstractions = null;
		vertCondType = VertexConditionType.PROBCOND;
		condVertex = null;
		condMaxPropCoverLoss = 0.0;
	}

	public List<ComparisonAbstraction> getAbstractions() {
		return abstractions;
	}

	public void setAbstractions(List<ComparisonAbstraction> abstractions) {
		this.abstractions = abstractions;
	}

	public VertexConditionType getVertCondType() {
		return vertCondType;
	}

	public void setVertCondType(VertexConditionType vertCondType) {
		this.vertCondType = vertCondType;
	}

	public List<Integer> getCondVertex() {
		return condVertex;
	}

	public void setCondVertex(List<Integer> condVertex) {
		this.condVertex = condVertex;
	}

	public Double getCondMaxPropCoverLoss() {
		return condMaxPropCoverLoss;
	}

	public void setCondMaxPropCoverLoss(Double condMaxPropCoverLoss) {
		this.condMaxPropCoverLoss = condMaxPropCoverLoss;
	}

}
