package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.projection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.contextaware.CVariantCatContSet;

public class SLDSPrefixWFutureTruncationBuilder<E extends CVariantCatContSet> extends 
		SLDSAbstractTransformerFactory<E> {

	private final static Logger logger = LogManager.getLogger( SLDSPrefixWFutureTruncationBuilder.class );
	
	private int prefixLength;
	
	public SLDSPrefixWFutureTruncationBuilder() {
		prefixLength = 0;
	}
	
	public SLDSPrefixWFutureTruncationBuilder<E> setPrefixLength(int prefixLength) {
		this.prefixLength = prefixLength;
		return this;
	}

	@Override
	public StochasticLanguageDataSource<E> build() throws SLDSTransformerBuildingException {
		if(this.parentDataSource == null) {
			logger.error("Missing mandatory parameters.");
			throw new SLDSTransformerBuildingException("Could not instantiate the prefix "
					+ "truncation transformer due to misssing arguments");
		}
		
		return new SLDSPrefixWFutureTruncation<E>(this.parentDataSource, this.prefixLength);
	}
}
