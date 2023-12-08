package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;

import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.StochasticLanguageIterator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;
import de.rwth.processmining.tb.core.emd.language.transformer.ProbMassNonEmptyTrace;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexCondition;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.VertexConditioningLogTransformer;
import de.rwth.processmining.tb.core.sps.data.csgraph.CSMeasurementTypes;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraph;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertexCS;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertexSupport;


public class DiffDecompGraphBuilder {
	private static final Logger logger = LogManager.getLogger(DiffDecompGraphBuilder.class);

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
	 * Cornerstone for which the Difference Decomposition visualization will be build.
	 */
	private CSGraph csGraph;
	
	/**
	 * Next free edge id.
	 */
	private int edgeId;
	
	/**
	 * Measurement type that will be considered when building
	 * the graph from the cornerstone graph.
	 */
	//TDO We assume that the measurement type exists!!!
	private DDGGraphType ddgType;

	public DiffDecompGraphBuilder() {
		//TODO Create better defaults
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
		this.ddgType = DDGGraphType.RESIDUAL;
		
		this.csGraph = null;
		this.edgeId = 0;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// Building Routines
	////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Build the graph.
	 * @return The created graph or null 
	 */
	public DiffDecompGraph build() throws IllegalArgumentException, IllegalStateException {
		// Parameters not set
		if (itemsetSorter == null || itemAbbreviator == null || csGraph == null) {
			throw new IllegalArgumentException(String.format("Failed to build the difference deomposition graph."
					+ "Missing Arguments:" + "[itemsetSorter: %b" + "itemAbbreviator: %b" + "csGraph: %b",
					itemsetSorter == null, itemAbbreviator == null, csGraph == null));
		}

		// Missing data in CSGraph
		if (!csGraph.isComparisonDataInitialized()) {
			throw new IllegalStateException("Cornerstone graph is not initialized. Cannot build the visualization");
		}
		
		////////////////////////////////////////
		// Initialize the graph structure
		// Tree of activity itemsets
		// Bipartite EMD reallocation graph at the leafs
		////////////////////////////////////////
		DiffDecompGraph g = initGraphStructure();
		return g;
	}

	/**
	 * Initialize the pure graph structure.
	 * 
	 * Creates vertices and edges and, finally, a difference decomposition graph ({@link DiffDecompGraph}) instance 
	 * that only contains the raw graph structure (e.g., layout is not initialized).
	 * 
	 * @return DiffDecompGraph with raw graph structure.
	 */
	private DiffDecompGraph initGraphStructure() {
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
		
		// The graph (tree with bipartite EMD solution graph at the leafs) 
		final Graph<DDGVertex, DDGEdge> gDiffDecomp = 
				new SimpleDirectedGraph<>(DDGEdge.class);		// Empty graph

		// Map between
		// Vertex in cornerstone graph -> Vertex in decomposition graph for visualization
		Map<CSGraphVertex, DDGVertexSet> mapCSGVert2DDGVert = new HashMap<>();
		List<DDGVertexSet> roots = new LinkedList<>();
		//========================================
		// Add vertices (itemsets, traces) and coupling edges
		//========================================
		for(CSGraphVertex v : csGraph.getG().vertexSet()) {
			int id = idGenerator.next();
			DDGVertexSet ddgV = null;
			DDGVertexProbInfo probabilityInfo = this.getPropInfo4Vertex(v);
			//////////////////////////////
			// Set vertex
			//////////////////////////////
			if (v instanceof CSGraphVertexCS vCS) {
				ddgV = new DDGVertexSetCS(id, 	// Id
						probabilityInfo,
						DDGActivityBuilder.activitiesFrom(vCS.getHfddVertexRef().getVertexInfo(), 
								Optional.of(itemAbbreviator)), // Activity itemset
						getConditionVerticesForVertex(v), // Condition vertices (if required)
						vCS); // Vertex reference
			}
			else if(v instanceof CSGraphVertexSupport vSupport) {
				ddgV = new DDGVertexSetSupport(id, 	// Id
						probabilityInfo,
						DDGActivityBuilder.activitiesFrom(vSupport.getHfddVertexRef().getVertexInfo(), 
								Optional.of(itemAbbreviator)), // Activity itemset
						getConditionVerticesForVertex(v), // Condition vertices (if required)
						vSupport); // Vertex reference
			}
			else {
				throw new RuntimeException("Uncovered cornerstone graph vertex class");
			}
			// Add to graph
			gDiffDecomp.addVertex(ddgV);
			// Update Maps
			mapCSGVert2DDGVert.put(v, ddgV);
			
			//////////////////////////////
			// EMD Trace Vertices
			//////////////////////////////
			// Add Trace Vertices and edges for CORNERSTONE Vertices
			if (ddgV instanceof DDGVertexSetCS vCS) {
				this.addOptimalEMDSolutionSubgraph(gDiffDecomp, vCS, idGenerator);
			}
			////////////////////
			// Update root
			////////////////////
			if (csGraph.getG().inDegreeOf(v) == 0) {
				roots.add(ddgV);
			}
		}
		
		
		//////////////////////////////
		// Root the Graph
		//////////////////////////////
		DDGVertex root = null;
		// Check if we found a valid root
		if (roots.size() == 0) {
			throw new IllegalStateException("Cornerstone graph with " + mapCSGVert2DDGVert.size() + " vertices"
					+ " does not contain a proper root!");
		}
		else if (roots.size() == 1) {
			// Precisely one root
			root = roots.get(0);
		}
		else {
			// Multiple roots -> Add artificial root
			root = new DDGVertexArtificialRoot(idGenerator.next());
			gDiffDecomp.addVertex(root);
			
			// Add edges
			for (DDGVertexSet v : roots) {
				// Add edges (root, v)
				double probEdgeLeft;
				double probEdgeRight;
				switch (this.ddgType) {
					case CONDITIONED_RESIDUAL:
						probEdgeLeft = v.getProbabilityInfo().probCondLeft().get();
						probEdgeRight = v.getProbabilityInfo().probCondRight().get();
						break;
					case RESIDUAL:
						probEdgeLeft = v.getProbabilityInfo().probNonCondLeft();
						probEdgeRight = v.getProbabilityInfo().probNonCondRight();
						break;
					default:
						logger.error("Unknown DDG type. Not sure how to specifiy the probability of an hierarchy edge. "
								+ "Will default to base vertex probability!");
						probEdgeLeft = v.getProbabilityInfo().probNonCondLeft();
						probEdgeRight = v.getProbabilityInfo().probNonCondRight();
						break;
				}
				gDiffDecomp.addEdge(root, v, new DDGEdgeInterSet(getNextEdgeId(), probEdgeLeft, 
						probEdgeRight));
			}
		}

		//////////////////////////////
		// Inter-set Edges
		//////////////////////////////
		for(DefaultEdge e : csGraph.getG().edgeSet()) {
			
			CSGraphVertex source = csGraph.getG().getEdgeSource(e);
			CSGraphVertex target = csGraph.getG().getEdgeTarget(e);
			
			// Left Sankey edges
			// subset -> superset
			DDGVertexSet u = mapCSGVert2DDGVert.get(source);
			DDGVertexSet v = mapCSGVert2DDGVert.get(target);
			// Add edges (u, v)
			double probEdgeLeft;
			double probEdgeRight;
			switch (this.ddgType) {
				case CONDITIONED_RESIDUAL:
					probEdgeLeft = v.getProbabilityInfo().probCondLeft().get();
					probEdgeRight = v.getProbabilityInfo().probCondRight().get();
					break;
				case RESIDUAL:
					probEdgeLeft = v.getProbabilityInfo().probNonCondLeft();
					probEdgeRight = v.getProbabilityInfo().probNonCondRight();
					break;
				default:
					logger.error("Unknown DDG type. Not sure how to specifiy the probability of an hierarchy edge. "
							+ "Will default to base vertex probability!");
					probEdgeLeft = v.getProbabilityInfo().probNonCondLeft();
					probEdgeRight = v.getProbabilityInfo().probNonCondRight();
					break;
			}
			gDiffDecomp.addEdge(u, v, new DDGEdgeInterSet(getNextEdgeId(), probEdgeLeft, 
					probEdgeRight));
		}
		return new DiffDecompGraph(gDiffDecomp, root);
	}
	
	/**
	 * Extend the given graph by a bipartite optimal EMD flow for the provided 
	 * difference cornerstone vertex.  
	 * 
	 * @param gDiffDecomp Activity set difference decomposition graph 
	 * 	where the EMD solution will be added to. 
	 * 	<strong>Must</strong> already contain the provided vertex.
	 * @param vCS Cornerstone vertex for which the EMD solution will be added. 
	 * @param idGenerator Id generator that can be used to created new ids for the added vertex.
	 * @throws IllegalStateException If the graph does not contain the provided vertex.
	 */
	private void addOptimalEMDSolutionSubgraph(Graph<DDGVertex, DDGEdge> gDiffDecomp, 
			DDGVertexSetCS vDDGCS, Iterator<Integer> idGenerator) throws IllegalStateException {
	
		CSGraphVertexCS<BasicTraceCC> vCS = (CSGraphVertexCS<BasicTraceCC>) vDDGCS.getCsGraphVertex();
		ProbMassNonEmptyTrace probInfoRes = getResidualProbabilityVertex(vCS);
		// Only add an EMD subgraph is there is residual flow present
		// If child vertices (not necessarily cornerstone vertices) PARTITION the cases that contain the activities
		// of the provided activity set, there is nothing left to explain
		// -> Problem of current implementation: If one of the children is a support vertex for which its
		// child (conerstone) vertices do not partition the trace set, we will still not add any EMD flow.
		// However, we assume that it then makes more sense to promote this vertex to a cornerstone vertex, 
		// if the user still wants to see the differences.
		if (!probInfoRes.allZero()) {
			double probablityLeft = probInfoRes.left();
			double probablityRight = probInfoRes.right();
			
			////////////////////
			// Create and add EMD split vertex
			////////////////////
			// Vertex
			DDGVertexEMDSplit vEMDSplit = new DDGVertexEMDSplit(idGenerator.next(), 
					probablityLeft, probablityRight, vCS);
			// Add to graph
			gDiffDecomp.addVertex(vEMDSplit);
			// Add edge
			gDiffDecomp.addEdge(vDDGCS, vEMDSplit, new DDGEdgeInterSet(getNextEdgeId(), 
					probablityLeft, probablityRight));
			
			////////////////////////////////////////////////////////////
			// Add EMD Trace Variants and Edges
			////////////////////////////////////////////////////////////
			EMDSolContainer<BasicTraceCC> emdSol = null;
			switch (ddgType) {
			case CONDITIONED_RESIDUAL:
				emdSol = vCS.getMeasurement(CSMeasurementTypes.CONDITIONED_RESIDUAL).get().getEMDSolution().get();
				break;
			case RESIDUAL:
				emdSol = vCS.getMeasurement(CSMeasurementTypes.RESIDUAL).get().getEMDSolution().get();
				break;
			default:
				throw new RuntimeException("Unknown probability type for DDG. Cannot instantiate EMD.");
			}
			 

			//// Trace vertices 
			// Left
			DDGVertexTrace[] traceVerticesLeft = createVertices4StochLang(idGenerator, 
					emdSol.getLanguageLeft(), LogSide.LEFT, vCS);
			// Right
			DDGVertexTrace[] traceVerticesRight = createVertices4StochLang(idGenerator, 
					emdSol.getLanguageRight(), LogSide.RIGHT, vCS);
			// Add vertices to graph
			Arrays.stream(traceVerticesLeft).forEach(gDiffDecomp::addVertex);
			Arrays.stream(traceVerticesRight).forEach(gDiffDecomp::addVertex);
			// Add edges from flow splitter to left language
			Arrays.stream(traceVerticesLeft).forEach(v -> gDiffDecomp.addEdge(vEMDSplit, v,	
							new DDGEdgeSplitToEMD(getNextEdgeId(), v.getProbability())));
			////////////////////
			// Create and add coupling edges
			////////////////////
			double c;	// Edge cost (ground distance)
			for(Triple<Integer, Integer, Double> f : emdSol.getNonZeroFlows()) {
				c = emdSol.getCost(f.getLeft(), f.getMiddle());
				gDiffDecomp.addEdge(traceVerticesLeft[f.getLeft()], traceVerticesRight[f.getMiddle()], 
						new DDGEdgeEMD(getNextEdgeId(), f.getRight(), c));
				logger.trace(() -> ("Adding EMD edge: " + traceVerticesLeft[f.getLeft()] + 
						" --(" + f.getRight() + ")-- " + traceVerticesRight[f.getMiddle()]));
			}
			// Rank by probability mass
			// Do it now because the index is important before!
			this.rankProbabilities(traceVerticesLeft);
			this.rankProbabilities(traceVerticesRight);
		}
	}
	
	/**
	 * !!! Internally sorts the vertex array !!!
	 * @param vertices
	 */
	private void rankProbabilities(DDGVertexTrace[] vertices) {
		Arrays.sort(vertices, Comparator.comparing(DDGVertexTrace::getProbability).reversed());
		int rank = 0;
		for (DDGVertexTrace v : vertices) {
			v.setProbabilityRank(rank);
			rank++;
		}
	}
	
	/**
	 * Create trace vertices for every trace in the EMD LP solution.
	 * !!! Indices of returned traces correspond to edge indices stored in the non-zero flow edges
	 * @param idGenerator
	 * @param lang
	 * @param logSide
	 * @param vCS
	 * @return
	 */
	private<F extends TraceDescriptor> DDGVertexTrace[] createVertices4StochLang(Iterator<Integer> idGenerator, 
			OrderedStochasticLanguage<F> lang, LogSide logSide, CSGraphVertexCS<F> vCS) {
		
		// Vertex array
		DDGVertexTrace[] vertices = new DDGVertexTrace[lang.getNumberOfTraceVariants()];
		// Iterate over stochastic language
		StochasticLanguageIterator<F> it = lang.iterator();
		int i = 0;
		while(it.hasNext()) {
			F descBase = it.next();
			double p = it.getProbability();
		
			////////////////////
			// Convert trace descriptor to list of activities
			////////////////////
			DDGActivity[] activities = new DDGActivity[descBase.length()];
			for(int j = 0; j < descBase.length(); j++) {
				// For now, we only assign categories if a categorical descriptor was used
				// -> we do not build a categorical mapping if not present before
				activities[j] = DDGActivityBuilder.activityFrom(descBase, j,
						Optional.of(itemAbbreviator));
			}

			vertices[i] = new DDGVertexTrace(idGenerator.next(), p, logSide, 
					Arrays.asList(activities), vCS);
			i++;
		}
		

		return vertices;
	}
	
	private Optional<Collection<DDGActivity>> getConditionVerticesForVertex(CSGraphVertex v) {
		
		if (this.ddgType == DDGGraphType.CONDITIONED_RESIDUAL) {
			Optional<ArrayList<Set<VertexCondition>>> conditionBase = csGraph.getConditionBase();
			Optional<BitSet> conditionActivities = 
							VertexConditioningLogTransformer.getConditionActivities(
									conditionBase, v.getHfddVertexRef());
			if (conditionActivities.isPresent()) {
				CategoryMapper catMap = v.getHfddVertexRef().getVertexInfo().getCategoryMapper();
				
				Set<DDGActivity> condDDGActivities = 
						conditionActivities.get().stream()
							.mapToObj(
									c -> DDGActivityBuilder.activityFrom(c, catMap, 
											Optional.of(this.itemAbbreviator)))
							.collect(Collectors.toSet()); // collect into set
				return Optional.of(condDDGActivities);
			}
			else {
				return Optional.empty();
			}
		}
		else {
			return Optional.empty();
		}
		
	}
	
	private ProbMassNonEmptyTrace getProbabilityVertex(CSGraphVertex vCS) {
		switch (this.ddgType) {
			case CONDITIONED_RESIDUAL:
				ProbMassNonEmptyTrace probInfo = vCS.getProbabilityMassInfo(CSMeasurementTypes.CONDITIONED);
				if (probInfo == null) {
					logger.error("No conditional probability information present " + vCS.getHfddVertexRef().getId());
					return new ProbMassNonEmptyTrace(0, 0, true);
				}
				else {
					return probInfo;
				}
			case RESIDUAL:
				double probLeft = vCS.getHfddVertexRef().getVertexInfo().getProbabilityLeft();
				double probRight = vCS.getHfddVertexRef().getVertexInfo().getProbabilityRight();
				return new ProbMassNonEmptyTrace(probLeft, probRight, 
						(Double.compare(probLeft, 0) == 0) && (Double.compare(probRight, 0) == 0));
			default:
				logger.warn("Unkown DDG type: " + this.ddgType + 
						". Don't know how to get probabilities for this type.");
				return new ProbMassNonEmptyTrace(0, 0, true);
		}
	}

	private ProbMassNonEmptyTrace getResidualProbabilityVertex(CSGraphVertex vCS) {
		switch (this.ddgType) {
			case CONDITIONED_RESIDUAL:
				ProbMassNonEmptyTrace probInfo = vCS.getProbabilityMassInfo(CSMeasurementTypes.CONDITIONED_RESIDUAL);
				if (probInfo == null) {
					logger.error("No conditional residual probability information present " + vCS.getHfddVertexRef().getId());
					return null;
				}
				else {
					return probInfo;
				}
			case RESIDUAL:
				ProbMassNonEmptyTrace probInfoRes = vCS.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL);
				if (probInfoRes == null) {
					logger.error("No residual probability information present " + vCS.getHfddVertexRef().getId());
					return new ProbMassNonEmptyTrace(0, 0, true);
				}
				else {
					return probInfoRes;
				}
			default:
				logger.warn("Unkown DDG type: " + this.ddgType + 
						". Don't know how to get residual probabilities for this type.");
				return new ProbMassNonEmptyTrace(0, 0, true);
		}
	}
	
	private DDGVertexProbInfo getPropInfo4Vertex(CSGraphVertex vCS) {///////////////
		//////////////////////////////
		// Conditioned Probabilities
		//////////////////////////////
		// Base
		double probLeftNonCond = vCS.getHfddVertexRef().getVertexInfo().getProbabilityLeft();
		double probRightNonCond = vCS.getHfddVertexRef().getVertexInfo().getProbabilityRight();
		//Residual
		ProbMassNonEmptyTrace probInfoRes = vCS.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL);
		if (probInfoRes == null) {
			throw new RuntimeException("Could not instantiate DDGVertex probability: " +
				"No residual probability information present " + vCS.getHfddVertexRef().getId());
		}
		//////////////////////////////
		// Non-conditioned Probabilities
		//////////////////////////////
		// Base
		ProbMassNonEmptyTrace probInfoCond = vCS.getProbabilityMassInfo(CSMeasurementTypes.CONDITIONED);
		Optional<Double> probCondLeft;
		Optional<Double> probCondRight;
		if (probInfoCond == null) {
			probCondLeft = Optional.empty();
			probCondRight = Optional.empty();
		}
		else {
			probCondLeft = Optional.of(probInfoCond.left());
			probCondRight = Optional.of(probInfoCond.right());
		}
		// Residual
		ProbMassNonEmptyTrace probInfoCondRes = vCS.getProbabilityMassInfo(CSMeasurementTypes.CONDITIONED_RESIDUAL);
		Optional<Double> probCondResLeft;
		Optional<Double> probCondResRight;
		if (probInfoCondRes == null) {
			probCondResLeft = Optional.empty();
			probCondResRight = Optional.empty();
		}
		else {
			probCondResLeft = Optional.of(probInfoCondRes.left());
			probCondResRight = Optional.of(probInfoCondRes.right());
		}
		
		return new DDGVertexProbInfo(
				probLeftNonCond, 
				probRightNonCond, 
				probInfoRes.left(), 
				probInfoRes.right(), 
				probCondLeft,
				probCondRight,
				probCondResLeft,
				probCondResRight);
		
	}

	////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters
	////////////////////////////////////////////////////////////////////////////////
	
	private int getNextEdgeId() {
		edgeId++;
		return edgeId;
	}

	public Comparator<String> getItemsetSorter() {
		return itemsetSorter;
	}

	public DiffDecompGraphBuilder setItemsetSorter(Comparator<String> itemsetSorter) {
		this.itemsetSorter = itemsetSorter;
		return this;
	}

	public Function<String, String> getItemAbbreviator() {
		return itemAbbreviator;
	}

	public DiffDecompGraphBuilder setItemAbbreviator(Function<String, String> itemAbbreviator) {
		this.itemAbbreviator = itemAbbreviator;
		return this;
	}

	public CSGraph getCsGraph() {
		return csGraph;
	}

	public DiffDecompGraphBuilder setCsGraph(CSGraph csGraph) {
		this.csGraph = csGraph;
		return this;
	}

	public DiffDecompGraphBuilder setGraphProbabilityType(DDGGraphType ddgType) {
		this.ddgType = ddgType;
		return this;
	}

	public DDGGraphType getGraphProbabilityType() {
		return this.ddgType;
	}
}
