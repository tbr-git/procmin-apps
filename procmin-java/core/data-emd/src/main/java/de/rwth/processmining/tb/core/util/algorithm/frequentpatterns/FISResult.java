package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns;

import java.util.Collection;

/**
 * @param frequentPatterns Patterns found
 * @param resultComplete Is the itemset complete (i.e., contains all items for the chosen threshold)
 * @param minSupport Minimum support at which the results was mined
*/
public record FISResult<T>(
	Collection<WeightedFrequentPattern<T>> frequentPatterns,
	boolean resultComplete, 
	double minSupport) {
}
