package de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationException;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagement;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexCondition;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexConditionType;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraph;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurement;

public class InterestingVertexFinder {
	private static final Logger logger = LogManager.getLogger(InterestingVertexFinder.class);
	
	/**
	 * Handle to the iteration management.
	 */
	protected final HFDDIterationManagement hfddItMan;
	
	/**
	 * Handle to the HFDDGraph
	 */
	protected final HFDDGraph hfddGraph;
	
	/**
	 * Handle to the underlying real graph structure.
	 */
	protected final Graph<HFDDVertex, DefaultEdge> g;
	
	/**
	 * Iteration to find the interesting vertices for
	 */
	protected final int iteration;
	
	// Added as member variable so that I can query it.
	/**
	 * Classify the subprocess structure instance (SPI) "under" this vertex.
	 * ("under" -> all less specific subprocesses)
	 */
	protected SubSPIType[] subSPITypes;
	
	/**
	 * Save condition type per vertex
	 * 0 initialization -> 0 standing for unconditioned
	 * Stores aggregated conditional contexts
	 * For example, (1 << 1) + (1 << 3).
	 */
	protected long[] vertexCondTypes;

	/**
	 * Number of vertices in the graph
	 */
	protected int nbrVertices;

	/**
	 * EMD threshod for vertex to be considered interesting.
	 */
	protected double tMetric;

	/**
	 * Factor by which a vertex must be interesting w.r.t. its predecessors.
	 *
	 * If a vertex has a very high EMD compared to its predecessors, it is interesting.
	 */
	protected  double tSurprise;

	/**
	 * Threshold when a vertex is considered to dominated "predecessor" differences.
	 */
	protected double tDom;

	public InterestingVertexFinder(HFDDIterationManagement hfddItMan, int iteration,
			double tMetric, double tSurprise, double tDom) {
		this.hfddItMan = hfddItMan;
		this.hfddGraph = hfddItMan.getHfddGraph();
		this.g = hfddGraph.getGraph();
		this.iteration = iteration;
		this.nbrVertices = hfddGraph.getGraph().vertexSet().size();

		this.tMetric = tMetric;
		this.tSurprise = tSurprise;
		this.tDom = tDom;
	}

	public Collection<DiffCandidate> findInterestingVertices() {
		PerspectiveDescriptor pDesc = 
				hfddItMan.getPerspective4Iteration(iteration);
		
		////////////////////////////////////////
		// Compute Conditioning Information
		////////////////////////////////////////
		Optional<ArrayList<Set<VertexCondition>>> aggCondDataCBase = Optional.empty();
		try {
			aggCondDataCBase = hfddItMan.getIteration(iteration).getAggDataCBase(false);
		} catch (HFDDIterationException e) {
			e.printStackTrace();
		}
		
		if (true) {
			// Each condition vertex is associated with a number (1 << offset)
			Map<HFDDVertex, Integer> offsets = new HashMap<>(3 * iteration);
			int nextOffset = 0;
			this.vertexCondTypes = new long[nbrVertices];
			
			// TODO !If I don't distinguish the context, I should also change the code later!
			if (aggCondDataCBase.isPresent()) {
				for (int i = 0; i < nbrVertices; i++) {
					Set<VertexCondition> s = aggCondDataCBase.get().get(i);
					// There are conditions for the vertex
					if (s != null) {
						for (VertexCondition c : s) {
							// Only consider probabilistic conditioning
							if (c.type() == VertexConditionType.PROBCOND) {
								Integer offset = offsets.get(c.condVertex());
								// No offset associated with this condition vertex yet
								if (offset == null) {
									offset = nextOffset;
									offsets.put(c.condVertex(), offset);
									nextOffset++;
								}
								// Add to conditions for i
								vertexCondTypes[i] += (1 << offset);
							}
						}
					}
				}
			}
		}
		
		////////////////////////////////////////
		// Compute Interesting Vertices
		////////////////////////////////////////
		Collection<HFDDVertex> interestingVertices = this.findInterestingVertices(
				aggCondDataCBase);
		if (logger.getLevel() == Level.TRACE) {
      logger.trace("Interesting Vertices:");
      for (HFDDVertex v : interestingVertices) {
        HFDDMeasurement measurementV = v.getVertexInfo().getMeasurements().get(pDesc);
        logger.trace(v.getId() + " - " +
            Arrays.toString(v.getVertexInfo().getItemsetHumanReadable()) + 
              ": " + measurementV.getMetric().get());
      }
		}

		final Optional<ArrayList<Set<VertexCondition>>> aggCondDataCBaseCopy = aggCondDataCBase;
		return interestingVertices.stream().map(u -> new DiffCandidate(u, getConditionalContext(u, 
				aggCondDataCBaseCopy))).toList();
	}
	
	public Collection<HFDDVertex> findInterestingVertices(
			Optional<ArrayList<Set<VertexCondition>>> aggCondDataCBase) {

		int nbrVertices = g.vertexSet().size();

		PerspectiveDescriptor pDesc = 
				hfddItMan.getPerspective4Iteration(iteration);
		
		// Classify the subprocess structure instance (SPI) "under" this vertex 
		// ("under" -> all less specific subprocesses)
		this.subSPITypes = new SubSPIType[nbrVertices];

		////////////////////////////////////////
		// Setup Pass
		// Determine Vertices to be Ignored
		////////////////////////////////////////
		this.setupPreFilterPhase(subSPITypes, aggCondDataCBase);

		////////////////////////////////////////
		// First Pass - Initialize Data
		////////////////////////////////////////
		this.forwardPassSPITypes(pDesc, subSPITypes);
		

		////////////////////////////////////////
		// Second Pass - Collection Interesting Vertices
		////////////////////////////////////////
		Collection<HFDDVertex> interestingVertices = this.backwardIVertexCollection(pDesc);

		
		return interestingVertices;	
	}
	
	protected SubSPIType getTypeBasedOnPredecessors(HFDDVertex v, boolean isVBelowT, List<HFDDVertex> subIncPredecessors) {
		PerspectiveDescriptor pDesc = 
					hfddItMan.getPerspective4Iteration(iteration);
		final HFDDMeasurement measurementV = v.getVertexInfo().getMeasurements().get(pDesc);
		
		
		// Remove ignored vertices
		subIncPredecessors = subIncPredecessors.stream().filter(
				u -> subSPITypes[u.getId()] != SubSPIType.IGNORE).toList();

		// Base case
		if (subIncPredecessors.size() == 0) {
			// NO predecessor
			if (isVBelowT) {
				return SubSPIType.PURE_UNINTERESTING;
			}
			else {
				return SubSPIType.INTERESTING;
			}
		}
		else {
			// Count sub-SPI types 
			Map<SubSPIType, Long> subSPICounts = subIncPredecessors.stream().map(u -> subSPITypes[u.getId()])
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		
			// All interesting
			if (subSPICounts.getOrDefault(SubSPIType.INTERESTING, -1L) == subIncPredecessors.size()) {
				if (isVBelowT) {
					return SubSPIType.SUB_INTERESTING;
				}
				else {
					return SubSPIType.INTERESTING;
				}
			}
			// All uninteresting
			else if (subSPICounts.getOrDefault(SubSPIType.PURE_UNINTERESTING, -1L) == subIncPredecessors.size()) {
				if (isVBelowT) {
					return SubSPIType.PURE_UNINTERESTING;
				}
				else {
					return SubSPIType.INTERESTING;
				}
			}
			// Mix of interesting and uninteresting
			else if (subSPICounts.getOrDefault(SubSPIType.INTERESTING, 0L) + 
					subSPICounts.getOrDefault(SubSPIType.PURE_UNINTERESTING, -0L) == subIncPredecessors.size()) {
				OptionalDouble maxPredMetric = subIncPredecessors.stream().mapToDouble(
						u -> u.getVertexInfo().getMeasurements().get(pDesc).getMetric().orElse(0.0)).max();

				if (isVBelowT) {
					return SubSPIType.SUB_INTERESTING;
				}
				else {
					if (measurementV.getMetric().orElse(-1.0) > tSurprise * maxPredMetric.orElse(0.0)) {
						return SubSPIType.INTERESTING;
					}
					else {
						return SubSPIType.SUB_INTERESTING;
					}
				}
			}
			else {
				return SubSPIType.SUB_INTERESTING;
			}
		}
	}

	private Optional<Collection<HFDDVertex>> getConditionalContext(HFDDVertex v, 
			Optional<ArrayList<Set<VertexCondition>>> aggCondDataCBase) {
		if (aggCondDataCBase.isPresent()) {
			Set<VertexCondition> s = aggCondDataCBase.get().get(v.getId());
			if (s == null) {
				return Optional.empty();
			}
			else {
				Set<HFDDVertex> conditionsV = 
						s.stream()
							.filter(c -> c.type() == VertexConditionType.PROBCOND)
							.map(VertexCondition::condVertex)
							.collect(Collectors.toSet());
				if (conditionsV.size() > 0) {
					return Optional.of(conditionsV);
				}
				else {
					return Optional.empty();
				}
			}
		}
		else {
			return Optional.empty();
		}
	}

	public SubSPIType[] getLastRunSPITypes() {
		return this.subSPITypes;
	}
	
	public long[] getLastRoundConditionTypes() {
		return this.vertexCondTypes;
	}

	public Collection<HFDDVertex> findInterestingVerticesDistinguish(
			double metricThreshold,
			final double threshSurprise,
			double threshDom, 
			Optional<ArrayList<Set<VertexCondition>>> aggCondDataCBase,
			long[] vertexCondTypes) {

		int nbrVertices = g.vertexSet().size();

		PerspectiveDescriptor pDesc = 
				hfddItMan.getPerspective4Iteration(iteration);
		
		// Classify the subprocess structure instance (SPI) "under" this vertex 
		// ("under" -> all less specific subprocesses)
		this.subSPITypes = new SubSPIType[nbrVertices];

		////////////////////////////////////////
		// First Pass - Initialize Data
		////////////////////////////////////////
		// Bottom up - HFDD graph traversal
		GraphIterator<HFDDVertex, DefaultEdge> itGraph = new TopologicalOrderIterator<>(g);
		
		while (itGraph.hasNext()) {
			HFDDVertex v = itGraph.next();

			HFDDMeasurement measurementV = v.getVertexInfo().getMeasurements().get(pDesc);
			
			// Ignore the condition vertex itself
			// It will always be uninteresting
			// This can be problematic for its successors.
			// They will always have one uninteresting predecessor. Moreover, 
			// they might not meet the surprise threshold since the other predecessors
			// were measured in a different context resulting in significantly higher scores
			if (aggCondDataCBase.isPresent()) {
				Set<VertexCondition> condV = aggCondDataCBase.get().get(v.getId());
				if (Objects.nonNull(condV)) {
					if (condV.contains(v)) {
						subSPITypes[v.getId()] = SubSPIType.IGNORE;
						continue;
					}
				}
			}
			
			// Is below threshold?
			boolean isVBelowT = measurementV.getMetric().isEmpty() || measurementV.getMetric().get() < metricThreshold;

			List<HFDDVertex> subIncPredecessors = Graphs.predecessorListOf(g, v);

			// Split non-ignored predecessors into equal and different conditional background
			final long condBackGroundV = vertexCondTypes[v.getId()];
			Map<Boolean, List<HFDDVertex>> partitionSameCondPredec = 
					subIncPredecessors.stream()
					.collect(Collectors.partitioningBy(
							u -> vertexCondTypes[u.getId()] == condBackGroundV));
			
			//////////////////////////////
			// Base Cases
			//////////////////////////////
			if (partitionSameCondPredec.get(true).isEmpty() && partitionSameCondPredec.get(false).isEmpty()) {
				// No predecessor at all
				if (isVBelowT) {
					subSPITypes[v.getId()] = SubSPIType.PURE_UNINTERESTING;
				}
				else {
					subSPITypes[v.getId()] = SubSPIType.INTERESTING;
				}
			}
			else if (vertexCondTypes[v.getId()] > 0 && 
					partitionSameCondPredec.get(true).stream().allMatch(u -> subSPITypes[u.getId()] == SubSPIType.IGNORE)) {
				// Consider refined vertices only;
				// If we have subtrees that are not direct successors of the conditioning vertex (but always co-occurr),
				// we must be careful when considering them interesting.
				// since they have no predecessor, they quickly become interesting even if their un-conditioned predecessors
				// already suggest that they contain activities of mixed interest
				// In this case, we try to be careful and ignore them.
				// Subsequently, we also want to ignore their successors, 
				// if we do not get any new information from the refined tree 
				// (i.e., all predecessors of the same condition type are ignored)
				subSPITypes[v.getId()] = SubSPIType.IGNORE;
			}
			else if (partitionSameCondPredec.get(true).isEmpty()){
				// No predecessor of same conditional background
				// No predecessor at all
				if (isVBelowT) {
					subSPITypes[v.getId()] = SubSPIType.PURE_UNINTERESTING;
				}
				else {
					SubSPIType subSPITypeV = this.getTypeBasedOnPredecessors(v, isVBelowT, 
							partitionSameCondPredec.get(false));
					if (subSPITypeV == SubSPIType.SUB_INTERESTING) {
						subSPITypeV = SubSPIType.IGNORE;
					}
					subSPITypes[v.getId()] = SubSPIType.INTERESTING;
				}
			}
			else {
				// There are predecessors of the same conditioning type and not all of them are ignored
				subSPITypes[v.getId()] = this.getTypeBasedOnPredecessors(v, isVBelowT, 
							partitionSameCondPredec.get(true));
			}
			
		}
	
		////////////////////////////////////////
		// Low-level Testing
		////////////////////////////////////////
		if (logger.getLevel()  == Level.DEBUG) {
			for (HFDDVertex v : g.vertexSet()) {
				HFDDMeasurement measurementV = v.getVertexInfo().getMeasurements().get(pDesc);
				logger.debug(v.getId() + " - " + 
					Arrays.toString(v.getVertexInfo().getItemsetHumanReadable()) + 
						"[" + vertexCondTypes[v.getId()] + "]: " + 
						subSPITypes[v.getId()] + " -> " + measurementV.getMetric().get());	
			}
		}

		EdgeReversedGraph<HFDDVertex, DefaultEdge> gBack = new EdgeReversedGraph<HFDDVertex, DefaultEdge>(g);
		itGraph = new TopologicalOrderIterator<>(gBack);
		
		// Interesting
		// A vertex is interesting if it is a maximal difference representative
		// 1) Either all or non of its subprocesses are difference representatives,
		// 2) There is no super-process with the same conditional background
		// 		of this vertex with metric > fwdPropagation * this.metric and that satisfies 1).
		Collection<HFDDVertex> interestingVertices = new LinkedList<>();
		// Vertices that are dominated by a vertex from above's collection
		// -> reflexiv.
		BitSet isInterestingOrDominated = new BitSet(nbrVertices);
		
		while (itGraph.hasNext()) {
			HFDDVertex v = itGraph.next();
			HFDDMeasurement measurementV = v.getVertexInfo().getMeasurements().get(pDesc);
			final long condBackGroundV = vertexCondTypes[v.getId()];

			
			// if there is a super-process that dominates v
			// and has sufficient metric score
			boolean hasDomSuperProc = Graphs.predecessorListOf(gBack, v).stream()
					.filter(u -> vertexCondTypes[u.getId()] == condBackGroundV)
					.anyMatch(
						u -> (isInterestingOrDominated.get(u.getId()) 
								&& u.getVertexInfo().getMeasurements().get(pDesc).getMetric().get() > 
						threshDom * measurementV.getMetric().get()));
			
			if (hasDomSuperProc) {
				isInterestingOrDominated.set(v.getId());
			}
			else if (subSPITypes[v.getId()] == SubSPIType.INTERESTING) {
				interestingVertices.add(v);
				isInterestingOrDominated.set(v.getId());
			}
		}
		
		return interestingVertices;	
	}

	/**
	* Initialize the vertex types based on the individual vertex' information.
	*
	* This implementation will set the status 'ignored' as follows: 
	* Ignore the condition vertex itself as well as its predecessors that always occur with it
	*
	* @param subSPITypes Array holding the vertices' types.
	* @param aggCondDataCBase The aggregated data-induced co-occurrence  
	*/
	protected void setupPreFilterPhase(SubSPIType[] subSPITypes, Optional<ArrayList<Set<VertexCondition>>> aggCondDataCBase) {
		////////////////////////////////////////
		// Setup Pass
		// Determine Vertices to be Ignored
		////////////////////////////////////////
		// Ignore the condition vertex itself as well as its predecessors that always occur with it
		// They will always be uninteresting after conditioning.
		// This can be problematic for their successors.
		// They will always have one uninteresting predecessor. Moreover, 
		// they might not meet the surprise threshold since the other predecessors
		// were measured in a different context resulting in significantly higher scores
		if (aggCondDataCBase.isPresent()) {
			Set<HFDDVertex> conditionVertices = 
					hfddItMan.getConditionVertices(this.iteration, VertexConditionType.PROBCOND);
			for (HFDDVertex vCond : conditionVertices) {
				// Breadth first backwards iteration
				// CONTINUE as long as the super-process is still co-occurring
				LinkedList<HFDDVertex> queue = new LinkedList<>();
				queue.add(vCond);
				while (queue.size() > 0) {
					HFDDVertex v = queue.poll();
					Set<VertexCondition> condV = aggCondDataCBase.get().get(v.getId());
					if (Objects.nonNull(condV)) {
						if (condV.stream()
								.filter(c -> c.type() == VertexConditionType.PROBCOND) 
								.map(VertexCondition::condVertex)
								.anyMatch(v2 -> Objects.equals(vCond, v2))) {
							subSPITypes[v.getId()] = SubSPIType.IGNORE;
							// If a vertex has multiple condition types, we should enqueue it multiple times 
							// (The test always refers to a different condition vertex)
							queue.addAll(Graphs.predecessorListOf(g, v));
						}
					}
				}
			}
		}
	}

	/**
	 * Performs a forward pass on the graph and determines the
	 * type of each vertex.
	 */
	protected void forwardPassSPITypes(PerspectiveDescriptor pDesc, SubSPIType[] subSPITypes) {
		// Bottom up - HFDD graph traversal
		GraphIterator<HFDDVertex, DefaultEdge> itGraph = new TopologicalOrderIterator<>(g);
		
		while (itGraph.hasNext()) {
			final HFDDVertex v = itGraph.next();

			HFDDMeasurement measurementV = v.getVertexInfo().getMeasurements().get(pDesc);
			
			// Skip ignore vertices
			if (Objects.equals(subSPITypes[v.getId()], SubSPIType.IGNORE)) {
				continue;
			}
			
			// Is below threshold?
			boolean isVBelowT = measurementV.getMetric().isEmpty() || measurementV.getMetric().get() < tMetric;

			List<HFDDVertex> subIncPredecessors = Graphs.predecessorListOf(g, v);
			
			//////////////////////////////
			// Base Cases
			//////////////////////////////
			if (subIncPredecessors.isEmpty()) {
				// No predecessor at all
				if (isVBelowT) {
					subSPITypes[v.getId()] = SubSPIType.PURE_UNINTERESTING;
				}
				else {
					subSPITypes[v.getId()] = SubSPIType.INTERESTING;
				}
			}
			else {
				// There are predecessors of the same conditioning type and not all of them are ignored
				subSPITypes[v.getId()] = this.getTypeBasedOnPredecessors(v, isVBelowT, 
							subIncPredecessors);
			}
		}
	}

	/**
	* Performs a topological sorted backward on the graph and 
	* collects the interesting vertices.
	*/
	protected Collection<HFDDVertex> backwardIVertexCollection(PerspectiveDescriptor pDesc) {
		// Topological backward iteration
		EdgeReversedGraph<HFDDVertex, DefaultEdge> gBack = new EdgeReversedGraph<HFDDVertex, DefaultEdge>(g);
		GraphIterator<HFDDVertex, DefaultEdge> itGraph = new TopologicalOrderIterator<>(g);
		itGraph = new TopologicalOrderIterator<>(gBack);
		
		// Interesting
		// A vertex is interesting if it is a maximal difference representative
		// 1) Either all or non of its subprocesses are difference representatives,
		// 2) There is no super-process with the same conditional background
		// 		of this vertex with metric > fwdPropagation * this.metric and that satisfies 1).
		Collection<HFDDVertex> interestingVertices = new LinkedList<>();
		// Vertices that are dominated by a vertex from above's collection
		// -> reflexiv.
		BitSet isInterestingOrDominated = new BitSet(nbrVertices);
		
		while (itGraph.hasNext()) {
			HFDDVertex v = itGraph.next();
			HFDDMeasurement measurementV = v.getVertexInfo().getMeasurements().get(pDesc);
			//final long condBackGroundV = vertexCondTypes[v.getId()];

			// if there is a super-process that dominates v
			// and has sufficient metric score
			// In original graph edge (v, u)
			// V dominated by u if (u interesting or dominated, threshold * v.metric < u.metric)
			boolean hasDomSuperProc = Graphs.predecessorListOf(gBack, v).stream()
					//.filter(u -> vertexCondTypes[u.getId()] == condBackGroundV)
					.anyMatch(
						u -> (isInterestingOrDominated.get(u.getId()) 
								&& u.getVertexInfo().getMeasurements().get(pDesc).getMetric().get() > 
						tDom * measurementV.getMetric().get()));
			
			if (hasDomSuperProc) {
				isInterestingOrDominated.set(v.getId());
			}
			else if (subSPITypes[v.getId()] == SubSPIType.INTERESTING) {
				interestingVertices.add(v);
				isInterestingOrDominated.set(v.getId());
			}
		}
		
		return interestingVertices;	
	}
	
}
