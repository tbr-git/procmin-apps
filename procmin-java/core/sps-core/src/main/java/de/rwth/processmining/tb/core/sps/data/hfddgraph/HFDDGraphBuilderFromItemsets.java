package de.rwth.processmining.tb.core.sps.data.hfddgraph;

import java.util.Collection;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

/**
 * HFDD Graph Builder that can be externally configured with a collection of itemsets where each itemset is another 
 * collection of categorical codes.
 * 
 * @author brockhoff
 *
 */
public class HFDDGraphBuilderFromItemsets extends HFDDGraphBuilder {
	
	private Collection<? extends Collection<Integer>> itemsets;
	
	public HFDDGraphBuilderFromItemsets() {
		this.itemsets = null;
	}

	public HFDDGraphBuilderFromItemsets(Collection<? extends Collection<Integer>> itemsets) {
		super();
		this.itemsets = itemsets;
	}

	@Override
	protected Collection<? extends Collection<Integer>> mineActivityItemsets(
			BiComparisonDataSource<? extends CVariant> dataSource) throws SLDSTransformationError {
		return itemsets;
	}
	
	public void setItemsets(Collection<? extends Collection<Integer>> itemsets) {
		this.itemsets = itemsets;
	}

}
