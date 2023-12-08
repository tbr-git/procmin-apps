package de.rwth.processmining.tb.core.data.comparison;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;

import de.rwth.processmining.tb.core.data.stochlangdatasource.SLDSPipelineBuildingUtil;
import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.util.SLDSCacheFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.util.StochLangDataSourceCache;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public class BiComparisonDataSource<E extends CVariant> {
	private final static Logger logger = LogManager.getLogger( BiComparisonDataSource.class );
	
	private StochasticLanguageDataSource<E> dataSourceLeft; 

	private StochasticLanguageDataSource<E> dataSourceRight;

	private final StochasticLanguageDataSource<E> dataSourceLeftBase; 

	private final StochasticLanguageDataSource<E> dataSourceRightBase; 

	public BiComparisonDataSource(StochasticLanguageDataSource<E> dataSourceLeft,
			StochasticLanguageDataSource<E> dataSourceRight) {
		super();
		this.dataSourceLeft = dataSourceLeft;
		this.dataSourceRight = dataSourceRight;
		this.dataSourceLeftBase = dataSourceLeft;
		this.dataSourceRightBase = dataSourceRight;
	}

	/**
	 * Copy constructor
	 * @param dataSourceLeft
	 * @param dataSourceRight
	 */
	public BiComparisonDataSource(BiComparisonDataSource<E> dataSource) {
		super();
		this.dataSourceLeft = dataSource.dataSourceLeft;
		this.dataSourceRight = dataSource.dataSourceRight;
		this.dataSourceLeftBase = dataSource.dataSourceLeftBase;
		this.dataSourceRightBase = dataSource.dataSourceRightBase;
	}

	public StochasticLanguageDataSource<E> getDataSourceLeft() {
		return dataSourceLeft;
	}

	public StochasticLanguageDataSource<E> getDataSourceRight() {
		return dataSourceRight;
	} 
	
	public void applyTransformation(SLDSTransformerFactory<E> transformationFactory) 
			throws SLDSTransformerBuildingException {
		this.applyTransformationLeft(transformationFactory);
		this.applyTransformationRight(transformationFactory);
	}

	public void applyTransformationLeft(SLDSTransformerFactory<E> transformationFactory) 
			throws SLDSTransformerBuildingException {
		dataSourceLeft = SLDSPipelineBuildingUtil.extendPipeline(dataSourceLeft, transformationFactory);
	}

	public void applyTransformationRight(SLDSTransformerFactory<E> transformationFactory) 
			throws SLDSTransformerBuildingException {
		dataSourceRight = SLDSPipelineBuildingUtil.extendPipeline(dataSourceRight, transformationFactory);
	}

	public StochasticLanguageDataSource<E> getDataSourceLeftBase() {
		return dataSourceLeftBase;
	}

	public StochasticLanguageDataSource<E> getDataSourceRightBase() {
		return dataSourceRightBase;
	}
	
	public XEventClassifier getClassifier() {
		return dataSourceLeft.getClassifier();
	}
	
	public void ensureCaching() {
		if(!(dataSourceLeft instanceof StochLangDataSourceCache)) {
			SLDSCacheFactory<E> factory = new SLDSCacheFactory<>();
			try {
				this.applyTransformationLeft(factory);
			} catch (SLDSTransformerBuildingException e) {
				logger.error("Could not append requested cache to left comparison data source. Ignoring the caching request");
				e.printStackTrace();
			}
		}
		if(!(dataSourceRight instanceof StochLangDataSourceCache)) {
			SLDSCacheFactory<E> factory = new SLDSCacheFactory<>();
			try {
				this.applyTransformationRight(factory);
			} catch (SLDSTransformerBuildingException e) {
				logger.error("Could not append requested cache to right comparison data source. Ignoring the caching request");
				e.printStackTrace();
			}
		}

		
	}
}
