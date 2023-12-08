package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gnu.trove.map.hash.TObjectIntHashMap;


@JsonSerialize(using = CSSankeyGraphSerializer.class)
public class CSSankeyGraph {
	
	/**
	 * Itemset correspondences between the left and right side.
	 */
	private BidiMap<CSSkVertex, CSSkVertex> itemsetCorrespond;

	/**
	 * Handle to the JGraphtT graph.
	 */
	private final Graph<CSSkVertex, CSSkEdge> g;
	
	/**
	 * Handle to the artificial root.
	 */
	private final CSSkRoot root;
	
	public CSSankeyGraph(Graph<CSSkVertex, CSSkEdge> graph, CSSkRoot root, 
			BidiMap<CSSkVertex, CSSkVertex> itemsetCorrespond) {
		this.g = graph;
		this.itemsetCorrespond = itemsetCorrespond;
		this.root = root;
	}

	
	/**
	 * Calculate and populate layouting information.
	 */
	public void calcLayoutInformation() {
		this.calculateLevelInfo();
		// Must be called after the levels were initialized
		this.calculateIntraLevelOrdering();
		this.addMatchingReductionInfo();
	}
	
	/**
	 * Endow each vertex with a level in the Sankey Diagram such that:
	 * <p><ul>
	 * <li> Right itemsets are mirrored wrt their corresponding left itemset
	 * <li> From level to right starting at maximum itemset level + 1 we have
	 * <ol>
	 * <li> Left flow split vertices 
	 * <li> Left EMD trace descriptors
	 * <li> Right EMD trace descriptors
	 * <li> Right flow split vertices 
	 * </ol><p>
	 * </ul>
	 * 
	 */
	private void calculateLevelInfo() {
		// TODO Profiling (is this a bottleneck. Probably not because graph is small)
		// Use as many JGrapht functions as possible 
		// => not necessarily the most efficient implementation
		
		Set<CSSkVertex> leftItemsetVertices = this.g.vertexSet().stream()
				.filter(CSSkVertex::isLeft) 						// Only left vertices
				.filter(v -> v instanceof CSSkItemsetVertex) 		// Only itemsets
				.collect(Collectors.toSet());
		// Add the root
		leftItemsetVertices.add(root);
		
		// Subgraph that only contains the left itemset vertices (and the root)
		AsSubgraph<CSSkVertex, CSSkEdge> gLeftItemsets = new AsSubgraph<>(this.g, leftItemsetVertices);
		root.setSkLevel(0);


		/* In Topological ordering, the Sankey level of a vertex will be 
		 * the maximum level of its predecessors plus one.
		 */
		TopologicalOrderIterator<CSSkVertex, CSSkEdge> itLeftItemsets = 
				new TopologicalOrderIterator<CSSkVertex, CSSkEdge>(gLeftItemsets);
		// Only root should be the artificial root
		assert itLeftItemsets.next() == root;
		int maxTotalLevel = 0;
		while(itLeftItemsets.hasNext()) {
			CSSkVertex v = itLeftItemsets.next();
			// Max predecessor level
			int maxPredLevel = 0;
			for (CSSkVertex u : Graphs.predecessorListOf(g, v)) {
				maxPredLevel = Math.max(maxPredLevel, u.getSkLevel());
			}
			v.setSkLevel(maxPredLevel + 1);
			maxTotalLevel = Math.max(maxTotalLevel, maxPredLevel + 1);
		}
		
		// Set left flow vertices to max itemset level + 1 (left) and 
		// max itemset + 4 (right)
		final int skFlowSplitLeftLevel = maxTotalLevel + 1;
		g.vertexSet().stream()
			.filter(v -> v instanceof CSSkFlowSplit)
			.forEach(v -> v.setSkLevel(skFlowSplitLeftLevel + (v.isLeft() ? 0 : 3)));

		// Set left trace vertices to max itemset level + 2 (left) and 
		// max itemset + 3 (right)
		final int skTraceLeftLevel = maxTotalLevel + 2;
		g.vertexSet().stream()
			.filter(v -> v instanceof CSSkTraceVertex)
			.forEach(v -> v.setSkLevel(skTraceLeftLevel + (v.isLeft() ? 0 : 1)));
		
		// Mirror the levels to the right and side
		// max itemset level + 4 + 1 + (max itemset level - corresponding vertex level)
		for (Entry<CSSkVertex, CSSkVertex> p : itemsetCorrespond.entrySet()) {
			p.getValue().setSkLevel(maxTotalLevel + 4 + 1 + (maxTotalLevel - p.getKey().getSkLevel()));
		}
		
	}

	/**
	 * Endow with additional information required for a matching-based visualization.
	 * In particular, flows between empty traces should be removed and probabilities must be adapted.
	 */
	public void addMatchingReductionInfo() {

		// Find left empty traces
		List<CSSkTraceVertex> leftEmptyTraces = this.g.vertexSet().stream()
				.filter(CSSkVertex::isLeft) 						// Only left vertices
				.filter(v -> v instanceof CSSkTraceVertex) 		// Only traces
				.map(v -> (CSSkTraceVertex) v)
				.filter(v -> v.getActivityDescriptors().length == 0) // Only empty traces
				.collect(Collectors.toList());

		List<CSSkVertex> rightTraces;
		CSSkTraceVertex uTrace;
		// Consider all left empty traces
		for (CSSkTraceVertex v : leftEmptyTraces) {
			// Get matching right traces
			rightTraces = Graphs.successorListOf(this.g, v);
			for (CSSkVertex u : rightTraces) {
				uTrace = (CSSkTraceVertex) u;
				// Right trace is also empty
				if (uTrace.getActivityDescriptors().length == 0) {
					////////////////////
					// Involved vertices
					// * Traces (vertices in outer loops)
					// * Split vertices
					////////////////////
					// Split vertices
					CSSkVertex flowSplitV = this.g.getEdgeSource(
							this.g.incomingEdgesOf(v).stream().findFirst().get());
					CSSkVertex flowSplitU = this.g.getEdgeTarget(
							this.g.outgoingEdgesOf(u).stream().findFirst().get());
						// Only reduce the matching weight of the two vertices
						// by removing the flow between the empty sets
					////////////////////
					// Involved edges
					// * Split left -> empty trace left
					// * empty trace left -> empty trace right
					// * empty trace right -> Split right
					////////////////////
					// Split -> Empty trace
					// Left
					CSSkEdge splitFlowEdgeV = this.g.getEdge(flowSplitV, v);
					// Right
					CSSkEdge splitFlowEdgeU = this.g.getEdge(u, flowSplitU);
					// Trace -> Trace
					CSSkEdge emptyFlowEdge = this.g.getEdge(v, u);
					
					// Empty flow
					double emptyFlow = emptyFlowEdge.getFlow();

					// Check if left empty trace is "lonely"
					if (rightTraces.size() == 1) {
						////////////////////
						// Adapt Weights and relevance
						////////////////////
						// Vertices:
						// * Left Trace: 0 (irrelevant)
						// * Right Trace: -= empty flow  (still relevant)
						// * Left split: Already determined by itemset probability
						// * Right split: Already determined by itemset probability
						////////////////////
						v.setMatchingWeight(0);
						v.setMatchingRelevant(false);
						uTrace.setMatchingWeight(uTrace.getMatchingWeight() - emptyFlow);
						// Edges:
						// * Left split -> left trace: 0 (irrelevant)
						// * Right trace -> Right split: -= empty flow (still relevant)
						// * Left trace -> Right trace: 0 (irrelevant)
						splitFlowEdgeV.setMatchingFlow(0);
						splitFlowEdgeV.setMatchingRelevant(false);
						emptyFlowEdge.setMatchingFlow(0);
						emptyFlowEdge.setMatchingRelevant(false);
						splitFlowEdgeU.setMatchingFlow(splitFlowEdgeU.getMatchingFlow() - emptyFlow);
					}
					else if (this.g.degreeOf(uTrace) == 2) {
						////////////////////
						// Adapt Weights and relevance 
						// (symmetric to upper case)
						////////////////////
						// Vertices:
						// * Left Trace: -= empty flow  (still relevant)
						// * Right Trace: 0 (irrelevant)
						// * Left split: Already determined by itemset probability
						// * Right split: Already determined by itemset probability
						////////////////////
						uTrace.setMatchingWeight(0);
						uTrace.setMatchingRelevant(false);
						v.setMatchingWeight(v.getMatchingWeight() - emptyFlow);
						// Edges:
						// * Left split -> Left trace: -= empty flow (still relevant)
						// * Left trace -> Right trace: 0 (irrelevant)
						// * Right trace -> Right split:  0 (irrelevant)
						splitFlowEdgeV.setMatchingFlow(splitFlowEdgeV.getMatchingFlow() - emptyFlow);
						emptyFlowEdge.setMatchingFlow(0);
						emptyFlowEdge.setMatchingRelevant(false);
						splitFlowEdgeU.setMatchingFlow(0);
						splitFlowEdgeU.setMatchingRelevant(false);
					}
					else {
						////////////////////
						// !!! For a proper metric, this will never occur !!!
						// Adapt Weights and relevance 
						////////////////////
						// Vertices:
						// * Left Trace: -= empty flow  (still relevant)
						// * Right Trace: -= empty flow  (still relevant)
						// * Left split: Already determined by itemset probability
						// * Right split: Already determined by itemset probability
						////////////////////
						v.setMatchingWeight(v.getMatchingWeight() - emptyFlow);
						uTrace.setMatchingWeight(uTrace.getMatchingWeight() - emptyFlow);
						// Edges:
						// * Left split -> Left trace: -= empty flow (still relevant)
						// * Left trace -> Right trace: 0 (irrelevant)
						// * Right trace -> Right split:  -= empty flow (still relevant)
						splitFlowEdgeV.setMatchingFlow(splitFlowEdgeV.getMatchingFlow() - emptyFlow);
						emptyFlowEdge.setMatchingFlow(0);
						emptyFlowEdge.setMatchingRelevant(false);
						splitFlowEdgeU.setMatchingFlow(splitFlowEdgeU.getMatchingFlow() - emptyFlow);
					}
				}
			}
		}
	}

	
	private void calculateIntraLevelOrdering() {
		//================================================================================
		// Intra Level Keys for the Itemset Vertices
		//================================================================================
		// Left itemsets
		List<CSSkVertex> leftItemsetVertices = this.g.vertexSet().stream()
				.filter(CSSkVertex::isLeft) 						// Only left vertices
				.filter(v -> v instanceof CSSkItemsetVertex) 		// Only itemsets
				.collect(Collectors.toList());
		// Sort by Sankey level
		leftItemsetVertices.sort((o1, o2) -> Integer.compare(o1.getSkLevel(), o2.getSkLevel()));
		
		// List that will be used to order the vertices within the same level
		List<Pair<CSSkVertex, Integer[]>> perLevelVertices = new LinkedList<>();
		
		// Iterate over all vertices that have not been considered yet (ordered by Sankey level)
		ListIterator<CSSkVertex> itOuter = leftItemsetVertices.listIterator();
		
		/*
		 * Intra level sorting (vertex, sorting key) generator.
		 * Key: Gathers the intra-level sorting keys of the predecessors and sorts them
		 */
		Function<CSSkVertex, Pair<CSSkVertex, Integer[]>> keyGen = 
				v -> Pair.of(v, Graphs.predecessorListOf(g, v).stream() 	// Get predecessors
					.map(u -> u.getIntraLevelKey())
					.sorted()
					.toArray(Integer[]::new));
		
		int curOrderKey = 0;
		while(itOuter.hasNext()) {
			perLevelVertices.clear();
			// Add first vertex of the novel level
			CSSkVertex v = itOuter.next();
			perLevelVertices.add(keyGen.apply(v));
			// Add remaining vertices of the same level
			while(itOuter.hasNext()) {
				CSSkVertex u = itOuter.next();
				if (u.getSkLevel() == v.getSkLevel()) {
					perLevelVertices.add(keyGen.apply(u));
				}
				else {
					// Push back the vertex from the next level
					itOuter.previous();
					break;
				}
			}
			// Sort based on minimum predecessor intra-level key
			perLevelVertices.sort((v1, v2) -> Arrays.compare(v1.getRight(), v2.getRight()));
			for (Pair<CSSkVertex, Integer[]> p : perLevelVertices) {
				p.getLeft().setIntraLevelKey(curOrderKey);
				curOrderKey++;
			}
		}
			
		//========================================
		// Mirror to the right vertices
		//========================================
		for (Entry<CSSkVertex, CSSkVertex> p : itemsetCorrespond.entrySet()) {
			p.getValue().setIntraLevelKey(p.getKey().getIntraLevelKey());
		}
		
		//================================================================================
		// Initialize Flow and Trace Descriptor Intra-level Keys
		//================================================================================
		// Get left flow split vertices 
		List<CSSkVertex> lFlowSplitLeft = g.vertexSet().stream()
			.filter(CSSkVertex::isLeft)						// Left vertices
			.filter(u -> u instanceof CSSkFlowSplit)		// Flow split vertices
			.toList();
		
		for(CSSkVertex f : lFlowSplitLeft) {
			// Flow vertex must have exactly only one predecessor (i.e., an itemset vertex)
			// Use this vertex' intra-level sk key in the induced coupling graph
			int intraSkLevel = Graphs.predecessorListOf(g, f).get(0).getIntraLevelKey();
			
			// Initiate breadth first traversal starting from the flow vertex
			BreadthFirstIterator<CSSkVertex, CSSkEdge> it = new BreadthFirstIterator<>(g, f);
			while(it.hasNext()) {
				// All traversed vertices receive the same intra level key
				CSSkVertex u = it.next();
				u.setIntraLevelKey(intraSkLevel);
				/* Stop breadth first traversal after the opposite flow vertex was encountered. 
				 * Since we use breadth first traversal all coupling vertices have been
				 * visited at this point.
				 */
				if(!u.isLeft() && u instanceof CSSkFlowSplit) {
					break;
				}
			}
		}
	}

	protected void updateDescriptorVariantKeys() {
		Set<CSSkTraceVertex> traceVertices = this.g.vertexSet().stream()
				.filter(v -> v instanceof CSSkTraceVertex)
				.map(v -> (CSSkTraceVertex) v)
				.collect(Collectors.toSet());
		
		TObjectIntHashMap<List<String>> descriptor2VariantId = new TObjectIntHashMap<>();
		int nextFreeId = 0;
		for (CSSkTraceVertex v : traceVertices) {
			/*
			 * TODO
			 * Quite inefficient using the hash code of the TraceDescriptor would be more efficient.
			 * Hashing the trace vertex does not work because of identity hashing
			 */
			List<String> tmp = Arrays.asList(v.getActivityDescriptors());
			int variantId;
			// Check if this descriptor was already encountered
			if (!descriptor2VariantId.contains(tmp)) {
				variantId = nextFreeId;
				nextFreeId++;
				descriptor2VariantId.put(tmp, variantId);
			}
			else {
				variantId = descriptor2VariantId.get(tmp);
			}
			v.setVariant(variantId);
		}
	}
	
	//================================================================================
	// Getters and Setters
	//================================================================================
	
	public Graph<CSSkVertex, CSSkEdge> getG() {
		return g;
	}
}
