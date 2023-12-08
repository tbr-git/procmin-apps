package de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection.SLDSFilterMandatoryCategoryFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection.SLDSFilterVariantsContainCategoryFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection.SLDSFilterVariantsIffContainsAllCategoriesBuilder;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraph;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertexInfo;

/**
 * Creates the residual log, that is:
 * <p><ol>
 * <li> Filters on traces that contain all activities of the referenced {@link HFDDVertex}.
 * <li> Computes activities that are contained a child's itemset but <b>not</b> in this vertex.
 * <li> Removes traces that contain an item from the preceding itemset
 * <li> Projects on activities of the referenced {@link HFDDVertex}
 * </ol><p>
 */
public class CSGraphResidualLogTransformer implements HFDDVertexLogTransformerOuterContext {
	private final static Logger logger = LogManager.getLogger( CSGraphResidualLogTransformer.class );
	
	/**
	 * CS Graph. Based on this graph, we determine the successors. 
	 */
	private final CSGraph csGraph;
	
	/**
	 * Mapping HFDDVertices to cornerstone vertices.
	 * Based on the mapping, we query the neighborhood in the cornerstone 
	 * graph.
	 */
	private final Map<HFDDVertex, CSGraphVertex> map2CSVertex;
	
	public CSGraphResidualLogTransformer(CSGraph csGraph) {
		this.csGraph = csGraph;
		// Initial size 2 * number of entries
		this.map2CSVertex = new HashMap<>(2 * csGraph.getG().vertexSet().size());
		// Instantiate the map
		csGraph.getG().vertexSet().forEach(vCS -> this.map2CSVertex.put(vCS.getHfddVertexRef(), vCS));
	}

	/**
	 * Prepare the data source by applying the log transformation. Moreover, give statistics.
	 * 
	 * @param biCompDS
	 * @param v
	 * @return 
	 */
	@Override
	public<V extends CVariant> HFDDLogTransformStep<V> getDataSourceOuterContext(HFDDVertex v, BiComparisonDataSource<V> biCompDS) {
		CSGraphVertex vCS = this.map2CSVertex.get(v);
		
		if (vCS == null) {
			throw new IllegalArgumentException(
				"Provided vertex is not associated with a vertex in the cornerstone graph");
		}
		/*
		 * ================================================================================
		 * Get Transformed Data Source and Residual Probability ---
		 * ================================================================================
		 */
		try {
			////////////////////
			// Residual Log Filtering
			////////////////////
			biCompDS = getResidualLog(this.csGraph.getG(), vCS, biCompDS);
		} catch (SLDSTransformerBuildingException e1) {
			logger.error("Error during HFDDVertex measurement! Could not build data source transformation!");
			return new HFDDLogTransformStep<V>(biCompDS, FilterTag.FAIL);
		}

		return new HFDDLogTransformStep<V>(biCompDS, FilterTag.RESIDUAL);
	}
	
	/**
	 * Creates the residual log, that is:
	 * <p><ol>
	 * <li> Filters on traces that contain all activities of the referenced {@link HFDDVertex}.
	 * <li> Computes activities that are contained a child's itemset but <b>not</b> in this vertex.
	 * <li> Removes traces that contain an item from the preceding itemset
	 * <li> Projects on activities of the referenced {@link HFDDVertex}
	 * </ol><p>
	 * 
	 * @param <T>
	 * @param g CSGraph where v is contained
	 * @param v CSGraph vertex to create the residual log for
	 * @param biCompDS Datasource for which the residual flow is created
	 * @return
	 * @throws SLDSTransformerBuildingException
	 */
	public static<V extends CVariant> BiComparisonDataSource<V> getResidualLog(Graph<CSGraphVertex, DefaultEdge> g, 
			CSGraphVertex v, BiComparisonDataSource<V> biCompDS) throws SLDSTransformerBuildingException {
		// Create copy of datasource
		biCompDS = new BiComparisonDataSource<>(biCompDS);
		biCompDS.ensureCaching();
		HFDDVertexInfo vInfo = v.getHfddVertexRef().getVertexInfo();
		
		// Reduce data on vertex sublog
		// Mandatory activity filtering -> Each trace must contain the vertex' activities
		SLDSFilterMandatoryCategoryFactory<V> factoryMandatory = new SLDSFilterMandatoryCategoryFactory<>();
		factoryMandatory.setClassifier(biCompDS.getClassifier())
			.setCategoryMapper(vInfo.getCategoryMapper())
			.setActivities(vInfo.getActivities());
		biCompDS.applyTransformation(factoryMandatory);
		
		// Aggregate all child categories 
		// (suffices to keep differences due to the previous filter set)
		// Split into children that only have a single differing activity 
		// or mulitple
		List<List<BitSet>> childDiffActivities = g.outgoingEdgesOf(v).stream().map(e -> g.getEdgeTarget(e))
				.map(u -> u.getHfddVertexRef())
				.map(u -> u.getVertexInfo().getActivitiesCopy())
				.map(a -> { 
					a.xor(vInfo.getActivities());
					return a;
				})
				.collect(
					Collectors.teeing(
							Collectors.filtering(a -> a.cardinality() == 1, Collectors.toList()),
							Collectors.filtering(a -> a.cardinality() > 1, Collectors.toList()),
							List::of
						)
					);
	
		BitSet aggSingleDiff = childDiffActivities.get(0).stream().collect(BitSet::new, BitSet::or, BitSet::or);
			
		List<BitSet> multiDiff = childDiffActivities.get(1);

		// Efficiency optimization
		// Discard variants w.r.t. the single difference activity set in a SINGLE PASS
		// We can discard a trace if it contains ANY of these activities
		if (aggSingleDiff.cardinality() > 0) { 	
			SLDSFilterVariantsContainCategoryFactory<V> factoryExcludeChild = new SLDSFilterVariantsContainCategoryFactory<>();
			try {
				factoryExcludeChild.setClassifier(biCompDS.getClassifier())
					.keepTraces(false)
					.setCategoryMapper(biCompDS.getDataSourceLeft().getVariantLog().getCategoryMapper())
					.setActivities(aggSingleDiff);
			} catch (SLDSTransformationError e1) {
				e1.printStackTrace();
				throw new SLDSTransformerBuildingException("Could not instantiate residual log transformation. Querying problems: " + e1.getMessage());
			}
			biCompDS.applyTransformation(factoryExcludeChild);
		}
		
		// For each entry a variant must contain ALL activities
		for (BitSet aMulti : multiDiff) {
			SLDSFilterVariantsIffContainsAllCategoriesBuilder<V> factoryExcludeChild = 
					new SLDSFilterVariantsIffContainsAllCategoriesBuilder<>();
			try {
				factoryExcludeChild.setClassifier(biCompDS.getClassifier())
					.keepTraces(false)
					.setCategoryMapper(biCompDS.getDataSourceLeft().getVariantLog().getCategoryMapper())
					.setActivities(aMulti);
			} catch (SLDSTransformationError e1) {
				e1.printStackTrace();
				throw new SLDSTransformerBuildingException("Could not instantiate residual log transformation. Querying problems: " + e1.getMessage());
			}
			biCompDS.applyTransformation(factoryExcludeChild);
		}
		
		// Project on itemset activities
		//SLDSProjectionCategoryFactory<T> factoryProjection = new SLDSProjectionCategoryFactory<>();
		//factoryProjection.setClassifier(biCompDS.getClassifier())
		//	.setCategoryMapper(vInfo.getCategoryMapper())
		//	.setActivities(vInfo.getActivities());
		//biCompDS.applyTransformation(factoryProjection);

		// Ensure caching
		biCompDS.ensureCaching();
		return biCompDS;
	}

}
