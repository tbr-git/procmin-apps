package de.rwth.processmining.tb.core.data.statistics;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;

public class ActivityOccurencePosition {
	private final static Logger logger = LogManager.getLogger( ActivityOccurencePosition.class );
	
	/**
	 * Map activity names to their average first occurrence position
	 */
	private Map<String, Double> avgFirstPosition;
	
	/**
	 * Map activity codes to their average first occurrence position
	 */
	private TIntDoubleHashMap avgFirstPositionCat;

	public ActivityOccurencePosition() {
		avgFirstPosition = new HashMap<>();
    avgFirstPositionCat = new TIntDoubleHashMap();
	}

	public static ActivityOccurencePosition getActOccurenceStatistics(CVariantLog<? extends CVariant> log) {
		return getActOccurenceStatistics(Collections.singleton(log));

	}
	/**
	 * Factory method that builds the statistics from a number of logs.
	 * 
	 * <b> Important: </b> The method assumes that the logs have the same category-to-activity mappings 
	 * (when restricted to the occurring activities!!!)
	 * @param logs
	 * @return
	 */
	public static ActivityOccurencePosition getActOccurenceStatistics(Collection<CVariantLog<? extends CVariant>> logs) {
		TIntDoubleHashMap cat2WeightedPositionSum = new TIntDoubleHashMap();
		TIntDoubleHashMap cat2TotalWeight = new TIntDoubleHashMap();
		TIntObjectHashMap<String> cat2Activity = new TIntObjectHashMap<>();

		// Fill the statistics maps
		logs.forEach(l-> fillAggregates(cat2WeightedPositionSum, cat2TotalWeight, cat2Activity, l));
		
		ActivityOccurencePosition inst = new ActivityOccurencePosition();
		// Iterate over the collected total positions
		TIntDoubleIterator itPos = cat2WeightedPositionSum.iterator();
		while(itPos.hasNext()) {
			itPos.advance();
			// Category
			int c = itPos.key();
			// First occurrence position sum
			double p = itPos.value();
			// Weight sum (e.g., total support)
			double w = cat2TotalWeight.get(c);
			
			// Add average position of first occurrence
			inst.addActivity(cat2Activity.get(c), p / w);
			inst.addActivity(c, p / w);
		}
		return inst;
	}
	
	/**
	 * Iterates through the log and fills the following aggregated statistics:
	 * @param cat2WeightedPositionSum Position of first occurrence sum for each category
	 * @param cat2TotalWeight Total variant weight for each category
	 * @param cat2Activity Category to activity mapping (keeps the first mapping that is inserted)
	 * @param log Log to iterate over
	 */
	private static void fillAggregates(TIntDoubleHashMap cat2WeightedPositionSum, TIntDoubleHashMap cat2TotalWeight, 
			TIntObjectHashMap<String> cat2Activity, CVariantLog<? extends CVariant> log) {
		if(log.getMaxCategoryCode() > 5 * 54) {
			logger.warn("High maxium category. Maybe using bitsets is not the opitmal choice");
		}
		// Keep track if we have already seen this category
		// Only count positions of first occurrences
		BitSet occuredInVariant = new BitSet(log.getMaxCategoryCode());
		for(CVariant v : log) {
			int p = 0;
			Iterator<Integer> itCat = v.iteratorVariantCategorical();
			while(itCat.hasNext()) {
				int c = itCat.next();
				// Not occurred yet
				if(!occuredInVariant.get(c)) {
					cat2WeightedPositionSum.adjustOrPutValue(c, v.getSupport() * p, v.getSupport() * p);
					cat2TotalWeight.adjustOrPutValue(c, v.getSupport(), v.getSupport());
					occuredInVariant.set(c);
				}
				p++;
			}
			occuredInVariant.clear();
		}
		// Save categories
		cat2WeightedPositionSum.forEachKey(new TIntProcedure() {
			
			@Override
			public boolean execute(int category) {
				// No mapping saved yet
				if(!cat2Activity.contains(category)) {
					// Is there a valid (non-zero) mapping?
					if(log.getActivity4Category(category) != null) {
						cat2Activity.put(category, log.getActivity4Category(category));
					}
					else {
						logger.error("Encountered category {} which is not in any category-to-activity mapping", category);
					}
				}
				return true;
			}
		});
		
	}
	
	/**
	 * Add statistics for a given activity
	 * @param activity Activity key.
	 * @param position Position value.
	 */
	public void addActivity(String activity, double position) {
		this.avgFirstPosition.put(activity, position);
	}

	/**
	 * Add statistics for a given activity
	 * @param activity Activity key.
	 * @param position Position value.
	 */
	public void addActivity(int activityCat, double position) {
		this.avgFirstPositionCat.put(activityCat, position);
	}

	public Map<String, Double> getAvgPosition() {
		return avgFirstPosition;
	}
	
	public Double getAvgFirstPosition(String activity) {
	  return avgFirstPosition.get(activity);
	}

	public Double getAvgFirstPosition(int activity) {
	  return avgFirstPositionCat.get(activity);
	}

}
