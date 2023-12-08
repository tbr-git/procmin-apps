package de.rwth.processmining.tb.core.data.stochlangdatasource.transform;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public abstract class SLDSAbstractTransformerFactory<E extends CVariant> implements SLDSTransformerFactory<E> {

	/**
	 * Parent data source.
	 */
	protected StochasticLanguageDataSource<E> parentDataSource;


	public SLDSAbstractTransformerFactory() {
		parentDataSource = null;
	}
	
	@Override
	public SLDSAbstractTransformerFactory<E> setParentDataSource(StochasticLanguageDataSource<E> parentDataSource) {
		this.parentDataSource = parentDataSource;
		return this;
	}
}
