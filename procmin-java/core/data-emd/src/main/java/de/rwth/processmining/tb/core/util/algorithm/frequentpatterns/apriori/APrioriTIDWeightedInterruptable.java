package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.apriori;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.FISResult;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedFrequentPattern;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedTransaction;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedTransactionDataSource;
import de.rwth.processmining.tb.core.util.backgroundwork.CachedBackgroundTaskService;

/**
 * Implementation of frequent itemsets mining on float-weighted transactions using the a-priori method.
 * <p>
 * This implementation is largely copied from the SPMF library's 
 * <a href="https://github.com/pommedeterresautee/spmf/blob/master/ca/pfv/spmf/algorithms/frequentpatterns/aprioriTID/AlgoAprioriTID_Bitset.java">implementation</a>. 
 * However, this implementation is extended as follows:
 * <p>
 * <ul>
 * <li>Each transaction has a weight (floating precision)</li>
 * <li>Mining can time out</li>
 * <li>Mining stops if a certain number of frequent itemsets is found</li>
 * <li>Items are generics</li>
 * </ul>
 * 
 * @author brockhoff
 */
public class APrioriTIDWeightedInterruptable<T extends Comparable<T>> {
	private static final Logger logger = LogManager.getLogger(APrioriTIDWeightedInterruptable.class);

  /**
   * The current level
   */
	protected int k; 

	/**
	 * Variables for counting support of items
	 */
	ArrayList<BitSet> mapItemTIDS;
	
	/**
	 * Transaction weights
	 */
	private double[] tWeights;

	/**
	 * The minimum support threshold
	 */
	private double minSuppRelative;

	/**
	 * Special parameter to restrict the maximum number of BFS itemsets to be found
	 */
	private int maxItemsetsLargeK = Integer.MAX_VALUE;

	/**
	 * Start time of execution
	 */
	private long startTimestamp = 0;

	/**
	 * End time of execution
	 */
	private long endTimeStamp = 0;
	
	/**
	 * The number of frequent itemsets found
	 */
	private int itemsetCount;
	
	/**
	 * Number of items
	 */
	private int tidcount = 0;
	
	/**
	 * Total transaction weight of the data source.
	 */
	private double totalTransactionWeight;
	
	/**
	 * Map item to id
	 */
	
	/**
	 * Map id to item
	 */
	private ArrayList<T> id2Item;
	
	/**
	 * Result frequent itemset collection.
	 */
	private Collection<APrioriTIDItemset> frequentItemsets;

	/**
	 * Default constructor
	 */
	public APrioriTIDWeightedInterruptable() {
	}
	
	public CompletableFuture<Optional<FISResult<T>>> findFrequentPattern(
			WeightedTransactionDataSource<T> dataSource, double minSupport, int maxMiningTimeInMs, int targetItemsetNbr) {

		CompletableFuture<Optional<FISResult<T>>> runPatternDiscovery =  CompletableFuture.supplyAsync(() -> {
			// We need a nested normal future
			// CompletableFutures can timeout but it is difficult to cancel the task that was
			// running when the timeout occurred. (By design)
			// Directly submitting a normal future has a stronger connection to the handling thread
			// and, therefore, we can flag an interruption by canceling the future.
			
			Future<Boolean> miningTask = CachedBackgroundTaskService.getInstance().submit(
					() -> runDiscoveryFrequentItemsets(minSupport, dataSource, targetItemsetNbr));
			
			boolean complete = false;
			try {
				// Only timeout if we want to
				if (maxMiningTimeInMs > 0) {
					complete = miningTask.get(maxMiningTimeInMs, TimeUnit.MILLISECONDS);
				}
				else {  
					// Block until completion
					complete = miningTask.get();
				}
			} catch (InterruptedException e) {
			  // If this get interrupted
			  return Optional.empty();
			} catch (ExecutionException e) {
			  return Optional.empty();
			} catch (TimeoutException e) {
				logger.info("Apriori - FIS Mining timed out. Returning intermediate results.");
				// In case of a timeout, CANCEL the mining task
				miningTask.cancel(true);
			}
			
      List<WeightedFrequentPattern<T>> resultItemsets = new LinkedList<>();
      for (APrioriTIDItemset aItemset : this.frequentItemsets) {
        List<T> items = new LinkedList<>();
        for (int item : aItemset.getItems()) {
          items.add(this.id2Item.get(item));
        }
        WeightedFrequentPattern<T> itemset = new WeightedFrequentPattern<>(items, aItemset.getSupport(), 
            aItemset.getSupport() / this.totalTransactionWeight);
        resultItemsets.add(itemset);
      }
      
      return Optional.of(new FISResult<>(resultItemsets, complete, minSupport));
			
		});
		
		return runPatternDiscovery;
	}

  public FISResult<T> discoverFrequentItemsets(Double minSupport, 
	    WeightedTransactionDataSource<T> dataSource,
	    int maxItemsetsLargeK) {
    boolean complete;
    try {
      complete = this.runDiscoveryFrequentItemsets(minSupport, dataSource, maxItemsetsLargeK);
    } catch (InterruptedException e) {
      complete = false;
    }

    List<WeightedFrequentPattern<T>> resultItemsets = new LinkedList<>();
    
    for (APrioriTIDItemset aItemset : this.frequentItemsets) {
      List<T> items = new LinkedList<>();
      for (int item : aItemset.getItems()) {
        items.add(this.id2Item.get(item));
      }
      WeightedFrequentPattern<T> itemset = new WeightedFrequentPattern<>(items, aItemset.getSupport(), 
          aItemset.getSupport() / this.totalTransactionWeight);
      resultItemsets.add(itemset);
    }
    
    return new FISResult<>(resultItemsets, complete, minSupport);
  }


	protected boolean runDiscoveryFrequentItemsets(Double minSupport, 
	    WeightedTransactionDataSource<T> dataSource,
	    int maxItemsetsLargeK)
			throws InterruptedException {
	  
    //////////////////////////////
	  // Reset
    //////////////////////////////
		frequentItemsets = new ArrayList<>();
		// record start time
		startTimestamp = System.currentTimeMillis();
		// reset number of itemsets found
		itemsetCount = 0;
		
		// initialize variable to count the number of transactions
		tidcount = 0;

		// Mapping of items to int
		mapItemTIDS = new ArrayList<>(); 
		// key : item   value: tidset of the item as a bitset
		id2Item = new ArrayList<>();
	  Map<T, Integer> item2Id = new HashMap<>();
		

    //////////////////////////////
		// Init
    //////////////////////////////
	  this.maxItemsetsLargeK = maxItemsetsLargeK;
	  int nbrItems = 0;
    ////////////////////
		// Parse and process data 
    ////////////////////
		ArrayList<Double> transactionWeights = new ArrayList<>();
		dataSource.reset();
		this.totalTransactionWeight = 0.0;
		while (dataSource.hasNext()) {
			WeightedTransaction<T> t = dataSource.next();
			this.totalTransactionWeight += t.getWeight();
			// Save transaction weight
			transactionWeights.add(t.getWeight());
			for (T item : t.getItems()) {
			  Integer id = item2Id.get(item);
        BitSet tids;
			  // Item not seen yet
			  if (id == null) {
			    item2Id.put(item, nbrItems);
			    id = nbrItems;
			    // Add item
			    id2Item.add(item);
			    // Add corresponding BitSet
					tids = new BitSet();
					// ids are assigned to items consecutively!
					// If we encounter a new item, its id will be its position in the list!
					mapItemTIDS.add(tids);

			    nbrItems++;

			  }
			  else {
          tids = mapItemTIDS.get(id);
			  }
				tids.set(tidcount);
			}
			// increase the transaction count
			tidcount++;
		}
		this.tWeights = transactionWeights.stream().mapToDouble(d -> d).toArray();

		// convert the support from a relative minimum support (%) to an 
		// "absolute" minimum support
		this.minSuppRelative = minSupport * totalTransactionWeight;

    //////////////////////////////
		// FIS Mining k = 1
    //////////////////////////////
		// To build level 1, we keep only the frequent items.
		// We scan the database one time to calculate the support of each
		// candidate.
		// Moreover, we remove infrequent items from consideration
		k = 1;
		List<APrioriTIDItemset> level = new ArrayList<>();
		// For each item
		int itemIdAll = 0;
		int itemIdFreq = 0;
		ListIterator<BitSet> itTIDs = mapItemTIDS.listIterator(0);
		// Cleaned mapping of items to int
		ArrayList<BitSet> mapItemTIDSCleaned = new ArrayList<>(); 
		// key : item   value: tidset of the item as a bitset
		ArrayList<T> id2ItemCleaned = new ArrayList<>();
		while (itTIDs.hasNext()) {
      BitSet tidSet = itTIDs.next();
			// get the weighted support count 
			double wSupport = getWeightedSupport(tidSet);
			// if the item is frequent
			if (wSupport >= minSuppRelative) { 
				// add the item to the set of frequent itemsets of size 1
				APrioriTIDItemset itemset = new APrioriTIDItemset(itemIdFreq, wSupport, tidSet);
				level.add(itemset);
				// Update cleaned
		    mapItemTIDSCleaned.add(tidSet);
        id2ItemCleaned.add(id2Item.get(itemIdAll));
        itemIdFreq++;
			} else {
				itTIDs.set(null); // if the item is not frequent we don't
				// need to keep it into memory.
			}
      itemIdAll++;
		}
		mapItemTIDS = mapItemTIDSCleaned;
		id2Item = id2ItemCleaned;

		// No check, level 1 is always fully contained
		// Assumption: There aren't ridiculously many activity classes
    this.itemsetCount += level.size();
		
		// Done processing the raw data.
		// Don't need to remember this.
		item2Id = null;

		// sort itemsets of size 1 according to lexicographical order.
		Collections.sort(level, new Comparator<APrioriTIDItemset>() {
			public int compare(APrioriTIDItemset o1, APrioriTIDItemset o2) {
				return o1.getItem(0) - o2.getItem(0);
			}
		});
		
    //////////////////////////////
		// FIS Mining k > 1
    //////////////////////////////
    frequentItemsets.addAll(level);

		// Generate candidates with size k = 1 (all itemsets of size 1)
		k = 2;
		// While the level is not empty
		while (!level.isEmpty()) {
			// We build the level k+1 with all the candidates that have
			// a support higher than the minsup threshold.
			level = generateCandidateSizeK(level, k-1);
			; // We keep only the last level...
			k++;
      // First check, level 1 + 2 are always fully contained
      // Assumption: There aren't ridiculously many activity classes
      if (this.itemsetCount > this.maxItemsetsLargeK) {
        endTimeStamp = System.currentTimeMillis();
        return false;
      }
		}
		
		// save end time
		endTimeStamp = System.currentTimeMillis();
		return true;
	}

	/**
	 * Method to generate itemsets of size k from frequent itemsets of size K-1.
	 * @param levelK_1  frequent itemsets of size k-1
	 * @return itemsets of size k
	 */
	protected List<APrioriTIDItemset> generateCandidateSizeK(List<APrioriTIDItemset> levelK_1, int k_1)
			throws InterruptedException  {
		// create a variable to store candidates
		List<APrioriTIDItemset> candidates = new ArrayList<>();

		// For each itemset I1 and I2 of level k-1
		loop1: for (int i = 0; i < levelK_1.size(); i++) {
			APrioriTIDItemset itemset1 = levelK_1.get(i);
			loop2: for (int j = i + 1; j < levelK_1.size(); j++) {
				APrioriTIDItemset itemset2 = levelK_1.get(j);
				
        // Interrupt on timeout
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }

				// we compare items of itemset1 and itemset2.
				// If they have all the same k-1 items and the last item of
				// itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a
				// candidate
				for (int k = 0; k < k_1; k++) {
					// if they are the last items
					if (k == k_1 - 1) {
						// the one from itemset1 should be smaller (lexical
						// order)
						// and different from the one of itemset2
						if (itemset1.getItem(k) >= itemset2.getItem(k)) {
							continue loop1;
						}
					}
					// if they are not the last items, and
					else if (itemset1.getItem(k) < itemset2.getItem(k)) {
						continue loop2; // we continue searching
					} else if (itemset1.getItem(k) > itemset2.getItem(k)) {
						continue loop1; // we stop searching: because of lexical
										// order
					}
				}

				// NOW COMBINE ITEMSET 1 AND ITEMSET 2
				// create list of common tids
				BitSet list = (BitSet) itemset1.getTransactionsIds().clone();
				list.and(itemset2.getTransactionsIds());

				double support;
				// Calculating support might be quite expensive, maybe we can re-use
				if (list.cardinality() == itemset1.getTransactionsIds().cardinality()) {
				  support = itemset1.getSupport();
				}
				else if (list.cardinality() == itemset2.getTransactionsIds().cardinality()) {
				  support = itemset2.getSupport();
				}
				else {
				  support = getWeightedSupport(list);
				}

				if (support >= minSuppRelative) {
					this.itemsetCount++;
          // Only check for levels >= 3
					// This one exceeds
          if (this.itemsetCount > this.maxItemsetsLargeK) {
            return candidates;
          }

					// Create a new candidate by combining itemset1 and itemset2
					int newItemset[] = new int[k_1 + 1];
					System.arraycopy(itemset1.getItems(), 0, newItemset, 0, k_1);
					newItemset[k_1] = itemset2.getItems()[k_1 - 1];

          APrioriTIDItemset candidate = new APrioriTIDItemset(newItemset, support, list);
					
					candidates.add(candidate);
          frequentItemsets.add(candidate);
				}
			}
		}
		return candidates;
	}

	private double getWeightedSupport(BitSet tidSet) {
	  double wSupport = 0;
     for (int i = tidSet.nextSetBit(0); i >= 0; i = tidSet.nextSetBit(i+1)) {
       wSupport += this.tWeights[i];
     }
     return wSupport;
	}

}
