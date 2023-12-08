package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.fpgrowth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.FISResult;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedFrequentPattern;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedTransaction;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedTransactionDataSource;
import de.rwth.processmining.tb.core.util.backgroundwork.CachedBackgroundTaskService;


/**
 * This code is based on the code found in  https://github.com/PySualk/fp-growth-java 
 * @author brockhoff
 * @author PySualk
 *
 */
public class WeightedFPGrowth<T> {
	
	private static final double SUPPORT_SEARCH_MIN = 0.001;
	private static final double SUPPORT_SEARCH_MAX = 0.999;
	private static final double MIN_INTERVAL_SIZE = 0.001;
	

	private Map<T, Double> itemWeights = new HashMap<>();
	private WeightedFPtree<T> fpTree;
	private Map<T, WeightedFPtree<T>> headerTable = new HashMap<>();
	private Set<WeightedFrequentPattern<T>> frequentPatterns = new HashSet<>();
	private Double minSupport;
	private Double totalTransactionWeight;
	private WeightedTransactionDataSource<T> dataSource;
	
	private final Comparator<T> tieBreakingItem;

	private static final Logger logger = LogManager.getLogger(WeightedFPGrowth.class);
	
	public WeightedFPGrowth(Comparator<T> tieBreakingItem) {
		this.tieBreakingItem = tieBreakingItem;
	}

	/**
	 * Mine frequent pattern using FP-growth.
	 * @param minSupport Minimum support
	 * @param dataSource Data source
	 * @return Frequent itemsets
	 */
	public CompletableFuture<Optional<FISResult<T>>> findFrequentPattern(Double minSupport,
			WeightedTransactionDataSource<T> dataSource) {

		return  CompletableFuture.supplyAsync(() -> {
			this.dataSource = dataSource;
			countFrequencyByWord();
			buildFPTree(dataSource);
			this.minSupport = minSupport;
			try {
				this.findFrequentPatterns(-1);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return Optional.empty();
			}
			return Optional.of(new FISResult<T>(this.frequentPatterns, true, this.minSupport));
		});
	}

	/**
	 * Mine frequent itemsets using FP-growth.
	 * 
	 * Mining can be timed out after a specified number of milliseconds.
	 * Since timeout without solution is probably never helpful, this implementation
	 * will return an intermediate results.
	 * Since the currently found patterns for FP-growth are not a good intermediate result
	 * (FP-growth is DFS and, therefore, it would return a random deep "path" from a "random" subtree), 
	 * this implementation applies a binary search to find a reasonable number of items within the given time.
	 * 
	 * @param dataSource Data source
	 * @param maxMiningTimeInMs Time till timeout in ms (maxMiningTimeInMs <= 0 means no timeout)
	 * @param targetItemsetNbr Target itemset number 
	 * @param targetINbrMargin Margin around target [(1-margin) * target, (1 + margin) * target]
	 * @return (Currently) found best frequent itemsets
	 */
	public CompletableFuture<Optional<FISResult<T>>> findFrequentPattern(
			WeightedTransactionDataSource<T> dataSource, int maxMiningTimeInMs, int targetItemsetNbr, 
			double targetINbrMargin) {
		this.dataSource = dataSource;
		countFrequencyByWord();
		buildFPTree(dataSource);
		////////////////////////////////////////////////////////////////////////////////
		// Mine Patterns
		// Mining can time out
		// In case of a timeout, the current patterns are returned
		////////////////////////////////////////////////////////////////////////////////
		CompletableFuture<Optional<FISResult<T>>> runPatternDiscovery =  CompletableFuture.supplyAsync(() -> {
			// We need a nested normal future
			// CompletableFutures can timeout but it is difficult to cancel the task that was
			// running when the timeout occurred. (By design)
			// Directly submitting a normal future has a stronger connection to the handling thread
			// and, therefore, we can flag an interruption by canceling the future.
			
			// If we want to save intermediate best results in case that we search the support by defining
			// and result size interval, we need an atomic reference to the intermediate results
			AtomicReference<Optional<FISResult<T>>> bestResult = new AtomicReference<>(Optional.empty());
		
			Future<Optional<FISResult<T>>> miningTask = CachedBackgroundTaskService.getInstance().submit(
					() -> searchPatternsSupportForItemsetSizeInterval(dataSource, 
							targetItemsetNbr, 
							targetINbrMargin, 
							bestResult));
			try {
				// Only timeout if we want to
				if (maxMiningTimeInMs > 0) {
					return miningTask.get(maxMiningTimeInMs, TimeUnit.MILLISECONDS);
				}
				else {  
					// Block until completion
					return miningTask.get();
				}
			} catch (InterruptedException e) {
				// Pass
			} catch (ExecutionException e) {
				// Pass
			} catch (TimeoutException e) {
				logger.info("FIS Mining timed out. Returning intermediate results.");
				// In case of a timeout, CANCEL the mining task
				miningTask.cancel(true);
			}
			return bestResult.get();
		});
		
		return runPatternDiscovery;
	}
	
	
	/**
	 * Run a binary search until it finds a support threshold that is within the 
	 * [targetItemsetNbr * ( 1 - margin), targetItemsetNbr * (1 + margin)] range. 
	 * In case of a timeout, the currently best itemset will be returned.
	 * Best itemsets are:
	 * 1. Completely mined (i.e., FIS mining was not canceled due to size)
	 * 	-> Such itemsets cover at least all items and are not stuck in a FPGrowth Subtree
	 * 2. Incomplete and above the max size and maximum min support
	 *  -> Hopefully, these itemsets have a better breath-wise covering
	 * @param dataSource Data source to mine the itemsets on
	 * @param targetItemsetNbr Target number of itemsets
	 * @param targetINbrMargin Allowed margin around the target number
	 * @param bestResult Write updates to intermediate results there.
	 * @return
	 */
	private Optional<FISResult<T>> searchPatternsSupportForItemsetSizeInterval(
			WeightedTransactionDataSource<T> dataSource, int targetItemsetNbr, 
			double targetINbrMargin, AtomicReference<Optional<FISResult<T>>> bestResult) {
		
		// Target itemset interval MIN
		int minItemsetSize = (int) ((1 - targetINbrMargin) * targetItemsetNbr);
		// Target itemset interval MAX
		int maxItemsetSize = (int) ((1 + targetINbrMargin) * targetItemsetNbr);
		// Number of itemsets found in last minin round
		int lastMiningRoundSize;
		// Did the last mining round complete (i.e., found ALL itemsets for the given support)?
		boolean isResultComplete;
		int round = 0;
		boolean done = false;
		double low = 0;
		double high = 1;
		this.minSupport = (low + high) / 2;
		while (!done) { 
			////////////////////////////////////////////////////////////////////////////////
			// MINING
			////////////////////////////////////////////////////////////////////////////////
			try {
				isResultComplete = this.findFrequentPatterns(maxItemsetSize);
			}
			catch (InterruptedException e) {
				////////////////////////////////////////
				// Timeout
				////////////////////////////////////////
				// No result found yet => Take what we have
				if (bestResult.get().isEmpty()) {
					bestResult.set(Optional.of(
							new FISResult<T>(this.frequentPatterns, false, this.minSupport)));

				}
				return bestResult.get();
			};

			lastMiningRoundSize = this.frequentPatterns.size();
			logger.debug("Round " + round + ": Found " + lastMiningRoundSize + " itemsets."); 

			////////////////////////////////////////
			// Optimal result
			// (Mining terminated and count is within bounds)
			// => Stop searching
			////////////////////////////////////////
			if (isResultComplete 
					&& lastMiningRoundSize >= minItemsetSize 
					&& lastMiningRoundSize <= maxItemsetSize) {
				logger.debug("Round " + round + ": Itemsets in target interval. Terminating FIS mining."); 
				bestResult.set(Optional.of(
						new FISResult<T>(this.frequentPatterns, isResultComplete, this.minSupport)));
				done = true;
			}
			else {
				////////////////////////////////////////
				// Update best intermediate result
				////////////////////////////////////////
				////////////////////
				// No result found yet.
				////////////////////
				// => Save this one
				if (bestResult.get().isEmpty()) {
					logger.debug("Round " + round + ": No intermediate best result! Taking the first one."); 
					bestResult.set(Optional.of(
							new FISResult<T>(this.frequentPatterns, isResultComplete, this.minSupport)));
				}
				////////////////////
				// Complete result
				// lower min support
				////////////////////
				// We could also prefer larger sets in general as they might contain more information;
				// however, since FPGrowth is DFS, we might be stuck in a single large and deep subtree
				if (isResultComplete && 
						(!bestResult.get().get().resultComplete() 
								|| this.minSupport < bestResult.get().get().minSupport())) {
					logger.debug("Round " + round + ": Found complete result with lower min support!"); 
					bestResult.set(Optional.of(
							new FISResult<T>(this.frequentPatterns, isResultComplete, this.minSupport)));
				}
				else if (!isResultComplete 
						&& !bestResult.get().get().resultComplete() 
						&& this.minSupport > bestResult.get().get().minSupport()) {
					////////////////////
					// Incomplete result
					// Larger min support
					////////////////////
					// Assumption: if results are incomplete and current best result is incomplete,
					// Store the one with a larger minimum support 
					// as information should be spread wider (DFS FPGrowth)
					logger.debug("Round " + round + ": Found incomplete result with larger min support!"); 
					bestResult.set(Optional.of(
							new FISResult<T>(this.frequentPatterns, isResultComplete, this.minSupport)));
				}
				////////////////////////////////////////
				// Update search boundaries
				////////////////////////////////////////
				// Too few itemsets => reduce minimal support
				if (lastMiningRoundSize < minItemsetSize) {
					high = this.minSupport;
					this.minSupport = (low + high) / 2;
					logger.debug("Round " + round 
							+ ": Too few itemsets. Reducing the minimum support to " + this.minSupport); 
				}
				// Too many itemsets => increase minimal support
				else {
					low = this.minSupport;
					this.minSupport = (low + high) / 2;
					logger.debug("Round " + round 
							+ ": Too many itemsets. Increasing the minimum support to " + this.minSupport); 
				}
				if ((high - low) < MIN_INTERVAL_SIZE) {
					done = true;
					logger.debug("Round " + round 
							+ ": Terminating after support adaption (Search interval too small)"); 
				}
				if ( this.minSupport < SUPPORT_SEARCH_MIN || this.minSupport > SUPPORT_SEARCH_MAX) {
					done = true;
					logger.debug("Round " + round 
							+ ": Terminating after support adaption (Too small or too large)"); 
				}
			}
			frequentPatterns = new HashSet<>();
			round++;
		}
		return bestResult.get();
	}
	


	private void countFrequencyByWord() {

		dataSource.reset();
		this.totalTransactionWeight = 0.0;
		while (dataSource.hasNext()) {
			WeightedTransaction<T> t = dataSource.next();
			this.totalTransactionWeight += t.getWeight();
			for (T word : t.getItems()) {
				if (itemWeights.containsKey(word)) {
					Double oldWeight = itemWeights.get(word);
					itemWeights.replace(word, oldWeight + t.getWeight());
				} else {
					itemWeights.put(word, t.getWeight());
				}
			}
		}

		logger.debug("minSupport: " + this.minSupport);
		logger.debug("TotalTransactionWeight: " + totalTransactionWeight);

	}

	private void buildFPTree(WeightedTransactionDataSource<T> dataSource) {

		// Add root to FPTree
		this.fpTree = new WeightedFPtree<>(null, null);
		this.fpTree.setRoot(Boolean.TRUE);

		// Create Header Table
		Map<T, WeightedFPtree<T>> headerTable = new HashMap<>();

		// Iterate over transactions but order items by frequency
		dataSource.reset();
		while (dataSource.hasNext()) {
			WeightedTransaction<T> t = dataSource.next();
			List<T> orderedWords = orderWordsByWeight(t.getItems(), this.itemWeights);

			logger.trace("Processing Transaction " + orderedWords);

			List<Double> orderedWordsValues = new ArrayList<>();
			for (int i = 0; i < t.getItems().size(); i++) {
				orderedWordsValues.add(t.getWeight());
			}

			insertFPTree(this.fpTree, orderedWords, orderedWordsValues,
					headerTable);

			this.headerTable = headerTable;
		}

	}

	private List<T> orderWordsByWeight(List<T> words,
			Map<T, Double> weights) {
		List<T> orderedWords =  new LinkedList<>(words);
		// I need to save the first comparing, otherwise .reversed() looses T and makes it an object comparator.
		Comparator<T> compareWeights = Comparator.comparing(w -> weights.get(w));
		Comparator<T> compareWeightsRev = compareWeights.reversed();
		Comparator<T> compareWRevWithTieBreak = compareWeightsRev.thenComparing(tieBreakingItem);
		orderedWords.sort(compareWRevWithTieBreak); 
		return orderedWords;
	}

	private void insertFPTree(WeightedFPtree<T> tree, List<T> words,
			List<Double> wordValues, Map<T, WeightedFPtree<T>> headerTable) {
		if (tree.getChildren().size() == 0) {
			if (words.size() > 0) {
				WeightedFPtree<T> subTree = new WeightedFPtree<>(words.get(0), tree);
				subTree.setParent(tree);
				subTree.setPathWeight(wordValues.get(0));
				if (headerTable.containsKey(words.get(0))) {
					subTree.setNext(headerTable.get(words.get(0)));
					headerTable.replace(words.get(0), subTree);
				} else {
					headerTable.put(words.get(0), subTree);
				}
				if (words.size() > 1)
					insertFPTree(subTree, words.subList(1, words.size()),
							wordValues.subList(1, wordValues.size()),
							headerTable);
				tree.addChild(subTree);
			}
		} else {
			for (WeightedFPtree<T> child : tree.getChildren()) {
				if (child.getItem().equals(words.get(0))) {
					child.increasePathWeight(wordValues.get(0));
					if (words.size() > 1)
						insertFPTree(child, words.subList(1, words.size()),
								wordValues.subList(1, wordValues.size()),
								headerTable);
					return;
				}
			}
			WeightedFPtree<T> newChild = new WeightedFPtree<T>(words.get(0), tree);
			newChild.setParent(tree);
			newChild.setPathWeight(wordValues.get(0));
			if (headerTable.containsKey(words.get(0))) {
				newChild.setNext(headerTable.get(words.get(0)));
				headerTable.replace(words.get(0), newChild);
			} else {
				headerTable.put(words.get(0), newChild);
			}
			if (words.size() > 1)
				insertFPTree(newChild, words.subList(1, words.size()),
						wordValues.subList(1, wordValues.size()), headerTable);
			tree.addChild(newChild);
		}

	}

	/**
	 * Mines the frequent patterns
	 * @param maxItemsets Maximum number of itemsets; otherwise terminated
	 * @return True if mining was completed (not too many itemsets)
	 * @throws InterruptedException 
	 */
	private boolean findFrequentPatterns(int maxItemsets) throws InterruptedException {
		fpGrowthStep(this.headerTable, this.frequentPatterns, 
				Collections.unmodifiableList(new LinkedList<>()), maxItemsets);
		if (maxItemsets > 0) {
			if (this.frequentPatterns.size() > maxItemsets) {
				return false;
			}
		}
		return true;
	}

	private void fpGrowthStep(Map<T, WeightedFPtree<T>> headerTable,
                              Set<WeightedFrequentPattern<T>> frequentPatterns, List<T> base, int maxItemsets) throws InterruptedException {

		for (T item : headerTable.keySet()) {
		
			// Interrupt on timeout
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			// Check if maximum number 
			if (maxItemsets > 0 && frequentPatterns.size() > maxItemsets) {
				return;
			}
			
			WeightedFPtree<T> treeNode = headerTable.get(item);

			List<T> currentPattern = new LinkedList<>(base);
			currentPattern.add(0, item);
			currentPattern = Collections.unmodifiableList(currentPattern);

			logger.trace("=============================================");
			logger.trace("Start Mining Rules for " + currentPattern);

			// 1. Step: Conditional Pattern Base
			Map<List<T>, Double> conditionalPatternBase = new HashMap<>();

			// Is the item frequent? (count >= minSupport)
			Double frequentItemsetCount = 0.0;

			// Jump from leaf to leaf
			while (treeNode != null) {

				LinkedList<T> conditionalPattern = new LinkedList<>();
				frequentItemsetCount += treeNode.getPathWeight();
				Double supportConditionalPattern = treeNode.getPathWeight();

				WeightedFPtree<T> parentNode = treeNode.getParent();

				// Work yourself up to the root
				while (!parentNode.isRoot()) {
					conditionalPattern.addFirst(parentNode.getItem());
					parentNode = parentNode.getParent();
				}

				treeNode = treeNode.getNext();

				List<T> putList = Collections.unmodifiableList(conditionalPattern);
				if (conditionalPattern.size() > 0)
					conditionalPatternBase.put(putList,
							supportConditionalPattern);

			}

			// Is the item frequent? (count >= minSupport)
			if (frequentItemsetCount < minSupport * totalTransactionWeight) {
				// Skip the current item
//				logger.trace(() -> "Refused Item Set:  " + currentPattern + " - " +
//						frequentItemsetCount);
				continue;
			} else {
				//TODO remove because it is always evaluated!
				//logger.trace("New Item Set:  " + currentPattern.toString() + " - " +
				//		frequentItemsetCount);
				frequentPatterns.add(new WeightedFrequentPattern<T>(currentPattern,
						frequentItemsetCount, (double) frequentItemsetCount
								/ this.totalTransactionWeight));
			}

			logger.trace("ConditionalPatternBase: " + conditionalPatternBase);

			// 2. Step: Conditional FP-Tree
			Map<T, Double> conditionalItemFrequencies = new HashMap<>();
			WeightedFPtree<T> conditionalTree = new WeightedFPtree<T>(null, null);
			conditionalTree.setRoot(Boolean.TRUE);

			for (List<T> conditionalPattern : conditionalPatternBase.keySet()) {
				
				for(T conditionalToken : conditionalPattern) {
					if (conditionalItemFrequencies
							.containsKey(conditionalToken)) {
						Double count = conditionalItemFrequencies
								.get(conditionalToken);
						count += conditionalPatternBase.get(conditionalPattern);
						conditionalItemFrequencies.put(conditionalToken, count);
					} else {
						conditionalItemFrequencies.put(conditionalToken,
								conditionalPatternBase.get(conditionalPattern));
					}
				}
			}

			// Remove not frequent nodes
			Map<T, Double> tmp = new HashMap<>(conditionalItemFrequencies);
			for (T s : tmp.keySet())
				if (conditionalItemFrequencies.get(s) < minSupport * totalTransactionWeight)
					conditionalItemFrequencies.remove(s);

			logger.trace("ConditionalItemFrequencies: " + 
					conditionalItemFrequencies);

			// Construct Conditional FPTree
			HashMap<T, WeightedFPtree<T>> conditionalHeaderTable = new HashMap<>();
			for (List<T> conditionalPattern : conditionalPatternBase.keySet()) {
				List<T> path = new ArrayList<>();
				List<Double> pathValues = new ArrayList<>();
				
				for(T conditionalToken : conditionalPattern) {
					if (conditionalItemFrequencies
							.containsKey(conditionalToken)) {
						path.add(conditionalToken);
						pathValues.add(conditionalPatternBase
								.get(conditionalPattern));

					}
				}

				if (path.size() > 0) {
					insertFPTree(conditionalTree, path, pathValues,
							conditionalHeaderTable);
				}

			}

			logger.trace("End Mining Rules for "  + currentPattern);

			if (!conditionalTree.getChildren().isEmpty())
				fpGrowthStep(conditionalHeaderTable,
						frequentPatterns, currentPattern, maxItemsets);
		}
	}

}
