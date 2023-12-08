package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.util;

import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.StochasticLangDataSourceTransformer;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;

/**
 * Cache that can be inserted into a {@code StochasticLanguageDataSource} transformation.
 * 
 * @author brockhoff
 *
 * @param <E> Encapsulated variant type
 */
public class StochLangDataSourceCache<E extends CVariant> extends StochasticLangDataSourceTransformer<E> {
	
	private XLog cachedXLog;
	
	private CVariantLog<E> cachedVariantLog;

	public StochLangDataSourceCache(StochasticLanguageDataSource<E> stochLangDataSource) {
		super(stochLangDataSource);

		cachedXLog = null; 
		cachedVariantLog = null;
	}

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError { 
		// Cache hit
		if(cachedXLog != null) {
			return cachedXLog;
		}
		cachedXLog = super.getDataRawTransformed();
		return cachedXLog;
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError { 
		// Cache hit
		if(cachedVariantLog != null) {
			return cachedVariantLog;
		}
		// Cache data and return
		cachedVariantLog = super.getVariantLog();
		return cachedVariantLog;
	}

	@Override
	public void clearOldCaches(PipeBackPropInfo propInfo) {
		// Clear if there is a more recent cache
		if(propInfo.foundNewCache) {
			cachedXLog = null;
			cachedVariantLog = null;
		}
		else {
			propInfo.foundNewCache = true;
		}
		super.clearOldCaches(propInfo);
	}

	@Override
	public void clearCaches() {
		super.clearCaches();
		cachedXLog = null;
		cachedVariantLog = null;
	}

}
