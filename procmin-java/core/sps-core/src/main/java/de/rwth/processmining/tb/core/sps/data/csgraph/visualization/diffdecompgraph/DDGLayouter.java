package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.BarycenterGreedyTwoLayeredBipartiteLayout2D;
import org.jgrapht.alg.drawing.model.Box2D;
import org.jgrapht.alg.drawing.model.LayoutModel2D;
import org.jgrapht.alg.drawing.model.MapLayoutModel2D;
import org.jgrapht.alg.drawing.model.Point2D;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class DDGLayouter {
	
	/**
	 * Assign the correct visualization depth level values.
	 * 
	 * @param ddG Graph for layouting.
	 */
	public static void updateLevelValues(DiffDecompGraph ddG) {
		/*
		 * --- Algorithm description --- 
		 * For the set vertices:
		 * Does topological order iteration over the {@link DiffDecompGraph} updating the level depth.
		 * For the flow splitter:
		 * Depth will be max set vertex depth + 1.
		 * For the EMD trace vertices:
		 * Depth will be max set vertex depth + 2/3 for left and right trace vertices.
		 *	--- Note on Performance ---
		 * Current implementation is not optimal and will touch vertices quite often.
		 * For example, we could restrict the intial depth-first traversal of the set vertices to only touch
		 * set vertices. However, the current graph structure makes this quite difficult.
		 * 
		 * Since graphs will be quite small (otherwise visualization will be a mess anyway), we stick 
		 * to the easier but less efficient implementation.
		 */

		// Get references
		Graph<DDGVertex, DDGEdge> g = ddG.getGraph();
		DDGVertex root = ddG.getRoot();
		
		// Set depth of root
		root.setVisLevel(0);
		
		////////////////////////////////////////
		// Update depth of set vertices
		////////////////////////////////////////
		int maxDepthTmp = -1;
		// Topological iteration
		// -> visit all predecessors such that the depth of all predecessors has been assigned
		// when the vertex is visited
		TopologicalOrderIterator<DDGVertex, DDGEdge> itG = new TopologicalOrderIterator<>(g);
		while (itG.hasNext()) {
			DDGVertex v = itG.next();
			OptionalInt maxInpLevel = g.incomingEdgesOf(v).stream()
					.mapToInt(e -> g.getEdgeSource(e).getVisLevel()).max();
			if (maxInpLevel.isPresent()) {
				v.setVisLevel(maxInpLevel.getAsInt() + 1);
				maxDepthTmp = Math.max(maxDepthTmp, maxInpLevel.getAsInt() + 1);
			}
			else {
				v.setVisLevel(0);
			}
			
		}

		////////////////////////////////////////
		// Update depth for flow splitter vertices / EMDTrace vertices
		////////////////////////////////////////
		final int maxDepth = maxDepthTmp;
		g.vertexSet().forEach(v -> {
			if (v instanceof DDGVertexEMDSplit) {
				v.setVisLevel( maxDepth + 1);
			}
			else if (v instanceof DDGVertexTrace vTrace) {
				switch (vTrace.getLogSide()) {
				case LEFT:
					vTrace.setVisLevel(maxDepth + 2); 
					break;
				case RIGHT:
					vTrace.setVisLevel(maxDepth + 3); 
					break;
				default:
					throw new RuntimeException("Unexpected log side. Cannot assign the DDG depth");
				}
			}
		});
	}

	public static void layoutEMDFlows(DiffDecompGraph ddG) {
		final Graph<DDGVertex, DDGEdge> g = ddG.getGraph();
		g.vertexSet().parallelStream()
			.filter(DDGVertexEMDSplit.class::isInstance)
			.map(DDGVertexEMDSplit.class::cast)
			.forEach(v -> layoutSingleEMDFlow(g, v));
	}
	
	protected static void layoutSingleEMDFlow(Graph<DDGVertex, DDGEdge> g, DDGVertexEMDSplit vFlowInducingSplit) {

		// Iterator
		DepthFirstIterator<DDGVertex, DDGEdge> itG = new DepthFirstIterator<>(g, vFlowInducingSplit);

		////////////////////////////////////////
		// Set of EMD Trace vertices
		////////////////////////////////////////
		Set<DDGVertexTrace> emdTraceVertices = new HashSet<>();
		StreamSupport
			.stream(Spliterators.spliteratorUnknownSize(itG, Spliterator.ORDERED), false)
			.filter(v -> v instanceof DDGVertexTrace)
			.map(v -> (DDGVertexTrace) v)
			.forEach(emdTraceVertices::add);

		////////////////////////////////////////////////////////////
		// Layout EMD Trace subgraph
		////////////////////////////////////////////////////////////
		Graph<DDGVertex, DDGEdge> gEMD = new AsSubgraph<>(g, emdTraceVertices);
		Set<DDGVertex> verticesLeft = emdTraceVertices.stream()
				.filter(v -> v.getLogSide() == LogSide.LEFT)
				.collect(Collectors.toSet());
		////////////////////////////////////////////////////////////
		// Run Layout Algorithm from JGraphT
		////////////////////////////////////////////////////////////
		// Configure Algorithm
		// Left side has fixed order (sorted by probability)
		// Order on right side will be optimized to minimize crossings
		BarycenterGreedyTwoLayeredBipartiteLayout2D<DDGVertex, DDGEdge> bipartiteLayoutAlg = 
				new BarycenterGreedyTwoLayeredBipartiteLayout2D<>(verticesLeft, new Comparator<DDGVertex>() {

					@Override
					public int compare(DDGVertex o1, DDGVertex o2) {
						DDGVertexTrace u = (DDGVertexTrace) o1;
						DDGVertexTrace v = (DDGVertexTrace) o2;
						
						return Double.compare(u.getProbability(), v.getProbability());
					}
				}, true);
		LayoutModel2D<DDGVertex> layoutEMD = 
				new MapLayoutModel2D<>(new Box2D(2, 
						Math.max(emdTraceVertices.size() - verticesLeft.size(), verticesLeft.size()) - 1));
		// Run algorithm
		bipartiteLayoutAlg.layout(gEMD, layoutEMD);
		
		////////////////////////////////////////
		// Derive order from 2D layout
		////////////////////////////////////////
		Map<Boolean, List<Entry<DDGVertex, Point2D>>> mapLeftRightLayout = 
				StreamSupport.stream(layoutEMD.spliterator(), false)
					.collect(Collectors.partitioningBy(
							e -> e.getKey() instanceof DDGVertexTrace vTrace && vTrace.getLogSide() == LogSide.LEFT));
		
		// Sort Right (and left although it is fixed (top down or bottom up?)
		mapLeftRightLayout.get(true).sort((e1, e2) -> Double.compare(e1.getValue().getY(), e2.getValue().getY()));
		mapLeftRightLayout.get(false).sort((e1, e2) -> Double.compare(e1.getValue().getY(), e2.getValue().getY()));
		
		// Assign order keys
		int i = 0;
		// Left
		for(var v : mapLeftRightLayout.get(true)) {
			((DDGVertexTrace) v.getKey()).setIntraEMDSideOrdering(i);
			i++;
		}
		// Right
		i = 0;
		for(var v : mapLeftRightLayout.get(false)) {
			((DDGVertexTrace) v.getKey()).setIntraEMDSideOrdering(i);
			i++;
		}

//		layoutEMD.forEach(e -> System.out.println(e.getKey().getId() + ": " + e.getValue()));
	}
	
	/**
	 * Classify the edges in the hierarchy part of the graph whether they are
	 * in the tree that is used for layouting or not.
	 * 
	 * @param ddG
	 */
	public static void updateSetLayoutTreeEdges(DiffDecompGraph ddG) {

		final Graph<DDGVertex, DDGEdge>  g = ddG.getGraph();
		
		//////////////////////////////
		// Setup Edge Comparators
		//////////////////////////////
		// 1. Compare by depth of source vertex
		// -> Prefer vertices that are deeper to avoid "backward" edges later
		final Comparator<DDGEdge> edgeSortingDepthTmp = new Comparator<>() {
			@Override
			public int compare(DDGEdge e1, DDGEdge e2) {

				DDGVertexSet v1 = (DDGVertexSet) g.getEdgeSource(e1);
				DDGVertexSet v2 = (DDGVertexSet) g.getEdgeSource(e2);
				
				// Compare by depth
				return Integer.compare(v1.getVisLevel(), v2.getVisLevel());
			}
		};

		final Comparator<DDGEdge> edgeSortingDepth = edgeSortingDepthTmp.reversed();
		
		// 2. Compare by probabilities of source vertex
		// -> Prefer vertices that are re more important
		final Comparator<DDGEdge> edgeSortingProbabilityTmp = new Comparator<>() {
			@Override
			public int compare(DDGEdge e1, DDGEdge e2) {

				DDGVertexSet v1 = (DDGVertexSet) g.getEdgeSource(e1);
				DDGVertexSet v2 = (DDGVertexSet) g.getEdgeSource(e2);
				
				// Compare by "importance" of source
				// Covered probability mass in logs (left + right)
				return Double.compare(
						v1.getProbabilityInfo().probNonCondLeft() 
							+ v1.getProbabilityInfo().probNonCondRight(),
						v2.getProbabilityInfo().probNonCondLeft() 
							+ v2.getProbabilityInfo().probNonCondRight());
			}
		};
		// Split in two because otherwise we get generics problem 
		// (reversed will return a Object comparator)
		final Comparator<DDGEdge> edgeSortingProbabilityHierarchy = edgeSortingProbabilityTmp.reversed();

		//////////
		// Final composed comparator
		//////////
		final Comparator<DDGEdge> edgeSorting = edgeSortingDepth.thenComparing(edgeSortingProbabilityHierarchy);
		
		//////////////////////////////
		// Classify Tree Edges
		//////////////////////////////
		g.vertexSet().stream().filter(v -> ((v instanceof DDGVertexSet) || (v instanceof DDGVertexEMDSplit)))
			.forEach(v -> {
				// Incoming edges 
				Optional<DDGEdge> treeEdge = g.incomingEdgesOf(v).stream()
						.sorted(edgeSorting).findFirst();
				// If parent
				treeEdge.ifPresent(e -> ((DDGEdgeInterSet) e).setEdgeInLayoutTree(true));
			}
		);
	}

}
