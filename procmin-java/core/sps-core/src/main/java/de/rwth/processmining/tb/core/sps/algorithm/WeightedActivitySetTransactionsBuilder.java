package de.rwth.processmining.tb.core.sps.algorithm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.google.common.collect.Iterables;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedTransaction;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.WeightedTransactionDataSourceImpl;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

public class WeightedActivitySetTransactionsBuilder {
	private final static Logger logger = LogManager.getLogger( WeightedActivitySetTransactionsBuilder.class );
	
	// TODO Important!!! -> Currently only works for at most 64 event classes
	public static WeightedTransactionDataSourceImpl<String> buildFromLog(XLog xlog, XEventClassifier eventClassifier) {
		
		Map<String, Integer> class2Index = new HashMap<>(64);
		Map<Integer, Integer> itemsetCount = new HashMap<>();
		Map<Integer, XTrace> traceResprentatives = new HashMap<>();
	
		int shift;
		int c;
		for(XTrace t : xlog) {
			int traceBits = 0;
			for(XEvent e : t) {
				String eventClass = eventClassifier.getClassIdentity(e);

				if(class2Index.containsKey(eventClass)) {
					shift = class2Index.get(eventClass);
				}
				else {
					shift = class2Index.size();
					if(shift > 64) {
						throw new RuntimeException("Cannot build weighted activity itemset database for a "
								+ "log with more than 64 event classes!.");
					}
					class2Index.put(eventClass, shift);
				}
				traceBits |= (1 << shift);
			}
			
			c = 1;
			if(itemsetCount.containsKey(traceBits)) {
				c = itemsetCount.get(traceBits) + 1;
			}
			else {
				traceResprentatives.put(traceBits, t);
			}
			itemsetCount.put(traceBits, c);
		}
		
		List<WeightedTransaction<String>> lWeightedActTransactions = new LinkedList<>();
		for(int k : itemsetCount.keySet()) {
			XTrace t = (XTrace) traceResprentatives.get(k);
			List<String> activities = new LinkedList<>();
			for(XEvent e : t) {
				activities.add(eventClassifier.getClassIdentity(e));
			}
			double w = itemsetCount.get(k) / ((double) xlog.size());
			lWeightedActTransactions.add(new WeightedTransaction<>(activities, w));
		}
		
		return new WeightedTransactionDataSourceImpl<String>(lWeightedActTransactions);
	}
	
	
	public static<T extends CVariant> WeightedTransactionDataSourceImpl<Integer> buildFromLog(BiComparisonDataSource<T> log) {
		
		//////////////////////////////
		// Count Unique Variants
		//////////////////////////////
		TObjectIntHashMap<CVariant> variantSupport = null;
		try {
			 variantSupport = new TObjectIntHashMap<>(
					2 * (log.getDataSourceLeft().getVariantLog().nbrVariants() 
							+ log.getDataSourceRight().getVariantLog().nbrVariants()));
			// Run through both logs and add variants
			for(CVariant v : Iterables.concat(log.getDataSourceLeft().getVariantLog(), log.getDataSourceRight().getVariantLog())) {
				// Category list
				variantSupport.adjustOrPutValue(v, v.getSupport(), v.getSupport());
			}
		} catch (SLDSTransformationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		//////////////////////////////
		// Create Transactions
		//////////////////////////////
		List<WeightedTransaction<Integer>> lWeightedCatTransactions = new LinkedList<>();
		variantSupport.forEachEntry(new TObjectIntProcedure<CVariant>() {

			@Override
			public boolean execute(CVariant v, int support) {
				List<Integer> categories = Arrays.stream(v.getTraceCategories()).boxed().collect(Collectors.toList()); 
				// Constructor will ensure uniqueness
				WeightedTransaction<Integer> t = new WeightedTransaction<Integer>(categories, support);
				lWeightedCatTransactions.add(t);
				return true;
			}
		});
		logger.debug("Created transaction dataset with " + lWeightedCatTransactions.size() + " disjoint transactions");
		
		return new WeightedTransactionDataSourceImpl<Integer>(lWeightedCatTransactions);
	}
}
