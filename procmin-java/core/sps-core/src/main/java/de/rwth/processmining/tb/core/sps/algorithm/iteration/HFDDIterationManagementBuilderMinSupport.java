package de.rwth.processmining.tb.core.sps.algorithm.iteration;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraph;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraphBuilderByMinSupport;

public class HFDDIterationManagementBuilderMinSupport extends HFDDIterationManagementBuilder {

	/**
	 * Minimum itemsets support.
	 * Activity itemsets under the threshold are so infrequent that they
	 * cannot contribute to a meaningful difference.
	 * Defaults to 0.05.
	 */
	private double minSetSupport = 0.05;

	/**
	 * Create the initial HFDD Graph.
	 * 
	 * @param biCompDS Data source to mine the HFDD graph structure from (frequent IS)
	 * @return HFDDGraph
	 * @throws HFDDIterationManagementBuildingException Raised if the creation fails
	 */
	@Override
	protected HFDDGraph mineHFDDGraph(BiComparisonDataSource<? extends CVariant> biCompDS) 
			throws HFDDIterationManagementBuildingException {

		// Create HFDD Graph
		HFDDGraph graph = null;
		HFDDGraphBuilderByMinSupport graphBuilder = new HFDDGraphBuilderByMinSupport();
		graphBuilder.setMinRelativeSupport(minSetSupport);
		try {
			graph = graphBuilder.buildBaseHFDDGraph(biCompDS);
		} catch (SLDSTransformationError e) {
			e.printStackTrace();
			logger.error("Failed to create the HFDD Graph");
			throw new HFDDIterationManagementBuildingException("Failed to mine the HFDD Graph: " + e.getMessage(), e);
		}
		return graph;
	}
	
	public double getMinSetSupport() {
		return minSetSupport;
	}

	public HFDDIterationManagementBuilderMinSupport setMinSetSupport(double minSetSupport) {
		this.minSetSupport = minSetSupport;
		return this;
	}
}
