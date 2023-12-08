package de.rwth.processmining.tb.core.data.stochlangdatasource;

import java.util.LinkedList;
import java.util.List;

import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

public class SLDSSplitter {
	
	
	/**
	 * "Split" the data source by replicating it nbrSplits times.
	 * The data source is <b>not</b> copied!
	 * @param <E> Type of the data source
	 * @param dataSource Data source
	 * @param nbrSplits Number of splits
	 * @return List containing data source nbrSplits times
	 */
	public static<E extends CVariant> List<StochasticLanguageDataSource<E>> splitDataStream(StochasticLanguageDataSource<E> dataSource, int nbrSplits) {
		List<StochasticLanguageDataSource<E>> res = new LinkedList<>();
		for(int i = 0; i < nbrSplits; i++) {
			res.add(dataSource);
		}
		return res;
	}

	/**
	 * Split the data source by applying a variable of transformers to the data source.
	 * @param <E> Type of the data source
	 * @param dataSource Data source
	 * @param factories Variable number of factories
	 * @return List of data sources obtained from applying the factories to the given data source
	 * @throws SLDSTransformerBuildingException 
	 */
	@SafeVarargs // Since the varargs compile to SLDSAbst*Factory[], we could sneak in other types causing runtime errors -> Make compilation stricter but does not avoid the problem
	public static<E extends CVariant> List<StochasticLanguageDataSource<E>> splitDataStream(StochasticLanguageDataSource<E> dataSource, 
			SLDSAbstractTransformerFactory<E>... factories) throws SLDSTransformerBuildingException {
		List<StochasticLanguageDataSource<E>> res = new LinkedList<>();
		for(SLDSAbstractTransformerFactory<E> f : factories) {
			res.add(f.setParentDataSource(dataSource).build());
		}
		return res;
	}

}
