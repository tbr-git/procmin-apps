package de.rwth.processmining.tb.core.sps.data.hfddgraph;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.sps.algorithm.WeightedActivitySetTransactionsBuilder;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.FISResult;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedFrequentPattern;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedTransactionDataSourceImpl;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.apriori.APrioriTIDWeightedInterruptable;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.fpgrowth.WeightedFPGrowth;

public class HFDDGraphBuilderTimeLimited extends HFDDGraphBuilder {

	private static final Logger logger = LogManager.getLogger(HFDDGraphBuilderTimeLimited.class);
	
	/**
	 * Frequent activity itemset mining time.
	 */
	private Integer freqActMiningTimeMs = 20000;

	/**
	 * Target itemset number.
	 */
	private Integer targetActISNbr = 1000;

	/**
	 * Target itemset margin.
	 */
	private Double targetActISMargin = 0.1; 
	
	/**
	 * If there are many frequently occurring activities, we won't be able to 
	 * mine all frequent activity sets event for high minimum support values.
	 * 
	 * Since FP-growth is a depth first search kind of algorithm, the currently best result
	 * will likely only cover a subsets of all activities. 
	 * 
	 *  Therefore, a backup solution is computed using this support threshold in a breadth first search manner
	 *  (i.e., A-priori). 
	 *  It collects all small frequent activity itemsets having at least this support until we reach
	 *  the target number of vertices ({@link #targetActISNbr).
	 */
	private double MIN_SUPPORT_APRIORI_BACKUP = 0.005;
	
	/**
	 * If we can find a complete result with FP-growth and a threshold larger than this,
	 * still resort to the Apriori solution.
	 * => Rather use small (e.g., size 2) itemsets than 
	 * ignoring activities that occur in less than 1% of all cases 
	 */
	private double MIN_SUPPORT_PREFER_APRIORI = 0.01;
	

	@Override
	protected Collection<WeightedFrequentPattern<Integer>> mineActivityItemsets(
			BiComparisonDataSource<? extends CVariant> dataSource) throws SLDSTransformationError {
	  logger.info("Discovering activity itemsets: #TargetItemsets={}, #TargetMargin={}, MaxTime={}ms", 
	      targetActISNbr, targetActISMargin, freqActMiningTimeMs);
	  
	  //////////////////////////////
	  // Multi Round procedure
	  // (Currently not done for simlicity) Try FP-growth, if nbr itemsets exceeds threshold
	  // 1. Run Apriori (to get BFS small frequent itemsets as backup solution) 
	  // 2. If time left: Try to slighly increase threshold using FP-Growth-backed bisection serach
	  //////////////////////////////
    ////////////////////
	  // Init
    ////////////////////
		// Build transactions
	  logger.info("Initializing activity itemset discovery");
		WeightedTransactionDataSourceImpl<Integer> FISTransactions = 
				WeightedActivitySetTransactionsBuilder.buildFromLog(dataSource);
	  // Keep track of start
	  long startApriori = System.currentTimeMillis();
	  logger.info("Running restricted (time/size) search");
    ////////////////////
	  // First Round: Apriori backup
    ////////////////////
		APrioriTIDWeightedInterruptable<Integer> algApriori = new APrioriTIDWeightedInterruptable<>();
    Optional<FISResult<Integer>> resApriori = Optional.empty();
		try {
      resApriori = algApriori.findFrequentPattern(FISTransactions, MIN_SUPPORT_APRIORI_BACKUP, 
          freqActMiningTimeMs, targetActISNbr).get();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
	  long endApriori = System.currentTimeMillis();
	  
	  // Logging
	  // Don't yet stop, maybe we can even find complete itemsets using smaller support!
	  if (resApriori.isPresent()) {
      logger.info("Found {} itemsets using interruptable apriori and minSupport={}", 
          resApriori.get().frequentPatterns().size(), MIN_SUPPORT_APRIORI_BACKUP);
	  }

		WeightedFPGrowth<Integer> algFPG = new WeightedFPGrowth<>((i1, i2) -> Integer.compare(i1, i2));
		Optional<FISResult<Integer>> resFPG = Optional.empty();
		try {
		  // Respect remaining time
		  int remainingTime = this.freqActMiningTimeMs - (int) (endApriori - startApriori);
			resFPG = algFPG.findFrequentPattern(FISTransactions, remainingTime, 
					this.targetActISNbr, this.targetActISMargin).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    //////////////////////////////
		// Decide which results to return
    //////////////////////////////
		if (resApriori.isPresent() && resFPG.isPresent()) {
		  // Itemsets found by FP-growth not complete
		  // => Return Apriori results
		  if (!resFPG.get().resultComplete()) {
		    logger.info("Using itemsets found with Apriori methd, FP-Growth results were incomplete");
		    return resApriori.get().frequentPatterns();
		  }
		  if (resFPG.get().minSupport() > MIN_SUPPORT_PREFER_APRIORI) {
		    logger.info("Using itemsets found with Apriori methd, FP-Growth results were complete but required {} min support!", resFPG.get().minSupport());
		    return resApriori.get().frequentPatterns();
		  }
      logger.info("Use itemsets found with FP-Grwoth");
      return resFPG.get().frequentPatterns();

		}
		else if (resApriori.isPresent()) {
      logger.info("FP-growth failed, use itemsets found with Apriori method");
		  return resApriori.get().frequentPatterns();
		}
		else if (resFPG.isPresent()) {
      logger.info("Apriori failed, use itemsets found with FP-Grwoth");
      return resFPG.get().frequentPatterns();
		}
		else {
		  // TODO Damn, we have problem
		  return null;
		}
	}

	public Integer getFreqActMiningTimeMs() {
		return freqActMiningTimeMs;
	}


	public HFDDGraphBuilderTimeLimited setFreqActMiningTimeMs(Integer freqActMiningTimeMs) {
		this.freqActMiningTimeMs = freqActMiningTimeMs;
		return this;
	}


	public Integer getTargetActISNbr() {
		return targetActISNbr;
	}


	public HFDDGraphBuilderTimeLimited setTargetActISNbr(Integer targetActISNbr) {
		this.targetActISNbr = targetActISNbr;
		return this;
	}
	
  public HFDDGraphBuilderTimeLimited setBFSInitMinSupport(double minSupport) {
    this.MIN_SUPPORT_APRIORI_BACKUP = minSupport;
    return this;
  }	

  public HFDDGraphBuilderTimeLimited setPreferBFSSupport(double support) {
    this.MIN_SUPPORT_PREFER_APRIORI = support;
    return this;
  }	


	public Double getTargetActISMargin() {
		return targetActISMargin;
	}


	public HFDDGraphBuilderTimeLimited setTargetActISMargin(Double targetActISMargin) {
		this.targetActISMargin = targetActISMargin;
		return this;
	}
	
}
