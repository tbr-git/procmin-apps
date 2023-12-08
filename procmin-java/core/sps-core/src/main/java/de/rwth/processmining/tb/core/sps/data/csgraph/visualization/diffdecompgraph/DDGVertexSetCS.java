package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

import java.util.Collection;
import java.util.Optional;

import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;

public final class DDGVertexSetCS extends DDGVertexSet {

	public DDGVertexSetCS(int id, DDGVertexProbInfo probabilityInfo,
			Collection<DDGActivity> activities, Optional<Collection<DDGActivity>> conditionActivities, 
			CSGraphVertex csGraphVertex) {
		super(id, probabilityInfo,
				activities, conditionActivities, csGraphVertex);
	}

	@Override
	public DDGVertexType getVertexType() {
		return DDGVertexType.INTERSETCS;
	}
}
