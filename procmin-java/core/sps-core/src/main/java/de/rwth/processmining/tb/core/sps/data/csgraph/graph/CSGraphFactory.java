package de.rwth.processmining.tb.core.sps.data.csgraph.graph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.SimpleDirectedGraph;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexCondition;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraph;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.sps.util.GraphConnectionUtil;
import de.rwth.processmining.tb.core.util.bitsets.BitSetInclusionComparator;

public class CSGraphFactory {
	
	public static CSGraph buildFromHFDDGraph(HFDDGraph hfddGraph, 
			Collection<Integer> cornerstoneIds,
			Optional<ArrayList<Set<VertexCondition>>> conditionBase) {
		Collection<Integer> supportIds = findSupportVerticesByGreedyIntersection(hfddGraph, cornerstoneIds);
		return buildFromHFDDGraph(hfddGraph, cornerstoneIds, supportIds, conditionBase);
	}
	
	public static CSGraph buildFromHFDDGraph(HFDDGraph hfddGraph, Collection<Integer> cornerstoneIds, 
			Collection<Integer> supportIds, Optional<ArrayList<Set<VertexCondition>>> conditionBase) {
		
		Graph<CSGraphVertex, DefaultEdge> g = 
				new SimpleDirectedGraph<>(DefaultEdge.class);		// Empty graph
		
		//================================================================================
		// Create and add vertices to Graph
		//================================================================================
		// Corner stone vertices
		for(Integer iV : cornerstoneIds) {
			HFDDVertex hfddVertex = hfddGraph.getVertexbyID(iV); 	// Vertex for Id
			// Create 
			CSGraphVertexCS<BasicTraceCC> v = new CSGraphVertexCS<BasicTraceCC>(hfddVertex);
			g.addVertex(v); 	// Add to graph
		}
		// Support vertices
		for(Integer iV : supportIds) {
			HFDDVertex hfddVertex = hfddGraph.getVertexbyID(iV); 	// Vertex for Id
			// Create 
			CSGraphVertexSupport v = new CSGraphVertexSupport(hfddVertex);
			g.addVertex(v); 	// Add to graph
		}
		
		//================================================================================
		// Generate edges
		//================================================================================
		/*
		 * Generate edges by:
		 * 	1. Generate all edges w.r.t. itemset inclusion
		 * 	2. Apply transitive reduction
		 */
		GraphConnectionUtil.addSPOEdges(g, new Comparator<CSGraphVertex>() {
			
			Comparator<BitSet> activitySetComparator = new BitSetInclusionComparator();

			@Override
			public int compare(CSGraphVertex u, CSGraphVertex v) {
				return activitySetComparator.compare(
						u.getHfddVertexRef().getVertexInfo().getActivities(), 
						v.getHfddVertexRef().getVertexInfo().getActivities());
			}
		});
		// Apply transitive reduction to the graph
		TransitiveReduction.INSTANCE.reduce(g);
		return new CSGraph(g, conditionBase);
	}
	
	/**
	 * Create a set of support vertex by iteratively adding the closest lca among all vertex pair's lca.
	 * 
	 * @param hfddGraph
	 * @param cornerstoneIds
	 * @return
	 */
	protected static Collection<Integer> findSupportVerticesByGreedyIntersection(HFDDGraph hfddGraph, 
			Collection<Integer> cornerstoneIds) {
		Map<Integer, Boolean> open4Support = new HashMap<>();
		for (Integer vId : cornerstoneIds) {
			open4Support.put(vId, true);
		}
		// Entry: (Intersection size, size of the larger subset, id1, id2)
		PriorityQueue<int[]> queue = new PriorityQueue<>(new Comparator<int[]>() {

			@Override
			public int compare(int[] o1, int[] o2) {
				int c = 0;
				// Sort by size of intersection
				c = -1 * Integer.compare(o1[0], o2[0]);
				if (c != 0) {
					return c;
				}
				// Break draws by size of the bigger set 
				c = -1 * Integer.compare(o1[1], o2[1]);
				return c;
			}
		});
	
		// Fix some order of the corner stones
		int[] orderedCornerStones = cornerstoneIds.stream().mapToInt(i -> i).toArray();

		// Generate queue entries for all cornerstone combinations
		for (int i = 0; i < orderedCornerStones.length - 1; i++) {
			int idV = orderedCornerStones[i];
			BitSet activitiesV = hfddGraph.getVertexbyID(idV).getVertexInfo().getActivities();
			for (int j = i + 1; j < orderedCornerStones.length; j++) {
				int idU = orderedCornerStones[j];

				// Set intersection
				BitSet activitiesU = hfddGraph.getVertexbyID(idU).getVertexInfo().getActivities();
				BitSet tmp = (BitSet) activitiesV.clone();
				tmp.and(activitiesU);
				// TODO if not complete
				
				// Create entry
				int[] e = new int[] {tmp.cardinality(), Math.max(activitiesV.cardinality(), activitiesU.cardinality()), idV, idU};
				queue.add(e);
			}
		}
		
		// List of support vertex ids
		List<Integer> supportIds = new LinkedList<>();
		// Support vertex finder
		SupportVertexFinder supVFinder = new SupportVertexFinder(hfddGraph);
		HFDDVertex u, v, supportVert;
		Optional<HFDDVertex> sOpt;
		////////////////////////////////////////////////////////////////////////////////
		// Intersect closest two vertices and add support vertex
		// (if vertex corresponding to the intersection is not yet a
		// support or cornerstone vertex)
		////////////////////////////////////////////////////////////////////////////////
		// Run until there are no valid (untested) intersection candidates anymore
		for (int[] e = queue.poll(); e != null; e = queue.poll()) {
			// Skip if one vertex already "has" a support vertex
			if (!(open4Support.get(e[2]) && open4Support.get(e[3]))) {
				continue;
			}
			
			int idV = e[2];
			int idU = e[3];
			
			u = hfddGraph.getVertexbyID(idU);
			v = hfddGraph.getVertexbyID(idV);
			
			////////////////////////////////////////
			// Search Support Vertex
			////////////////////////////////////////
			sOpt = supVFinder.findSupportVertexFor(u, v);
			
			////////////////////////////////////////
			// Analyze Support Vertex
			////////////////////////////////////////
			if (sOpt.isEmpty()) {
				// The only support vertex is the empty set / there is no real
				// supporting set 
				// -> Maybe we can still support one of the vertices in 
				// a different context => Ignore for now
			}
			else {
				supportVert = sOpt.get();
				////////////////////////////////////////
				// If support item is equal to one 
				// of the involved vertices
				////////////////////////////////////////
				// => Remove need for support for the other
				if (supportVert == v) {
					open4Support.put(idU, false);
				}
				else if (supportVert == u) {
					open4Support.put(idV, false);
				}
				else {
					////////////////////////////////////////
					// Support vertex is a proper subset
					////////////////////////////////////////
					open4Support.put(idU, false);
					open4Support.put(idV, false);
					int idS = supportVert.getId();
				
					// If the vertex is neither a support vertex nor a cornerstone vertex
					// Not necessarily covered by the check before
					if (! open4Support.containsKey(idS)) {
						supportIds.add(idS);
						BitSet activitiesS = supportVert.getVertexInfo().getActivities();
						
						// Create relation entries w.r.t. all open vertices
						for (Entry<Integer, Boolean> openSetEntry : open4Support.entrySet()) {
							if (!openSetEntry.getValue()) {
								continue;
							}
							
							// Get intersection size
							HFDDVertex w = hfddGraph.getVertexbyID(openSetEntry.getKey());
							BitSet activitiesW = w.getVertexInfo().getActivities();
							BitSet tmp = (BitSet) activitiesS.clone();
							tmp.and(activitiesW);
							
							queue.add(new int[] {tmp.cardinality(), Math.max(activitiesS.cardinality(), activitiesW.cardinality()), idS, w.getId()});
						}
						// Support vertex also requires support
						open4Support.put(supportVert.getId(), true);
					}
				}
			}
		}
		return supportIds;
		
	}

}
