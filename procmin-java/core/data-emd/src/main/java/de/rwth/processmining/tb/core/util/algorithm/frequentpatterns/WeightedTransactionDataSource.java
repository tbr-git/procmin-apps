package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns;

/**
 * This code is based on the code found in  https://github.com/PySualk/fp-growth-java 
 * @author brockhoff
 * @author PySualk
 *
 */
public interface WeightedTransactionDataSource<T> {

	WeightedTransaction<T> next();

	boolean hasNext();

	void reset();

}
