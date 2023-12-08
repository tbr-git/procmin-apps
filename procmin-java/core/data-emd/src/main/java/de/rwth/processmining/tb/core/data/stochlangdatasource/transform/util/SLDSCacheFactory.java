package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public class SLDSCacheFactory<E extends CVariant> extends SLDSAbstractTransformerFactory<E> {
	private final static Logger logger = LogManager.getLogger( SLDSCacheFactory.class );

	public SLDSCacheFactory() {
		super();
	}

	@Override
	public StochasticLanguageDataSource<E> build() {
		if(this.parentDataSource == null) {
			logger.error("Missing parent data source.");
		}
		return new StochLangDataSourceCache<>(this.parentDataSource);
	}

}
