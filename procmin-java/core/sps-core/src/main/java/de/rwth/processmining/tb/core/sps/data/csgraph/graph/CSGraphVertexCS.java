package de.rwth.processmining.tb.core.sps.data.csgraph.graph;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;

import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.StochasticLanguageIterator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexCondition;
import de.rwth.processmining.tb.core.sps.data.csgraph.CSMeasurementTypes;
import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey.CSSkEdge;
import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey.CSSkFlowSplit;
import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey.CSSkItemsetVertex;
import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey.CSSkTraceVertex;
import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey.CSSkVertex;
import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey.EdgeType;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurementEMDSol;

public final class CSGraphVertexCS<F extends TraceDescriptor> extends CSGraphVertex {
	private static final Logger logger = LogManager.getLogger(CSGraphVertexCS.class);

	/**
	 * Mapping measurement types to the measurement data.
	 */
	private final EnumMap<CSMeasurementTypes, HFDDMeasurementEMDSol<F>> measurements;
	
	private Optional<Set<VertexCondition>> vertexConditions;
	
	public CSGraphVertexCS(HFDDVertex hfddVertexRef) {
		super(hfddVertexRef);
		this.measurements = new EnumMap<>(CSMeasurementTypes.class);
		this.vertexConditions = Optional.empty();
	}
	
	@Override
	public Pair<? extends CSSkVertex, ? extends CSSkVertex> createAndAddSankeySubgraphs(Graph<CSSkVertex, CSSkEdge> g,
			Iterator<Integer> idGenerator) {
		double probabilityMassLeft = this.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL).left();
		double probabilityMassRight = this.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL).right();
		// Left itemset Sankey vertex
		CSSkItemsetVertex vertexItemsetLeft = new CSSkItemsetVertex(idGenerator.next(), true, this);
		// Right itemset Sankey vertex
		CSSkItemsetVertex vertexItemsetRight = new CSSkItemsetVertex(idGenerator.next(), false, this);
		// Add itemset vertices to Sankey diagram graph
		g.addVertex(vertexItemsetLeft);
		g.addVertex(vertexItemsetRight);
		
		//================================================================================
		// Create the Sankey flow for the EMD
		//================================================================================
		if (!this.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL).allZero()) {
			//========================================
			// Flow splitter vertices
			//========================================
			// Create flow splitter vertices
			CSSkFlowSplit vSplitLeft = new CSSkFlowSplit(idGenerator.next(), true, this);
			CSSkFlowSplit vSplitRight = new CSSkFlowSplit(idGenerator.next(), false, this);
			// Add flow splitters to graph
			g.addVertex(vSplitLeft);
			g.addVertex(vSplitRight);
			// Add flow splitter edges
			g.addEdge(vertexItemsetLeft, vSplitLeft, new CSSkEdge(probabilityMassLeft, 0, EdgeType.INTERSET)); // item -> split
			g.addEdge(vSplitRight, vertexItemsetRight, new CSSkEdge(probabilityMassRight, 0, EdgeType.INTERSET)); // split -> itemset
			//========================================
			// EMD trace vertices
			//========================================
			EMDSolContainer<F> emdSol = measurements.get(CSMeasurementTypes.RESIDUAL).getEMDSolution().get();
			// Create trace vertices for left language
			CSSkTraceVertex[] skVerticesLeft = createVertices4StochLang(idGenerator, emdSol.getLanguageLeft(), true);
			CSSkTraceVertex[] skVerticesRight = createVertices4StochLang(idGenerator, emdSol.getLanguageRight(), false);
			// Add vertices to graph
			Arrays.stream(skVerticesLeft).forEach(g::addVertex);
			Arrays.stream(skVerticesRight).forEach(g::addVertex);
			// Add edges from flow splitter
			Arrays.stream(skVerticesLeft).forEach(v -> g.addEdge(vSplitLeft, v,				// flow splitter -> trace descriptor
							new CSSkEdge(v.getProbabilityMass(), 0, EdgeType.INTERSET)));
			Arrays.stream(skVerticesRight).forEach(v -> g.addEdge(v, vSplitRight, 			// trace descriptor -> splitter
							new CSSkEdge(v.getProbabilityMass(), 0, EdgeType.INTERSET)));
			// Create and add coupling edges
			double c;
			for(Triple<Integer, Integer, Double> f : emdSol.getNonZeroFlows()) {
				c = emdSol.getCost(f.getLeft(), f.getMiddle());
				g.addEdge(skVerticesLeft[f.getLeft()], skVerticesRight[f.getMiddle()], 
						new CSSkEdge(f.getRight(), c, EdgeType.COUPLING));
				logger.trace(() -> ("Adding EMD edge: " + skVerticesLeft[f.getLeft()] + 
						" --(" + f.getRight() + ")-- " + skVerticesRight[f.getMiddle()]));
			}
		}
		return Pair.of(vertexItemsetLeft, vertexItemsetRight);
	}
	
	/**
	 * Create the trace Sankey vertices for elements of a stochastic language.
	 * @param idGenerator Id generator for the vertex ids.
	 * @param lang Stochastic language
	 * @param isLeft Left log or right log
	 * @return Array of created vertices in the order that they occur in 
	 * 	the ordered stochastic language
	 */
	private CSSkTraceVertex[] createVertices4StochLang(Iterator<Integer> idGenerator, OrderedStochasticLanguage<F> lang, boolean isLeft) {
		// Vertex array
		CSSkTraceVertex[] vertices = new CSSkTraceVertex[lang.getNumberOfTraceVariants()];
		int i = 0;
		// Iterate over stochastic language
		StochasticLanguageIterator<F> it = lang.iterator();
		while(it.hasNext()) {
			F descBase = it.next();
			double p = it.getProbability();
			
			// Array of trace element's string representation
			String[] strDescriptor = new String[descBase.length()];
			for(int j = 0; j < descBase.length(); j++) {
				strDescriptor[j] = descBase.toString(j);
			}
			CSSkTraceVertex v = new CSSkTraceVertex(idGenerator.next(), isLeft, p, this, strDescriptor);
			vertices[i] = v;
			i++;
			logger.trace(() -> ("Created trace vertex: " + v.toString()));
		}
		return vertices;
	}
	
	public void setMeasurement(CSMeasurementTypes type, HFDDMeasurementEMDSol<F> measurement) {
		this.measurements.put(type, measurement);
	}

	public Optional<HFDDMeasurementEMDSol<F>> getMeasurement(CSMeasurementTypes type) {
		HFDDMeasurementEMDSol<F> res = this.measurements.get(type);
		if (res == null) {
			return Optional.empty();
		}
		else {
			return Optional.of(res);
		}
	}
}
