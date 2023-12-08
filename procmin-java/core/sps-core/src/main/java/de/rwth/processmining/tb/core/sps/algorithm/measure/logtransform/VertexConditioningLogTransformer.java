package de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection.SLDSFilterMandatoryCategoryFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexCondition;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexConditionType;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;

public class VertexConditioningLogTransformer implements HFDDVertexLogTransformerOuterContext {

	private final static Logger logger = LogManager.getLogger( VertexConditioningLogTransformer.class );

	private final Optional<ArrayList<Set<VertexCondition>>> aggDataCBase;
	
	public VertexConditioningLogTransformer(Optional<ArrayList<Set<VertexCondition>>> aggDataCBase) {
		this.aggDataCBase = aggDataCBase;
	}
	
	
	@Override
	public<V extends CVariant> HFDDLogTransformStep<V> getDataSourceOuterContext(HFDDVertex v, 
			BiComparisonDataSource<V> biCompDS) throws SLDSTransformerBuildingException {

		Optional<BitSet> conditionActivities = getConditionActivities(aggDataCBase, v);
		if (conditionActivities.isPresent()) {

			// Build aggregated activity condition set (AND over condition vertices)
			// ONLY consider PROBABILISTIC conditioning 
			if (conditionActivities.get().cardinality() > 0) {
				// Create copy of datasource
				biCompDS = new BiComparisonDataSource<>(biCompDS);
				// Reduce to variants that contain condition activities  
				// Mandatory activity filtering -> Each trace must contain the vertex' activities
				SLDSFilterMandatoryCategoryFactory<V> factoryMandatory = 
						new SLDSFilterMandatoryCategoryFactory<>();
				factoryMandatory.setClassifier(biCompDS.getClassifier())
					.setCategoryMapper(v.getVertexInfo().getCategoryMapper())
					.setActivities(conditionActivities.get());
				biCompDS.applyTransformation(factoryMandatory);
			}
			return new HFDDLogTransformStep<V>(biCompDS, FilterTag.CONDITION);
		}
		else {
			return new HFDDLogTransformStep<V>(biCompDS, FilterTag.CONDITION_NONE);
		}
	}
	
	/**
	 * Build aggregated activity condition set (AND over condition vertices) 
	 * ONLY consider PROBABILISTIC conditioning 
	 * 
	 * @param s
	 * @return
	 */
	public static Optional<BitSet> getConditionActivities(Optional<ArrayList<Set<VertexCondition>>> aggDataCBase, 
			HFDDVertex v) { 
		
		if (aggDataCBase.isPresent() && aggDataCBase.get().get(v.getId()) != null) {
			Set<VertexCondition> s = aggDataCBase.get().get(v.getId());
			BitSet conditionActivities = s.stream()
					.filter(vertCon -> vertCon.type() == VertexConditionType.PROBCOND)
					.map(vertCon -> vertCon.condVertex().getVertexInfo().getActivities())
					.reduce(new BitSet(v.getVertexInfo().getCategoryMapper().getMaxCategoryCode()),
							(setUnion, u) -> {
								setUnion.or(u);
								return setUnion;
							});
			return Optional.of(conditionActivities);
		}
		else {
			return Optional.empty();
		}
		
	}
}
