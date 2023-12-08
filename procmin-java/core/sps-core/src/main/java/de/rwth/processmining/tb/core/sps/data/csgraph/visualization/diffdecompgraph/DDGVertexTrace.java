package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;

/**
 * Vertex in the difference decomposition graph that represents a trace variant 
 * in the EMD solution.
 * @author brockhoff
 *
 */
public final class DDGVertexTrace extends DDGVertex {
	
	/**
	 * Probability mass assigned to this trace variant 
	 */
	private final double probability;
	
	/**
	 * Convenience information indicating to which log is belongs (left, right). 
	 */
	private final LogSide logSide;
	
	/**
	 * Activity names assigned to this trace
	 */
	private final ImmutableList<DDGActivity> activities;
	
	/**
	 * Showing the EMD reallocation, gives a hint how to order each side 
	 * to minimize crossings
	 */
	private int intraEMDSideOrdering;
	
	/**
	 * Can be used to rank traces that have a common reference point. 
	 * For example, all traces on one side of EMD. 
	 */
	private Optional<Integer> probabilityRank;
	
	public DDGVertexTrace(int id, double probability, LogSide logSide, List<DDGActivity> activities,
			CSGraphVertex csGraphVertex) {

		super(id, "ddg-trace-" + csGraphVertex.getHfddVertexRef().getId() + "-" + logSide + "-" + id, 
				csGraphVertex);
		this.probability = probability;
		this.logSide = logSide;
		this.activities = ImmutableList.copyOf(activities);
		this.intraEMDSideOrdering = 0;
		this.probabilityRank = Optional.empty();
	}

	////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters
	////////////////////////////////////////////////////////////////////////////////

	public double getProbability() {
		return probability;
	}
	
	public LogSide getLogSide() {
		return logSide;
	}
	
	public ImmutableList<DDGActivity> getActivities() {
		return this.activities;
	}

	public int getIntraEMDSideOrdering() {
		return intraEMDSideOrdering;
	}

	public void setIntraEMDSideOrdering(int intraEMDSideOrdering) {
		this.intraEMDSideOrdering = intraEMDSideOrdering;
	}

	@Override
	public DDGVertexType getVertexType() {
		return DDGVertexType.EMD;
	}

	public Optional<Integer> getProbabilityRank() {
		return probabilityRank;
	}

	public void setProbabilityRank(int probabilityRank) {
		this.probabilityRank = Optional.of(probabilityRank);
	}

}
