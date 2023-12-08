package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import de.rwth.processmining.tb.core.data.variantlog.transform.CVariantCondition;
import de.rwth.processmining.tb.core.data.variantlog.util.VariantCopyingException;

/**
 * Apply a generic variant filter to the data source, that is, keep or remove all traces
 * that satisfy a given condition.
 * @author brockhoff
 *
 */
public class SLDSVariantConditionFilter<E extends CVariant> extends StochasticLangDataSourceTransformer<E> {
	private final static Logger logger = LogManager.getLogger( SLDSVariantConditionFilter.class );
	
	/**
	 * Variant condition condition 
	 * (Semantics should be equivalent to {@link this#variantCondition} 
	 */
	private final CVariantCondition<E> variantCondition; 

	/**
	 * Trace condition on an {@link XTrace}.
	 * (Semantics should be equivalent to {@link this#variantCondition}
	 */
	private final XTraceCondition xTraceCondition;
	
	/**
	 * If true, keep the variant. 
	 * If false, drop the variant.
	 */
	private final boolean keep;

	public SLDSVariantConditionFilter(StochasticLanguageDataSource<E> stochLangDataSource,
			CVariantCondition<E> variantCondition, XTraceCondition xTraceCondition, boolean keep) {
		super(stochLangDataSource);
		this.variantCondition = variantCondition;
		this.xTraceCondition = xTraceCondition;
		this.keep = keep;
	}
	
	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		XLog log = super.getDataRawTransformed();
		XTraceCondition wrapperCond;
		// Depending on whether we want to keep the traces
		if(keep) {
			wrapperCond = this.xTraceCondition;
		}
		else {
			// wrap in condition that negates the given condition
			wrapperCond = new XTraceCondition() {
				
				@Override
				public boolean keepTrace(XTrace trace) {
					return !xTraceCondition.keepTrace(trace);
				}
			};
		}
		log = LogFilter.filter(null, 100, log, null,
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						// Keep all events
						return true;
					}
				}, 
				wrapperCond
				);
		
		return log;
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		CVariantLog<E> log = super.getVariantLog();
		try {
			log = log.filterVariantByCondition(variantCondition, keep);
		}
		catch(VariantCopyingException e) {
			logger.error("Error in Variant condition filterin", e);
			throw new SLDSTransformationError("Transformation failed: "  + e.getMessage());
		}
		return log;
	}

}
