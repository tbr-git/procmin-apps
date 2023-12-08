package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CSSkEdge extends DefaultWeightedEdge {
//public class CSSkEdge {

	/**
	 * 
	 */
	@JsonIgnore
	private static final long serialVersionUID = -3217615307183253561L;
	
	/**
	 * Flow in terms of probability mass on this edge.
	 */
	private double flow;
	
	/**
	 * Flow in the matching-based visualization.
	 */
	private double matchingFlow;
	
	/**
	 * Cost associated to this edge.
	 */
	private double cost;
	
	/**
	 * Edge type of this edge.
	 */
	private final EdgeType edgeType;

	/**
	 * Is this edge relevant for the matching-based visualization?
	 * We may want to remove edges between empty traces (defaults to true).
	 */
	private boolean matchingRelevant;
	
	public CSSkEdge(double flow, double cost, EdgeType edgeType) {
		this.flow = flow;
		this.cost = cost;
		this.edgeType = edgeType;
		this.matchingRelevant = true;
		this.matchingFlow = flow;
	}
	
	//================================================================================
	// Getters and Setters
	//================================================================================

	public void setFlow(double flow) {
		this.flow = flow;
	}

	public double getFlow() {
		return flow;
	}

	public double getCost() {
		return cost;
	}

	public EdgeType getEdgeType() {
		return edgeType;
	}

	public boolean isMatchingRelevant() {
		return matchingRelevant;
	}

	public void setMatchingRelevant(boolean matchingRelevant) {
		this.matchingRelevant = matchingRelevant;
	}

	public double getMatchingFlow() {
		return matchingFlow;
	}

	public void setMatchingFlow(double matchingFlow) {
		this.matchingFlow = matchingFlow;
	}

}
