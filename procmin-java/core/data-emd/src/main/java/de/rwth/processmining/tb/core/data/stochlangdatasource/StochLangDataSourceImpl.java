package de.rwth.processmining.tb.core.data.stochlangdatasource;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.util.PipeBackPropInfo;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantKeys;
import de.rwth.processmining.tb.core.data.variantlog.util.LogBuildingException;

/**
 * Basic {@code StochasticLanguageDataSource} implementation that wraps an event log 
 * and abstracts/summarizes the contained data by means of categorical-classifier-complete variants.
 * 
 * @author brockhoff
 *
 */
public class StochLangDataSourceImpl<E extends CVariant> implements StochasticLanguageDataSource<E> {

	/**
	 * The wrapped event log
	 */
	private final XLog log;
	
	private final CVariantLog<E> variants;
	
	private final CVariantLogFactory<E> factory;
	
	public StochLangDataSourceImpl(XLog log, CVariantLogFactory<E> variantLogFactory) throws LogBuildingException {
		super();
		this.log = log;

		// Variant log is always populated and will never be cleared
		// (e.g., by cache clearing)
		this.factory = variantLogFactory;
		factory.setLog(this.log);
		variants = factory.build();
	}
	
	public StochLangDataSourceImpl(XLog log, CVariantLog<E> variantLog, 
			CVariantLogFactory<E> variantLogFactory) {
		this.log = log;
		this.factory = variantLogFactory;
		this.variants = variantLog;
	}

	@Override
	public XLog getDataRaw() {
		return log;
	}

	@Override
	public XLog getDataRawTransformed() {
		return log;
	}

	@Override
	public CVariantLog<E> getVariantLog() {
		return variants;
	}

	@Override
	public void clearOldCaches(PipeBackPropInfo propInfo) {
		// Do nothing
	}

	@Override
	public void clearCaches() {
		// Do nothing
	}

	@Override
	public XEventClassifier getClassifier() {
		return factory.getClassifier();
	}

	@Override
	public CVariantLogFactory<E> getVariantLogFactory() {
		return factory;
	}

	@Override
	public VariantKeys getVariantProperties() {
		return variants.getVariantKey();
	}

}
