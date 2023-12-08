package de.rwth.processmining.tb.core.sps.algorithm.measure;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.util.VariantCopyingException;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraph;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;

public class DependantVertexFinder {

	/**
	 * 
	 * @param biCompDS The basic data source (of the entire log)
	 * @return
	 * @throws SLDSTransformerBuildingException 
	 * @throws SLDSTransformationError 
	 */
	public static Set<HFDDVertex> findDependantVertices(HFDDGraph hfddGraph, HFDDVertex v, 
			BiComparisonDataSource<? extends CVariant> biCompDS, 
			double maxProbCoverLoss) throws SLDSTransformerBuildingException, SLDSTransformationError {
		
		final BitSet knownDependencies = new BitSet(hfddGraph.getVertices().size() + 1);

		// V's SELECTION of relevant variants 
		// Compute ones upfront
		BiComparisonDataSource<? extends CVariant> biCompDSV = v.getVertexInfo()
				.createLogVertexSelection(biCompDS);
		
		// Parallel Stream
		ArrayList<HFDDVertex> vertices = new ArrayList<HFDDVertex>(hfddGraph.getVertices());
		vertices.sort((u, w) -> Integer.compare(u.getVertexInfo().getActivities().cardinality(), 
				w.getVertexInfo().getActivities().cardinality()));
		return vertices.parallelStream()
			.map(u -> {
				try {
					return checkDependency(hfddGraph, v, biCompDS, biCompDSV, u, knownDependencies, maxProbCoverLoss);
				} catch (VariantCopyingException | SLDSTransformationError e) {
					e.printStackTrace();
					return new HashSet<HFDDVertex>();
				}
			})
			.flatMap(s -> s.stream())
			.collect(Collectors.toSet());
	}
	
	/**
	 * 
	 * @param hfddGraph
	 * @param v
	 * @param biCompDSVSelected
	 * @param u
	 * @param knownDependencies
	 * @return
	 * @throws VariantCopyingException
	 * @throws SLDSTransformationError
	 */
	private static Set<HFDDVertex> checkDependency(HFDDGraph hfddGraph, HFDDVertex v, 
			BiComparisonDataSource<? extends CVariant> biCompDS, BiComparisonDataSource<? extends CVariant> biCompDSVSelected, 
			HFDDVertex u, BitSet knownDependencies, double maxProbCoverLoss) throws VariantCopyingException, SLDSTransformationError {
		
		////////////////////////////////////////////////////////////
		// Important
		// Heavy on side effects!!!
		// - the knowDependency Bitset will be modified concurrently
		//		-> Since we only set bits, we might start unnecessary work, but we
		// 			don't need to synchronize
		// 
		// The different logs:
		// - biCompDS: Outer-scope scope log (used on the entire graph without any filtering for any specific vertex)
		// - biCompDSVSelected: Contains variants that v would project on non-empty traces -> Full variants!
		////////////////////////////////////////////////////////////

		// We already could infer it for this vertex
		if (knownDependencies.get(u.getId())) {
			return Collections.emptySet();
		}
		
		// Base outer-scope log size
		int sizeL = biCompDS.getDataSourceLeft().getVariantLog().sizeLog();
		int sizeR = biCompDS.getDataSourceRight().getVariantLog().sizeLog();
		
		// Probability of v
		double probVLeft = v.getVertexInfo().getBaseMeasurement().getProbLeftNonEmpty();
		double probVRight = v.getVertexInfo().getBaseMeasurement().getProbRightNonEmpty();

		// Probability w.r.t base log
		double probULeft = u.getVertexInfo().getBaseMeasurement().getProbLeftNonEmpty();
		double probURight = u.getVertexInfo().getBaseMeasurement().getProbRightNonEmpty();
		
		// Size difference tells us that v => u does not hold
		// U is much more likely than V
		// => many occurrences of U without the context of V
		if (probULeft - probVLeft > maxProbCoverLoss 
				|| probURight - probVRight > maxProbCoverLoss) {
			return Collections.emptySet();
		}
		
		int[] activitiesU = u.getVertexInfo().getActivities().stream().toArray();
		// Compare left log 
		int resL = compareConditionLogs(activitiesU, probULeft, sizeL, 
			biCompDSVSelected.getDataSourceLeft().getVariantLog(), maxProbCoverLoss);

		if (resL == -1) {
			// Completely disjoint on left side -> Not conditional relation 
			// Moreover, intersection of v and more specific subprocesses of u will be empty
			// => Discard subtree
			flagDescendants(hfddGraph, u, knownDependencies, false);
			return Collections.emptySet();
		}
		else if (resL == 0) {
			// There is a small intersection
			return Collections.emptySet();
		}
		else {
			// Decent intersection for left log
			// => Check right
			int resR = compareConditionLogs(activitiesU, probURight, sizeR, 
				biCompDSVSelected.getDataSourceRight().getVariantLog(), maxProbCoverLoss);
			if (resR == -1) {
				// Completely disjoint on right side -> Not conditional relation 
				// Moreover, intersection of v and more specific subprocesses of u will be empty
				// => Discard subtree
				flagDescendants(hfddGraph, u, knownDependencies, false);
				return Collections.emptySet();
			}
			else if (resR == 0) {
				// There is a small intersection
				return Collections.emptySet();
			}
			else {
				// Good intersection left and right
				Set<HFDDVertex> specializationClosure = flagDescendants(hfddGraph, u, knownDependencies, true);
				return specializationClosure;
			}
		}
		
		//			// Counts Left after selection
		//			int countLeftUSel = biCompDSVSelected.getDataSourceLeft()
		//					.getVariantLog().getTracesMandatoryActivities(activitiesU).stream()
		//						.mapToInt(variant -> variant.getSupport()).sum();
		//					
		//			// Not dependent
		//			// Moreover, more specific children of u cannot be dependent as well
		//			if (countLeftUSel == 0) {
		//			}
		//			double probNeLeftUSel = ((double) countLeftUSel) / sizeL;
		//		if (Math.abs(probNeLeftUBase - probNeLeftUSel) > maxProbCoverLoss) {
		//			return Collections.emptySet();
		//		}
		//
		//		// Don't merge with above to avoid log processing
		//		// Counts Left after selection
		//		int countRightUSel = biCompDSVSelected.getDataSourceRight()
		//				.getVariantLog().getTracesMandatoryActivities(activitiesU).size();
		//		double probNeRightUSel = ((double) countRightUSel) / sizeL;
		//
		//		if (Math.abs(probNeRightUBase - countRightUSel) > maxProbCoverLoss) {
		//			GraphIterator<HFDDVertex, DefaultEdge> itGraph = 
		//					new DepthFirstIterator<>(this.hfddGraph.getGraph(), u);
		//			
		//			// No need to process it. Won't depend anyway
		//			while (itGraph.hasNext()) {
		//				this.knownDependencies.set(itGraph.next().getId());
		//			}
		//			
		//			return Collections.emptySet();
		//		}
		//		
		//		// V => Always U 
		//		// Then, this must also hold for all children of u
		//		if (Double.compare(probNeLeftUSel, probNeLeftUBase) == 0 
		//				&& Double.compare(probNeRightUSel, probNeRightUBase) == 0) {
		//			Set<HFDDVertex> dependantVertices = new HashSet<>();
		//			
		//			GraphIterator<HFDDVertex, DefaultEdge> itGraph = 
		//					new DepthFirstIterator<>(this.hfddGraph.getGraph(), u);
		//			
		//			// No need to process it. Won't depend anyway
		//			while (itGraph.hasNext()) {
		//				HFDDVertex w = itGraph.next();
		//				this.knownDependencies.set(w.getId());
		//				dependantVertices.add(w);
		//			}
		//			return dependantVertices;
		//		}
		//		else {
		//			return Collections.singleton(u);
		//		}
	}
	
	private static Set<HFDDVertex> flagDescendants(HFDDGraph hfddGraph, HFDDVertex u, 
			BitSet knownDependencies, boolean collectDesc) {

		Set<HFDDVertex> descendants = null;
		if (collectDesc) {
			descendants = new HashSet<>();
		}
		GraphIterator<HFDDVertex, DefaultEdge> itGraph = 
				new DepthFirstIterator<>(hfddGraph.getGraph(), u);
		HFDDVertex w;
		while (itGraph.hasNext()) {
			w = itGraph.next();
			if (collectDesc) {
				descendants.add(w);
			}
			knownDependencies.set(w.getId());
		}
		return descendants;
	}
	
	private static int compareConditionLogs(int[] activitiesU, double probU, int sizeLogBase, 
			CVariantLog<? extends CVariant> condLog, double maxProbCoverLoss) throws VariantCopyingException {
		
		// Since U is not relevant in this log at all, 
		// conditioning would be fine with this log.
		// In fact, choice then depends on the other log 
		// (assuming that probability is not 0 in both logs)
		if (Double.compare(probU, 0) != 1) {
			return 1;
		}
		// Count U's selections in condition log (i.e., V's selection)
		int countUV = condLog.getTracesMandatoryActivities(activitiesU).stream()
					.mapToInt(variant -> variant.getSupport()).sum();
		
		
		// No intersection
		// Moreover, more specific children of u cannot be dependent as well
		if (countUV == 0) {
			return -1;
		}
		else {
			// Probability of U AND V
			double probUV = ((double) countUV) / sizeLogBase;
			
			if (probU - probUV > maxProbCoverLoss) {
				return 0;
			}
			else {
				return 1;
			}

		}
	}
}
