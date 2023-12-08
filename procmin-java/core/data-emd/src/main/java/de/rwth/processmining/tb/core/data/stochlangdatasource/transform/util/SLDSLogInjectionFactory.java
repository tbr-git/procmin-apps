package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.util;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.util.LogBuildingException;

/**
 * Factory for an XLog injection transformation into an existing pipeline.
 * 
 * Example usage:
 * <p>{@code
 * 
 * }</p>
 * @author brockhoff
 *
 * @param <E>
 */
public class SLDSLogInjectionFactory<E extends CVariant> extends SLDSAbstractTransformerFactory<E> {
	private final static Logger logger = LogManager.getLogger( SLDSLogInjectionFactory.class );

	/**
	 * Factory to create a variant log from the provided injected log.
	 */
	private CVariantLogFactory<E> factory;
	
	////////////////////////////////////////
	// Data
	// If the variant log already exists, it can be provided.
	// Yet we do not enforce any proper correspondence between
	// the provided xlog and variant log.
	// !!! We just assume they correspond !!!
	////////////////////////////////////////
	
	/**
	 * Injected log. Will be used as source for subsequent transformations 
	 * and the variant log
	 */
	private XLog xlog;
	
	/**
	 * Injected variant log. 
	 */
	private Optional<CVariantLog<E>> variantLog;

	public SLDSLogInjectionFactory() {
		super();
		factory = null;
		xlog = null;
		this.variantLog = Optional.empty();
	}
	
	public SLDSLogInjectionFactory<E> setXLog(XLog xlog) {
		this.xlog = xlog;
		return this;
	}

	public SLDSLogInjectionFactory<E> setVariantLog(CVariantLog<E> variantLog) {
		this.variantLog = Optional.of(variantLog);
		return this;
	}

	public SLDSLogInjectionFactory<E> setVariantLogFactory(CVariantLogFactory<E> factory) {
		this.factory = factory;
		return this;
	}

	@Override
	public StochasticLanguageDataSource<E> build() throws SLDSTransformerBuildingException {
		if(factory == null || xlog == null || this.parentDataSource == null) {
			logger.error("Missing mandatory parameters for the log injection.");
		}
		if (this.variantLog.isEmpty()) {
      try {
        return new SLDSLogInjection<E>(parentDataSource, factory, xlog);
      } catch (LogBuildingException e) {
        throw new SLDSTransformerBuildingException("Could not build the log injection transformer:\n" + e.getMessage());
      }
		}
		else {
      return new SLDSLogInjection<E>(parentDataSource, factory, xlog, this.variantLog.get());
		}
	}
	
	
	
}
