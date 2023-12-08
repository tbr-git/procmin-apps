package de.rwth.processmining.tb.core.data.stochlangdatasource.transform;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.util.PipeBackPropInfo;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantKeys;

/**
 * Abstract {@code StochasticLanguageDataSource} transformation decorator.
 * @author brockhoff
 *
 * @param <E> Encapsulated variant type
 */
public abstract class StochasticLangDataSourceTransformer<E extends CVariant> implements StochasticLanguageDataSource<E> {
	
	/**
	 * Handle to the decorated {@code StochasticLanguageDataSource}.
	 */
	private StochasticLanguageDataSource<E> stochLangDataSource;

	/**
	 * 
	 * @param stochLangDataSource Handle to the decorated {@code StochasticLanguageDataSource}.
	 */
	public StochasticLangDataSourceTransformer(StochasticLanguageDataSource<E> stochLangDataSource) {
		super();
		this.stochLangDataSource = stochLangDataSource;
	}

	@Override
	public XLog getDataRaw() {
		return stochLangDataSource.getDataRaw();
	}

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		return stochLangDataSource.getDataRawTransformed();
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		return stochLangDataSource.getVariantLog();
	}

	@Override
	public void clearOldCaches(PipeBackPropInfo propInfo) {
		// Do nothing and propagate back
		stochLangDataSource.clearOldCaches(propInfo);
	}

	@Override
	public void clearCaches() {
		// Do nothing and propagate back
		stochLangDataSource.clearCaches();
	}

	@Override
	public XEventClassifier getClassifier() {
		return stochLangDataSource.getClassifier();
	}

	@Override
	public CVariantLogFactory<E> getVariantLogFactory() {
		return stochLangDataSource.getVariantLogFactory();
	}

	@Override
	public VariantKeys getVariantProperties() {
		return stochLangDataSource.getVariantProperties();
	}
	
}
