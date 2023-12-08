package de.rwth.processmining.tb.core.emd.dataview;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;

/**
 * Class that provides some (insufficient) fundamental functionality to create a view on two event logs using EMD.
 * 
 * It specifies:
 * <ul>
 * <li> A stochastic language transformer (how to transform an input event log into a stochastic language)
 * <li> The view id
 * </ul>
 * 
 * @author brockhoff
 *
 */
public abstract class ViewConfig {

	private final static Logger logger = LogManager.getLogger( ViewConfig.class );
	
	/**
	 * Identifier for this view
	 */
	private ViewIdentifier viewId;

	/** 
	 * Data-to-stochastic-language transformer
	 */
	private Window2OrderedStochLangTransformer langTransformer;
	
	public ViewConfig() {
		langTransformer = null;
		viewId = null;
	}
	
	/**
	 * Copy constructor.
	 * @param langTransformer
	 * @param descDistPair
	 * @param viewIdentifier
	 */
	public ViewConfig(Window2OrderedStochLangTransformer langTransformer, ViewIdentifier viewIdentifier) {
		super();
		this.langTransformer = langTransformer;
		this.viewId = viewIdentifier;
	}
	
	public ViewConfig(ViewConfig viewConfig) {
		//TODO
		Window2OrderedStochLangTransformer copiedLanguageTransformer = null;
		Class<?> transformerType = viewConfig.getLangTransformer().getClass();
	    // This next line throws a number of checked exceptions you need to catch
	    try {
			copiedLanguageTransformer = (Window2OrderedStochLangTransformer) transformerType.getConstructor(transformerType).newInstance(viewConfig.getLangTransformer());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			logger.error("Could not copy the language transformer in the copy constructor");
			e.printStackTrace();
		}
	    
	    this.langTransformer = copiedLanguageTransformer;
	    this.viewId = viewConfig.getViewIdentifier();
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// Getter/Setter
	////////////////////////////////////////////////////////////////////////////////
	public ViewConfig setLangTransformer(Window2OrderedStochLangTransformer langTransformer) {
		this.langTransformer = langTransformer;
		return this;
	}

	public ViewConfig setViewIdenfitier(ViewIdentifier viewIdentifier) {
		this.viewId = viewIdentifier;
		return this;
	}

	public Window2OrderedStochLangTransformer getLangTransformer() {
		return langTransformer;
	}

	public ViewIdentifier getViewIdentifier() {
		return viewId;
	}
	
}
