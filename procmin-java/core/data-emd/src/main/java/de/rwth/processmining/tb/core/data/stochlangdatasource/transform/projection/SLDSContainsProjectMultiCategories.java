package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.projection;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.StochasticLangDataSourceTransformer;
import de.rwth.processmining.tb.core.data.variantlog.base.CCCVariantImpl;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.data.variantlog.transform.CVariantTransformer;
import de.rwth.processmining.tb.core.data.variantlog.transform.ExtractMultiCategoriesStrict;


/**
 * Filter the data based on multiple activity itemsets.
 * If an itemset is fully contained, also project on it.
 */
public class SLDSContainsProjectMultiCategories<E extends CVariant> extends StochasticLangDataSourceTransformer<E> {

	/**
	 * List of activity code sets. 
	 * If a set is fully contained, we project on it
	 */
	private final List<BitSet> activities;
	
	/**
	 * Mapping from category codes to activity labels.
	 */
	private final CategoryMapper categoryMapper;
	
	/**
	 * Invert the keep index mechanism 
	 */
	private final boolean complementProjection;
	

	public SLDSContainsProjectMultiCategories(StochasticLanguageDataSource<E> stochLangDataSource, 
			List<BitSet> activities, CategoryMapper categoryMapper, boolean complementProjection) {
		super(stochLangDataSource);
		this.activities = activities;
		this.categoryMapper = categoryMapper;
		this.complementProjection = complementProjection;
	}

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		XLog xlog = super.getDataRawTransformed();

		// Code partly copied and adapted from
		// org.processmining.plugins.log.logfilters.LogFilter
		Iterator<XTrace> itTrace = xlog.iterator();
		
		// TODO cash variants
		while (itTrace.hasNext()) {
			XTrace trace = itTrace.next();
			
      // Create pseudo variant
			int[] categoricalTrace = new int[trace.size()];
			int i = 0;
			for (XEvent e : trace) {
			  int c = categoryMapper.getCategory4Activity(super.getClassifier().getClassIdentity(e));
			  categoricalTrace[i] = c;
			  i++;
			}
			CCCVariantImpl proxyVariant = new CCCVariantImpl(categoricalTrace, 1);
			BitSet indices = ExtractMultiCategoriesStrict.getProjectionIndices(proxyVariant, activities);
			if (this.complementProjection) {
			  indices.flip(0, indices.size());
			}
			
			// Keep indices extract from proxy variant (see LogFilter)
			Iterator<XEvent> itEvent = trace.iterator();
			i = 0;
			while (itEvent.hasNext()) {
			  itEvent.next();
			  if (!indices.get(i)) {
			    itEvent.remove();
			  }
			  i++;
			}

			// Remove empty traces
			if (trace.isEmpty()) {
				itTrace.remove();
			}
		}
		return xlog;
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
	  CVariantLog<E> log = super.getVariantLog();
	  
	  CVariantTransformer<E> transfomer = new ExtractMultiCategoriesStrict<>(activities, this.complementProjection);

	  return log.applyVariantTransformer(transfomer, false);
	}

}

