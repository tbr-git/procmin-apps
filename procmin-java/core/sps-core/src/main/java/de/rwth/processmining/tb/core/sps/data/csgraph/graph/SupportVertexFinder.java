package de.rwth.processmining.tb.core.sps.data.csgraph.graph;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraph;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.util.bitsets.BitSetUtil;

public class SupportVertexFinder {
	
	/**
	 * Handle to the {@code HFDDGraph} where this find
	 * should retrieve support vertices from.
	 * 
	 */
	private final HFDDGraph hfddGraph;
	
	private final List<HFDDVertex> vertices;
	
	/**
	 * Constructor. Requires a handle to a HFDDGraph so that, potentially,
	 * data structures can be initialized once.
	 * @param hfddGraph Handle to the HFDDGraph.
	 */
	public SupportVertexFinder(HFDDGraph hfddGraph) {
		this.hfddGraph = hfddGraph;

		vertices = new LinkedList<>(this.hfddGraph.getVertices());
		// Sort by cardinality of the itemsets
		vertices.sort((u, v) -> Integer.compare(
				u.getVertexInfo().getActivities().cardinality(),
				v.getVertexInfo().getActivities().cardinality()));
	}

	/**
	 * Find a support vertex for the two provided vertices.
	 * Retrieves a support vertex from the HFDDGraph that was initially provided.
	 * 
	 * @param u
	 * @param v
	 * @return Support vertex or 
	 */
	public Optional<HFDDVertex> findSupportVertexFor(HFDDVertex u, HFDDVertex v) {
		// Get support itemset by intersection
		BitSet activitiesV = v.getVertexInfo().getActivities();
		BitSet activitiesU = u.getVertexInfo().getActivities();
		BitSet tmp = (BitSet) activitiesV.clone();
		tmp.and(activitiesU);

		////////////////////////////////////////
		// If support item is equal to one 
		// of the involved vertices
		////////////////////////////////////////
		if (tmp.cardinality() == activitiesV.cardinality()) {
			return Optional.of(v);
		}
		else if (tmp.cardinality() == activitiesU.cardinality()) {
			return Optional.of(u);
		}
		else {
			////////////////////////////////////////
			// Support vertex is a proper subset
			////////////////////////////////////////
			// Get handle to support vertex by itemset
			HFDDVertex s = hfddGraph.getVertex(tmp);
			////////////////////////////////////////
			// Intersection-based support vertex is 
			// not contained in the graph
			////////////////////////////////////////
			if (s == null) {
				// Search for the maximum size (maximum support) vertex that is a subset of the 
				// intersection-based support vertex
				int intersectSizeClosest = -1;
				double supportClosest = -1;
				for (HFDDVertex w : hfddGraph.getGraph().vertexSet()) {
					if (BitSetUtil.isSubset(w.getVertexInfo().getActivities(), tmp)	// w is subset of tmp and
							&& (w.getVertexInfo().getActivities().cardinality() > intersectSizeClosest // larger subset
								|| (w.getVertexInfo().getActivities().cardinality() == intersectSizeClosest  
										&&  Math.max(w.getVertexInfo().getBaseMeasurement().getProbLeftNonEmpty(), 
												w.getVertexInfo().getBaseMeasurement().getProbLeftNonEmpty())
											> supportClosest))) { // or larger likelihood in one of the languages
						s = w;
						intersectSizeClosest = w.getVertexInfo().getActivities().cardinality();
						supportClosest = Math.max(w.getVertexInfo().getBaseMeasurement().getProbLeftNonEmpty(), 
												w.getVertexInfo().getBaseMeasurement().getProbLeftNonEmpty());
					}
				}
			}
			////////////////////////////////////////
			// Not even a proper subset of the 
			// Intersection-based support vertex is 
			// not contained in the graph
			////////////////////////////////////////
			if (s == null) { // If search does not succeed, s is still null
				// The only support vertex is the empty set / there is no real
				// supporting set -> Remove both from support request and 
				// leave them disconnected
				return Optional.empty();
			}
			else {
				return Optional.of(s);
			}
		}
	}

}
