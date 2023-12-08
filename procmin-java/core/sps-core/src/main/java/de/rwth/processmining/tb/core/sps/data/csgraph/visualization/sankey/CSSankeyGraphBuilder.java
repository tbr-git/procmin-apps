package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.statistics.ActivityOccurencePosition;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraph;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;

public class CSSankeyGraphBuilder {
	private static final Logger logger = LogManager.getLogger(CSSankeyGraphBuilder.class);
	
	/**
	 * Function that can be used to "sort" the itemsets; 
	 * defaults to implicitly random sorting.
	 * 
	 * This function can be used to display itemsets such that the order of
	 * activities is more intuitive and, therefore, easier to read.
	 */
	private Comparator<String> itemsetSorter;
	
	/**
	 * This abbreviation function  is applied to each activity before displaying it; 
	 * defaults to identity function.
	 * 
	 * Usage is to short the activity names for a less cluttered visualization
	 */
	private Function<String, String> itemAbbreviator;
	
	/**
	 * Cornerstone for which the Sankey flow visualization will be build.
	 */
	private CSGraph csGraph;
	
	//TODO Remove (measurement already runs in Cornerstone Graph)
	/**
	 * Data source that will be used to measure the cornerstone difference on.
	 */
	BiComparisonDataSource<? extends CVariant> dataSource;
	
	public CSSankeyGraphBuilder() {
		// Default itemset sorter
		// Everything is equal => sorting does not change
		itemsetSorter = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return 0;
			}
		};
		
		// Identity function
		itemAbbreviator = s -> s;
		
		this.csGraph = null;
	}
	
	//================================================================================
	// Concrete building routines
	//================================================================================
	
	/**
	 * Build the Sankey graph.
	 * @return Sankey graph.
	 */
	public CSSankeyGraph build() {
		// Parameters not set
		if (itemsetSorter == null || itemAbbreviator == null || csGraph == null || dataSource == null) {
			logger.error("Failed to build the Sankey graph: Missing input parameters: "
					+ "itemsetSorter: {}, itemAbbreviator: {}, csGraph: {}, dataSource:{}",
					itemsetSorter == null,  itemAbbreviator == null, csGraph == null, dataSource == null); 
			return null;
		}
		// Missing data in CSGraph
		if (!csGraph.isComparisonDataInitialized()) {
			logger.error("Comparison data in cornerstone graph not initialized. Abort creating Sankey diagram");
			return null;
		}
		
		// Create raw graph structure
		CSSankeyGraph sankeyGraph = initGraphStructure();

		// Format activity names
		sankeyGraph.getG().vertexSet().stream().forEach(
				v -> v.formatActivities(itemsetSorter, itemAbbreviator));

		// Initialize additional information
		sankeyGraph.calcLayoutInformation();
		// Variant key information
		sankeyGraph.updateDescriptorVariantKeys();
		return sankeyGraph;
	}

	/**
	 * Initialize the pure graph structure.
	 * 
	 * Creates vertices and edges and, finally, a SankeyGraph instance that
	 * only contains the raw graph structure (e.g., layout is not initialized).
	 * 
	 * @return Sankey graph with raw graph structure.
	 */
	private CSSankeyGraph initGraphStructure() {
		// Create an id generator
		Iterator<Integer> idGenerator = new Iterator<Integer>() {
			
			int id = 0;

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Integer next() {
				id++;
				return id;
			}
		};
		
		// Mapping from cornerstone graph to the corresponding left Sankey diagram vertex
		Map<CSGraphVertex, CSSkVertex> csVertex2skVertex = new HashMap<>();
		// Mapping between corresponding Sankey diagram vertices
		BidiMap<CSSkVertex, CSSkVertex> vertexCorrespond = new DualHashBidiMap<>();
		
		Graph<CSSkVertex, CSSkEdge> gSankey = 
				new SimpleDirectedGraph<>(CSSkEdge.class);		// Empty graph
		//========================================
		// Add vertices (itemsets, traces) and coupling edges
		//========================================
		for(CSGraphVertex v : csGraph.getG().vertexSet()) {
			// Create subgraph (two itemset vertices or subgraph with coupling)
			Pair<? extends CSSkVertex, ? extends CSSkVertex> subgraphEntry = 
					v.createAndAddSankeySubgraphs(gSankey, idGenerator);
			csVertex2skVertex.put(v, subgraphEntry.getLeft());
			vertexCorrespond.put(subgraphEntry.getLeft(), subgraphEntry.getRight());
		}
		//========================================
		// Add between itemset edges (left and right)
		//========================================
		for(DefaultEdge e : csGraph.getG().edgeSet()) {
			CSGraphVertex source = csGraph.getG().getEdgeSource(e);
			CSGraphVertex target = csGraph.getG().getEdgeTarget(e);
			
			// Left Sankey edges
			// subset -> superset
			CSSkVertex skSourceLeft = csVertex2skVertex.get(source);
			CSSkVertex skTargetLeft = csVertex2skVertex.get(target);
			gSankey.addEdge(skSourceLeft, skTargetLeft, new CSSkEdge(skTargetLeft.getProbabilityMass(), 0, EdgeType.INTERSET));

			// Right Sankey edges
			// superset -> subset
			CSSkVertex skSourceRight = vertexCorrespond.get(skSourceLeft);
			CSSkVertex skTargetRight = vertexCorrespond.get(skTargetLeft);
			gSankey.addEdge(skTargetRight, skSourceRight, new CSSkEdge(skTargetRight.getProbabilityMass(), 0, EdgeType.INTERSET));
		}
		
		// Add artificial root
		CSSkRoot root = addArtificialRoot(gSankey);
		
		CSSankeyGraph sankeyGraph = new CSSankeyGraph(gSankey, root, vertexCorrespond);
		return sankeyGraph;
	}
	
	private CSSkRoot addArtificialRoot(Graph<CSSkVertex, CSSkEdge> gSankey) {
		// TODO Check how you create ids
		CSSkRoot root = new CSSkRoot(-1);
		// Get itemsets without predecessor
		List<CSSkVertex> itemsetRoots = gSankey.vertexSet().stream()
				.filter(v -> gSankey.inDegreeOf(v) == 0).toList();
		
		// Add root and edges to itemset roots
		gSankey.addVertex(root);
		for (CSSkVertex v : itemsetRoots) {
			gSankey.addEdge(root, v, new CSSkEdge(v.getProbabilityMass(), 0, EdgeType.INTERSET));
		}
		
		return root;
	}
	
	
	//================================================================================
	// Getters and Setters
	//================================================================================

	/**
	 * Sort the categories according to their average position in a trace.
	 * @param actOccPos Average position statistics.
	 * @param categoryMapper Mapping that maps categories to activity names.
	 * @return Builder
	 */
	public CSSankeyGraphBuilder sortItemsetByActivityOccurence(final ActivityOccurencePosition actOccPos) {
		itemsetSorter = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				//TODO Avoid translation to String
				// Compare average occurrence position of category o1 and category o2
				return actOccPos.getAvgPosition().get(o1)
					.compareTo(actOccPos.getAvgPosition().get(o2));
			}
		};
		return this;
	}

	public Comparator<String> getItemsetSorter() {
		return itemsetSorter;
	}

	public CSSankeyGraphBuilder setItemsetSorter(Comparator<String> itemsetSorter) {
		this.itemsetSorter = itemsetSorter;
		return this;
	}

	public CSSankeyGraphBuilder setDataSource(BiComparisonDataSource<? extends CVariant> dataSource) {
		this.dataSource = dataSource;
		return this;
	}

	public Function<String, String> getItemAbbreviator() {
		return itemAbbreviator;
	}

	public CSSankeyGraphBuilder setItemAbbreviator(Function<String, String> itemAbbreviator) {
		this.itemAbbreviator = itemAbbreviator;
		return this;
	}

	public CSGraph getCsGraph() {
		return csGraph;
	}

	public CSSankeyGraphBuilder setCsGraph(CSGraph csGraph) {
		this.csGraph = csGraph;
		return this;
	}
	

}
