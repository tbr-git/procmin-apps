package de.rwth.processmining.tb.core.sps.algorithm.iteration;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraph;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraphBuilderTimeLimited;

public class HFDDIterationManagementBuilderTimeout extends HFDDIterationManagementBuilder {
	
	/**
	 * A graph builder, if present, use this one.
	 */
	private HFDDGraphBuilderTimeLimited spsGraphBuilder;
	
	public HFDDIterationManagementBuilderTimeout() {
	  this.spsGraphBuilder = new HFDDGraphBuilderTimeLimited();
	}

	@Override
	protected HFDDGraph mineHFDDGraph(BiComparisonDataSource<? extends CVariant> biCompDS)
			throws HFDDIterationManagementBuildingException {
		// Create HFDD Graph
		HFDDGraph graph = null;
		try {
			graph = spsGraphBuilder.buildBaseHFDDGraph(biCompDS);
		} catch (SLDSTransformationError e) {
			e.printStackTrace();
			logger.error("Failed to create the HFDD Graph");
			throw new HFDDIterationManagementBuildingException("Failed to mine the HFDD Graph: " + e.getMessage(), e);
		}
		return graph;
	}
	
	public HFDDIterationManagementBuilderTimeout setTargetItemsetNumber(int targetNumber) {
	  this.spsGraphBuilder.setTargetActISNbr(targetNumber);
		return this;
	}

	public HFDDIterationManagementBuilderTimeout setTargetItemsetMargin(double margin) {
	  this.spsGraphBuilder.setTargetActISMargin(margin);
		return this;
	}
	
	public HFDDIterationManagementBuilderTimeout setMaxMiningTime(int time) {
		this.spsGraphBuilder.setFreqActMiningTimeMs(time);
		return this;
	}

	public HFDDIterationManagementBuilderTimeout setSPSGraphBuilder(HFDDGraphBuilderTimeLimited spsGraphBuilder) {
	  this.spsGraphBuilder = spsGraphBuilder;
		return this;
	}
	
}
