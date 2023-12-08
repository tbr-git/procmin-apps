package de.rwth.processmining.tb.core.data.stochlangdatasource;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.util.PipeBackPropInfo;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantKeys;

/**
 * Interface that defines a data source that can be used to create stochastic languages.
 * 
 * It wraps input data and returns either the current underlying raw data or a log 
 * over variants (that use a categorical encoding for the activities).
 * 
 * It is an interface that allows to stack event data transformations using a
 * decorator pattern.
 * @author brockhoff
 *
 * @param <E> Encapsulated variant type
 */
public interface StochasticLanguageDataSource<E extends CVariant> {
	
	/**
	 * Fetch the event log that has been used to instantiate the data source.
	 * @return The initial raw data/event log
	 */
	public XLog getDataRaw();
	
	/**
	 * Fetch the transformed event log that contains the data on which the current
	 * variant log is build. Can be used to trace back to individual cases. 
	 * @throws SLDSTransformationError Raised when an error in the transformation pipeline is raised
	 * @return
	 */
	public XLog getDataRawTransformed() throws SLDSTransformationError;
	
	/**
	 * The data from the data source in terms of trace variants.
	 * @throws SLDSTransformationError Raised when an error in the transformation pipeline is raised
	 * @return
	 */
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError;
	
	/**
	 * Clear data stored in intermediate caches. 
	 * @param propInfo Information class required to backpropagate deletion information.
	 */
	public void clearOldCaches(PipeBackPropInfo propInfo);

	/**
	 * Clear all caches.
	 */
	public void clearCaches();
	
	/**
	 * Get the used event classifier that is used .
	 * @return
	 */
	public XEventClassifier getClassifier();
	
	/**
	 * Get the variant log factory that has been used to instantiate this data source.
	 * Can be used to re-instantiate the data source after applying a filter that cannot
	 * be implemented on the variant log only.
	 * @return Variant log factory
	 */
	public CVariantLogFactory<E> getVariantLogFactory();
	
	/**
	 * Get the properties of the wrapped variant log without
	 * actually constructing it.
	 * @return The embedded variant log's properties.
	 */
	public VariantKeys getVariantProperties();
	
}
