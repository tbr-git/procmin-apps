package de.rwth.processmining.tb.core.data.stochlangdatasource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.variantlog.base.CCCLogImplFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.CCCVariantImpl;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.util.LogBuildingException;
import de.rwth.processmining.tb.core.emd.language.SLDataSourceLazyImpl;

/**
 * Defines a builder that connect and build a {@link StochasticLanguageDataSource}.
 * 
 * In essence, a log can be added and then a {@link StochasticLanguageDataSource} build on top of it.
 * @author brockhoff
 *
 * @param <E> Variant abstraction to be used
 */
public class SLDSConnectorFactory<E extends CVariant> {

	private final static Logger logger = LogManager.getLogger( SLDSConnectorFactory.class );
	
	/**
	 * XLog to be built on 
	 */
	private XLog xlog;

	/**
	 * Variant Factory to be used
	 */
	private CVariantLogFactory<E> variantLogFactory;
	
	
	public SLDSConnectorFactory() {
		this.xlog = null;
		this.variantLogFactory = null;
		
	}
	/**
	 * 
	 * @param xlog Event log data source
	 * @return
	 */
	public SLDSConnectorFactory<E> setXLog(XLog xlog) {
		this.xlog = xlog;
		return this;
	}
	
	/**
	 * Set the variant log factory that will be used to represent the log.
	 * @param variantLogFactory
	 * @return
	 */
	public SLDSConnectorFactory<E> setVariantLogFactory(CVariantLogFactory<E> variantLogFactory) {
		this.variantLogFactory = variantLogFactory;
		return this;
	}

	/**
	 * Build a stochastic language data source that connects to an event log.
	 * @param <E> Variant abstraction type that is encapsulated by the data source.
	 * @return 
	 */
	public StochasticLanguageDataSource<E> build() throws LogBuildingException {
		if(this.xlog == null) {
			throw new LogBuildingException("XLog is null. Data source building failed!");
		}
		if(this.variantLogFactory == null) {
			throw new LogBuildingException("Variant factory is null. Data source building failed!");
		}
		return new StochLangDataSourceImpl<E>(this.xlog, this.variantLogFactory);
	}

	/**
	 * Build a stochastic language data source that connects to an event log.
	 * Connection between event log (xlog) and variant log is lazy. 
	 * Variant log is only instantiate when it is polled for the first time.
	 * 
	 * @param <E> Variant abstraction type that is encapsulated by the data source.
	 * @return Stochastic language data source that lazily instantiates the variant log
	 * @throws LogBuildingException 
	 */
	public StochasticLanguageDataSource<E> buildLazy() throws LogBuildingException {
		if(this.xlog == null) {
			throw new LogBuildingException("XLog is null. Data source building failed!");
		}
		if(this.variantLogFactory == null) {
			throw new LogBuildingException("Variant factory is null. Data source building failed!");
		}
		return new SLDataSourceLazyImpl<E>(this.xlog, this.variantLogFactory);
	}
	
	public static StochasticLanguageDataSource<CCCVariantImpl> buildCCCDataSource(XLog xlog, 
			XEventClassifier classifier) throws LogBuildingException {
		CCCLogImplFactory variantLogFactory = new CCCLogImplFactory();
		variantLogFactory.setClassifier(classifier);

		SLDSConnectorFactory<CCCVariantImpl> sldsFactory = new SLDSConnectorFactory<>();
		return sldsFactory.setVariantLogFactory(variantLogFactory).setXLog(xlog).build();
		
	}

}
