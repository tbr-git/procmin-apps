package de.rwth.processmining.tb.core.emd.language;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.util.PipeBackPropInfo;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantKeys;
import de.rwth.processmining.tb.core.data.variantlog.util.LogBuildingException;

public class SLDataSourceLazyImpl<E extends CVariant> implements StochasticLanguageDataSource<E> {
  private final static Logger logger = LogManager.getLogger(SLDataSourceLazyImpl.class);

	/**
	 * The wrapped event log
	 */
	private final XLog log;
	
	private CVariantLog<E> variants;
	
	private final CVariantLogFactory<E> factory;
	
	public SLDataSourceLazyImpl(XLog log, CVariantLogFactory<E> variantLogFactory) throws LogBuildingException {
		super();
		this.log = log;

		// Variant log is always populated and will never be cleared
		// (e.g., by cache clearing)
		this.factory = variantLogFactory;
		factory.setLog(this.log);
		variants = null;
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
	  if (variants == null) {
	    logger.debug("Actually initializing lazy variant log");
	    try {
        this.variants = this.factory.build();
      } catch (LogBuildingException e) {
        e.printStackTrace();
      }
	  }
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
		return getVariantLog().getVariantKey();
	}
}
