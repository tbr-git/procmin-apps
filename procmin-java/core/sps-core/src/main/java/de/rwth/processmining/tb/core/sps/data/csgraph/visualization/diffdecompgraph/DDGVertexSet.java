package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;

/**
 * Vertex in the difference decomposition graph (tree) that represents an activity set.
 * 
 * @author brockhoff
 *
 */
public abstract sealed class DDGVertexSet extends DDGVertex permits DDGVertexSetCS, DDGVertexSetSupport   {
	
	/**
	 * Information on probability masses in the two log associated with this vertex.
	 * For example, considering non-empty traces, 
	 * conditioned and non-conditioned or residual probability mass.
	 */
	private final DDGVertexProbInfo probabilityInfo;

	/**
	 * Associated set of activities.
	 */
	private final List<DDGActivity> activities;
	
	/**
	 * Optional set of activities on which the vertex' measurement 
	 * was conditioned.
	 */
	private final Optional<List<DDGActivity>> conditionActivities;
	
	public DDGVertexSet(int id, DDGVertexProbInfo probabilityInfo, 
			Collection<DDGActivity> activities, Optional<Collection<DDGActivity>> conditionActivities, 
			CSGraphVertex csGraphVertex) {

		super(id, "ddg-set-" + csGraphVertex.getHfddVertexRef().getId(), csGraphVertex);
		this.probabilityInfo = probabilityInfo;

		// Sort activities by their names
		Comparator<DDGActivity> comp = Comparator.comparing((DDGActivity activity) -> {
			if (activity.getActivityAbbrev().isPresent()) {
				return activity.getActivityAbbrev().get();
			}
			else {
				return activity.getActivity();
			}
		});
		this.activities = activities.stream().sorted(comp).toList();
		this.conditionActivities = conditionActivities.map(c -> c.stream().sorted(comp).toList());
	}

	////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters
	////////////////////////////////////////////////////////////////////////////////
	
	public DDGVertexProbInfo getProbabilityInfo() {
		return probabilityInfo;
	}
	
	public List<DDGActivity> getActivities() {
		return this.activities;
	}

	public Optional<List<DDGActivity>> getConditionActivities() {
		return conditionActivities;
	}
}
