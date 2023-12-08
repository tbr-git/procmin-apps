package de.rwth.processmining.tb.core.sps.data.csgraph.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.Graph;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.FeatureExtractorDistancePairVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.controlflow.LevenshteinCCStateful;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased.TraceAsFeatureExtractor;
import de.rwth.processmining.tb.core.sps.algorithm.measure.CSVertexConditionedMeasurer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.CSVertexConditionedResidualMeasurer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.CSVertexMeasurer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.CSVertexResidualMeasurer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexCondition;
import de.rwth.processmining.tb.core.util.backgroundwork.CachedBackgroundTaskService;

public class CSGraph {
	private static final Logger logger = LogManager.getLogger(CSGraph.class);
	
	private final UUID uuid;
	/**
	 * Handle to the JGraphtT graph.
	 */
	private final Graph<CSGraphVertex, DefaultEdge> g;
	
	private boolean isComparisonDataInitialized;
	
	private final Optional<ArrayList<Set<VertexCondition>>> conditionBase;
	
	protected CSGraph(Graph<CSGraphVertex, DefaultEdge> graphStructure, 
			Optional<ArrayList<Set<VertexCondition>>> conditionBase) {
		this.uuid = UUID.randomUUID();
		this.g = graphStructure;
		this.isComparisonDataInitialized = false;
		this.conditionBase = conditionBase;
	}
	
	/**
	 * Run the comparison data initialization with a given vertex measure.
	 * 
	 * @param <V> Variant type required by the coupler (data source variants must extends this type)
	 * @param <B> Variant type of the data 
	 * @param biCompDS Data source to measure on
	 * @param vertexMeasure Vertex measure that defines the comparison data.
	 * @return True, if initialization succeeded.
	 */
	private<B extends V, V extends CVariant> boolean initializeComparisonData(BiComparisonDataSource<B> biCompDS, 
			Collection<CSVertexMeasurer<V>> vertexMeasures) {

		// ================================================================================
		// Run Initialization on Vertices
		// ================================================================================
		List<Future<Boolean>> runningInitializations = new LinkedList<>();		// Task List
		
		for (CSVertexMeasurer<V> vertexMeasure : vertexMeasures) {
			for(CSGraphVertex v : this.g.vertexSet()) {
				runningInitializations.add(CachedBackgroundTaskService.getInstance().submit(
						new CSVertexInitializationTask<B>(this.g, v, vertexMeasure, biCompDS)));
			}
		}
		
		this.isComparisonDataInitialized = true;
		try {
			for(Future<Boolean> f : runningInitializations) {
				isComparisonDataInitialized &= f.get();
			}
		}
		catch (InterruptedException e1) {
			logger.error("Comparison data initialization has been interrupted");
			return false;
		}
		catch (ExecutionException e) {
			e.printStackTrace();
			logger.error("Error while running concurrent cornerstone graph initialization on vertices");
		}
		if (!this.isComparisonDataInitialized) {
			logger.error("Cornerstone comparison data initialization did not succeed!");
		}
		
		return this.isComparisonDataInitialized;
	}

	/**
	 * Run the comparison data initialization with default view description.
	 * 
	 * Uses global (frequency-aware) language transformer and standard LVS.
	 * 
	 * Runs the initialization for each vertex concurrently.
	 * @return True, if initialization succeeded.
	 */
	public<T extends CVariant> boolean initializeComparisonData(BiComparisonDataSource<T> biCompDS) {
		
		Collection<CSVertexMeasurer<CVariant>> measures = getDefaultMeasures(biCompDS);
		
		return this.initializeComparisonData(biCompDS, measures);
	}
	
	public Collection<CSVertexMeasurer<CVariant>> getDefaultMeasures(BiComparisonDataSource<?> biCompDS) { 

		biCompDS.ensureCaching();
		// Trace descriptor + distance
		FeatureExtractorDistancePairVariant<BasicTraceCC, CVariant, LevenshteinCCStateful> desDistPair = 
		    new FeatureExtractorDistancePairVariant<BasicTraceCC, CVariant, LevenshteinCCStateful>(
		        new TraceAsFeatureExtractor(),
		        new LevenshteinCCStateful()); 
		        
		
		List<CSVertexMeasurer<CVariant>> measures = new LinkedList<>();
		// Instantiate the measurement
		CSVertexResidualMeasurer<CVariant, BasicTraceCC, LevenshteinCCStateful> measureRes = 
		    new CSVertexResidualMeasurer<>(this, desDistPair);
		measures.add(measureRes);
		if (conditionBase.isPresent()) {
			CSVertexConditionedMeasurer<CVariant, BasicTraceCC, LevenshteinCCStateful> measureCond = 
					new CSVertexConditionedMeasurer<>(this, desDistPair, conditionBase.get());
			measures.add(measureCond);
			
			CSVertexConditionedResidualMeasurer<CVariant, BasicTraceCC, LevenshteinCCStateful> measureCondRes = 
					new CSVertexConditionedResidualMeasurer<>(this, desDistPair, conditionBase.get());
			measures.add(measureCondRes);
		}
		return measures;
	}

	/**
	 * Initialize the comparison data for the provided vertex.
	 * 
	 * Class that wraps initialization tasks for specific vertices such that initialization can be parallelized.
	 *
	 * @author brockhoff
	 *
	 * @param <E> Log coupler assumes this variant
	 * @param <T> Variant log element type (must extend the type required by the log coupler)
	 */
	public static class CSVertexInitializationTask<T extends CVariant> implements Callable<Boolean> {
		
		/** 
		 * Vertex to run the measurement on.
		 */
		private final CSGraphVertex v;

		/** 
		 * Measurement execution routine that couples log probability measures.
		 */
		private final CSVertexMeasurer<? super T> vertexMeasure;
		
		/**
		 * Base data source.
		 * Most likely, the vertex will copy this source and apply his particular reduction on it.
		 */
		private final BiComparisonDataSource<T> biCompDS;
		
		/**
		 * Handle to the graph in which the provided vertex {@link #v} is embedded.
		 * Useful to access, for example, neighbors.
		 */
		private final Graph<CSGraphVertex, DefaultEdge> g;


		public CSVertexInitializationTask(Graph<CSGraphVertex, DefaultEdge> g, 
				CSGraphVertex v, CSVertexMeasurer<? super T> vertexMeasure,
				BiComparisonDataSource<T> biCompDS) {
			super();
			this.g = g;
			this.v = v;
			this.vertexMeasure = vertexMeasure;
			this.biCompDS = biCompDS;
		}


		@Override
		public Boolean call() throws Exception {
			this.vertexMeasure.processVertex(v, biCompDS);
			return true;
		}
		
	}
	
	//================================================================================
	// Getters and Setters
	//================================================================================

	public Graph<CSGraphVertex, DefaultEdge> getG() {
		return g;
	}

	public boolean isComparisonDataInitialized() {
		return isComparisonDataInitialized;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	public Optional<ArrayList<Set<VertexCondition>>> getConditionBase() {
		return this.conditionBase;
	}

}
