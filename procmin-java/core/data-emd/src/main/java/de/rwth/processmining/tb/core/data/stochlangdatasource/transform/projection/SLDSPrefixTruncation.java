package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.projection;

import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.StochasticLangDataSourceTransformer;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.transform.CVariantPrefixTruncator;
import de.rwth.processmining.tb.core.data.variantlog.transform.CVariantTransformer;

public class SLDSPrefixTruncation<E extends CVariant> extends StochasticLangDataSourceTransformer<E> {

	/**
	 * Length of the prefix.
	 */
	private final int prefixLength;
	
	public SLDSPrefixTruncation(StochasticLanguageDataSource<E> stochLangDataSource, int prefixLength) {
		super(stochLangDataSource);
		this.prefixLength = prefixLength;
	}

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		throw new RuntimeException("Not implemented XLog: prefix truncation");
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		CVariantLog<E> log = super.getVariantLog();
		CVariantTransformer<E> transformer = new CVariantPrefixTruncator<>(prefixLength);
		
		return log.applyVariantTransformer(transformer, false);
	}
	
	

}
