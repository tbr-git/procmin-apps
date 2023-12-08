package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns;

import java.util.Iterator;
import java.util.List;

public class WeightedTransactionDataSourceImpl<T> implements WeightedTransactionDataSource<T> {

	private List<WeightedTransaction<T>> listTransactions;
	
	private Iterator<WeightedTransaction<T>> itTransactions;

	public WeightedTransactionDataSourceImpl(List<WeightedTransaction<T>> listTransactions) {
		this.listTransactions = listTransactions;
		this.itTransactions = this.listTransactions.iterator();
	}	

	@Override
	public boolean hasNext() {
		return this.itTransactions.hasNext();
	}

	@Override
	public WeightedTransaction<T> next() {
		return itTransactions.next();
	}

	@Override
	public void reset() {
		itTransactions = this.listTransactions.iterator();
	}

}
