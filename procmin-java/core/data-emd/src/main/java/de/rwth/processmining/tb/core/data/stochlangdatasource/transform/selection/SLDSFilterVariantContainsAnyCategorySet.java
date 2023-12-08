package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.XEventCondition;
import org.processmining.plugins.log.logfilters.XTraceCondition;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.StochasticLangDataSourceTransformer;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.data.variantlog.transform.CVariantCondition;
import de.rwth.processmining.tb.core.data.variantlog.transform.ContainsAllCategoriesCondition;
import de.rwth.processmining.tb.core.data.variantlog.util.VariantCopyingException;

/**
 * Decorator for a stochastic language data source that filters the variants.
 * A variant is kept/dropped if it fully contains any set of categories of the provided collection of category sets.
 * 
 * @author brockhoff
 *
 * @param <E> Type of the variant
 */
public class SLDSFilterVariantContainsAnyCategorySet<E extends CVariant> extends StochasticLangDataSourceTransformer<E> {
  
	private final static Logger logger = LogManager.getLogger( SLDSFilterVariantContainsAnyCategorySet.class );
  
	/**
	 * Check if any of theses category sets (i.e., any row) is fully contained.
	 * Based on {@link this#keep}, it is either kept or dropped.
	 */
	private int[][] activityCodes;

	/**
	 * Used event classifier.
	 */
	private XEventClassifier classifier;
	
	/**
	 * Category mapper required when using 
	 */
	private CategoryMapper categoryMapper;

	/**
	 * Keep the traces that contain all activities (true) or drop them (false).
	 * Defaults to true
	 */
	private final boolean keep;
	
	/**
	 * Constructor
	 * @param stochLangDataSource Data to filter
	 * @param activities "Table" of categories. Each row corresponds to a set of categories.
	 * @param classifier Event classifier
	 * @param categoryMapper Category mapper
	 * @param keep Keep (true) if filter condition is true or drop (false)
	 */
	public SLDSFilterVariantContainsAnyCategorySet(StochasticLanguageDataSource<E> stochLangDataSource, 
			int[][] activities, XEventClassifier classifier, CategoryMapper categoryMapper, boolean keep) {
		super(stochLangDataSource);
		this.activityCodes = activities;
		this.classifier = classifier;
		this.categoryMapper = categoryMapper;
		this.keep = keep;
	}

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
	  // For each condition: category id set
	  final List<Set<Integer>> activityIdSets = new LinkedList<>();
		for(int[] s : activityCodes) {
		  Set<Integer> activities = new HashSet<>();
		  for (int a : s) {
        activities.add(a);
		  }
		  activityIdSets.add(activities);
		}

		XLog log = super.getDataRawTransformed();
		// Depending on whether we want to keep the traces
		log = LogFilter.filter(null, 100, log, null,
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						// Keep all events
						return true;
					}
				},
				new XTraceCondition() {
			
					@Override
					public boolean keepTrace(XTrace trace) {
					  // Iterate over condition activity sets
					  for (Set<Integer> conditionCategories : activityIdSets) {
              HashSet<Integer> containedCat = new HashSet<>();
              for(XEvent e : trace) {
                Integer c = categoryMapper.getCategory4Activity(classifier.getClassIdentity(e));
                if(c != null && conditionCategories.contains(c)) {
                  containedCat.add(c);
                }
              }
              if (conditionCategories.size() == conditionCategories.size()) {
                return keep;
              }
					  }
					  return !keep;
					}
				}
			);
		return log;
	}
	
	
	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		CVariantLog<E> log = super.getVariantLog();

		// Filtering on activities that are not even in the log
		// => All traces match the condition
		if(activityCodes.length == 0) {
			if(keep) {
				return log.getEmptyCopy();
			}
			else {
				return log;
			}
		}
		else {
		  // Is any of the category sets is empty?
		  for (int[] s : activityCodes) {
		    if (s.length == 0) {
		      // If the set is empty, it is contained
          if(keep) {
            return log;
          }
          else {
            return log.getEmptyCopy();
          }
		    }
		  }
		  
      ////////////////////
		  // Non-empty category sets
      ////////////////////
		  List<CVariantCondition<E>> containmentConditions = new LinkedList<>();
		  for (int[] s: activityCodes) {
        CVariantCondition<E> containsActivitiesCond = new ContainsAllCategoriesCondition<>(s);
        containmentConditions.add(containsActivitiesCond);
		  }
		  
		  
			try {
				log = log.filterVariantByConditionAny(containmentConditions, keep);
			}
			catch(VariantCopyingException e) {
				logger.error("Error in Variant condition filtering", e);
				throw new SLDSTransformationError("Transformation failed: "  + e.getMessage());
			}
			return log;
		}
	}
	
	protected static<T extends CVariant> CVariantLog<T> applyCategoryFilter(CVariantLog<T> log, final int[] categories, 
			final boolean keep) throws SLDSTransformationError {
		// Filtering on activities that are not even in the log
		// => All traces match the condition
		if(categories.length == 0) {
			if(keep) {
				return log;
			}
			else {
				return log.getEmptyCopy();
			}
		}
		else {
			CVariantCondition<T> containsActivitiesCond = new ContainsAllCategoriesCondition<>(categories);
			try {
				log = log.filterVariantByCondition(containsActivitiesCond, keep);
			}
			catch(VariantCopyingException e) {
				logger.error("Error in Variant condition filtering", e);
				throw new SLDSTransformationError("Transformation failed: "  + e.getMessage());
			}
			return log;
		}
	}
}
