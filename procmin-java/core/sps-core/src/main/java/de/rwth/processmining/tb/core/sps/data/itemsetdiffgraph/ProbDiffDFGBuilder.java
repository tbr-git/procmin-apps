package de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultDirectedGraph;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.StochasticLanguageIterator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.AbstTraceCC;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolAnalyzer;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.HFDDVertexMeasurer;
import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph.LogSide;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurementEMDSol;

public class ProbDiffDFGBuilder {
	private final static Logger logger = LogManager.getLogger( ProbDiffDFGBuilder.class );
	
	/**
	 * Handle to the graph structure that will be filled.
	 */
	private final DefaultDirectedGraph<ProbDiffDFGVertex, ProbDiffDFGEdge> probDFG;
	
	/**
	 * Category mapper for the activities.
	 */
	private final CategoryMapper cm;
	
	/**
	 * Maximum category code used for normal activities
	 */
	private final int maxActCatCode;

	/**
	 * Binary digits for max activity category code.
	 */
	private final int binCatDigits;

	/** 
	 * Artificial start vertex
	 */
	private final ProbDiffDFGVertexStart vStart;

	/** 
	 * Artificial end vertex
	 */
	private final ProbDiffDFGVertexEnd vEnd;

	/**
	 * Mapping from category code to activity vertex
	 */
	private final Map<Integer, ProbDiffDFGVertexActivity> mapActVert;
	
	/**
	 * Mapping from edge id to edge 
	 * edgeID: categoryCodeLeftVertex|categoryCodeRight vertex
	 */
	private final Map<Integer, ProbDiffDFGEdge> mapEdge;
	
	public ProbDiffDFGBuilder(CategoryMapper cm) {
		this.cm = cm;
		this.probDFG = new DefaultDirectedGraph<>(ProbDiffDFGEdge.class);		// Empty graph
		this.maxActCatCode = cm.getMaxCategoryCode();
		this.vStart = new ProbDiffDFGVertexStart(maxActCatCode + 1);
		this.vEnd = new ProbDiffDFGVertexEnd(maxActCatCode + 2);
		this.binCatDigits = 32 - Integer.numberOfLeadingZeros(maxActCatCode + 2 - 1) + 1;
		
		this.mapActVert = new HashMap<>();
		this.mapEdge = new HashMap<>();
	}
	
	public<B extends V, V extends CVariant, F extends TraceDescriptor> ProbDiffDFG buildProbDiffDFG(final HFDDVertex v, 
			final HFDDVertexMeasurer<V, F> measure, final BiComparisonDataSource<B> biCompDS, ProbDiffDFGType type) {

		HFDDMeasurementEMDSol<F> measurement = measure.measureVertexDetails(v, biCompDS, false);
		
		if (measurement.getEMDSolution().isEmpty()) {
			throw new RuntimeException("Probabilistic Difference DFG creation failed. Failed to get EMD solution!.");
		}
		else {
			return this.buildProbDiffDFG(measurement.getEMDSolution().get(), 
			    measurement.getProbLeftNonEmpty(), measurement.getProbRightNonEmpty(), type);
		}
	}

	public<F extends TraceDescriptor> ProbDiffDFG buildGlobalProbDiffDFG(EMDSolContainer<F> emdSol) {
		Pair<Double, Double> probNonEmpty = EMDSolAnalyzer.getProbabilityNonEmpty(emdSol);
		return buildProbDiffDFG(emdSol, probNonEmpty.getLeft(), probNonEmpty.getRight(), ProbDiffDFGType.GLOBAL);
	}
	
	public<F extends TraceDescriptor> ProbDiffDFG buildProbDiffDFG(EMDSolContainer<F> emdSol, 
	    double probNonEmptyLeft, double probNonEmptyRight, ProbDiffDFGType type) {
		//////////////////////////////
		// Add Traces
		//////////////////////////////
		
		probDFG.addVertex(vStart);
		probDFG.addVertex(vEnd);
		addStochLang(emdSol.getLanguageLeft(), LogSide.LEFT);
		addStochLang(emdSol.getLanguageRight(), LogSide.RIGHT);
		
		return new ProbDiffDFG(probDFG, probNonEmptyLeft, probNonEmptyRight, type);
		
	}
	
	private<F extends TraceDescriptor> void addStochLang(OrderedStochasticLanguage<F> lang, LogSide logSide) {
		StochasticLanguageIterator<F> it = lang.iterator();
		int i = 0;
		while(it.hasNext()) {
			F traceDesc = it.next();
			double p = it.getProbability();
		
			////////////////////
			// Digest Trace
			////////////////////
			addTrace(logSide, traceDesc, (float) p);
		}
	}
	
	private void addTrace(LogSide logSide, TraceDescriptor traceDesc, float p) {

		// Init last activity with artificial start
		ProbDiffDFGVertex lastAct = vStart;
		for(int i = 0; i < traceDesc.length(); i++) {
			
			//////////////////////////////
			// Init activity Id
			//////////////////////////////
			// Array of trace element's string representation
			String activity = traceDesc.toString(i);
			int categoryCode;
			if (traceDesc instanceof BasicTraceCC descCat) {
				categoryCode = descCat.getTraceCategories()[i];
			}
			else if (traceDesc instanceof AbstTraceCC descAbstCat) {
				categoryCode = descAbstCat.getTrace()[i];
			}
			else {
				categoryCode = this.cm.getCategory4Activity(activity);
			}

			//////////////////////////////
			// Vertex Update
			//////////////////////////////
			// Get or Init Activity Vertex
			ProbDiffDFGVertexActivity curAct = null;
			curAct = mapActVert.get(categoryCode);
			if (curAct == null) {
				curAct = new ProbDiffDFGVertexActivity(categoryCode, activity, 0f, 0f);
				this.probDFG.addVertex(curAct);
				mapActVert.put(categoryCode, curAct);
			}
			switch (logSide) {
				case LEFT:
					curAct.incProbabilityLeft(p);
					break;
				case RIGHT:
					curAct.incProbabilityRight(p);
					break;
				default:
					break;
			}
			
			//////////////////////////////
			// Edge Update
			//////////////////////////////
			int edgeId = lastAct.getCategoryCode() << (this.binCatDigits + 1) | curAct.getCategoryCode();
			ProbDiffDFGEdge e = null;
			
			e = mapEdge.get(edgeId);
			if (e == null) {
				e = new ProbDiffDFGEdge(edgeId);
				mapEdge.put(edgeId, e);
				this.probDFG.addEdge(lastAct, curAct, e);
			}

			switch (logSide) {
				case LEFT:
					e.incProbabilityLeft(p);
					break;
				case RIGHT:
					e.incProbabilityRight(p);
					break;
				default:
					break;
			}
			
			// Last vertex = current vertex
			lastAct = curAct;
		}

		//////////////////////////////
		// Last Edge Update
		//////////////////////////////
		int edgeId = lastAct.getCategoryCode() << (this.binCatDigits + 1) | vEnd.getCategoryCode();
		ProbDiffDFGEdge e = null;
		
		e = mapEdge.get(edgeId);
		if (e == null) {
			e = new ProbDiffDFGEdge(edgeId);
			mapEdge.put(edgeId, e);
			this.probDFG.addEdge(lastAct, vEnd, e);
		}
		switch (logSide) {
			case LEFT:
				e.incProbabilityLeft(p);
				break;
			case RIGHT:
				e.incProbabilityRight(p);
				break;
			default:
				break;
		}
		
	}
	
}
