package de.rwth.processmining.tb.core.data.variantlog.base;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.variantlog.util.LogBuildingException;

public abstract class CVariantLogFactory<E extends CVariant> {
	private final static Logger logger = LogManager.getLogger( CVariantLogFactory.class );

	/**
	 * Classifier to derive activity names
	 */
	protected XEventClassifier classifier = null;;
	
	/**
	 * Log that should be converted.
	 */
	protected XLog log = null;
	
	/**
	 * Activity to categorical code mapping (optional).
	 */
	protected CategoryMapper catMapper = null;

	public CVariantLogFactory<E> setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
		return this;
	}
	
	public XEventClassifier getClassifier() {
		return this.classifier;
	}
	
	public CVariantLogFactory<E> setLog(XLog log) {
		this.log = log;
		return this;
	}

	public CVariantLogFactory<E> setCategoryMapper(CategoryMapper catMapper) {
		this.catMapper = catMapper;
		return this;
	}
	
	public CategoryMapper getCategoryMapper() {
		return this.catMapper;
	}

	public abstract CVariantLog<E> build() throws LogBuildingException;
	
	public static<T extends CVariant> CVariantLog<T> buildFrom(CVariantLog<T> log, ArrayList<T> variants) {
		return new CVariantLogImpl<>(log.getClassifier(), log.getCategoryMapper(), variants);
	}
}
