package de.rwth.processmining.tb.core.sps.data.hfddgraph;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.sps.algorithm.WeightedActivitySetTransactionsBuilder;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedFrequentPattern;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedTransactionDataSourceImpl;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.fpgrowth.WeightedFPGrowth;

public class HFDDGraphBuilderByMinSupport extends HFDDGraphBuilder {
	
	/**
	 * Minimum support.
	 */
	private double minRelativeSupport = 0.02;

	@Override
	protected Collection<WeightedFrequentPattern<Integer>> mineActivityItemsets(
			BiComparisonDataSource<? extends CVariant> dataSource) 
			throws SLDSTransformationError {
//		int totalSize = dataSource.getDataSourceLeft().getVariantLog().sizeLog();
//		totalSize += dataSource.getDataSourceRight().getVariantLog().sizeLog();

		// Build transactions
		WeightedTransactionDataSourceImpl<Integer> FISTransactions = 
				WeightedActivitySetTransactionsBuilder.buildFromLog(dataSource);
		//WeightedFPGrowth<Integer> alg = new WeightedFPGrowth<>();
		WeightedFPGrowth<Integer> alg = new WeightedFPGrowth<>((i1, i2) -> Integer.compare(i1, i2));
	
		Collection<WeightedFrequentPattern<Integer>> res = null;
		try {
			res = alg.findFrequentPattern(minRelativeSupport, FISTransactions).get().get().frequentPatterns();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;

	}

	public double getMinRelativeSupport() {
		return minRelativeSupport;
	}

	public HFDDGraphBuilderByMinSupport setMinRelativeSupport(double minRelativeSupport) {
		this.minRelativeSupport = minRelativeSupport;
		return this;
	}

}
