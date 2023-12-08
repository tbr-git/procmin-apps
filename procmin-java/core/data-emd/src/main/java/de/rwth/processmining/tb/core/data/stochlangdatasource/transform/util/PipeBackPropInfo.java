package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.util;

/**
 * Info class that can be used to backpropagate information along the {@code StochasticLanguageDataSource} transformation.
 * @author brockhoff
 *
 */
public class PipeBackPropInfo {
	
	/** 
	 * Has there already been found a more recent cache.
	 */
	protected boolean foundNewCache;

	public PipeBackPropInfo() {
		super();
		this.foundNewCache = false;
	}
	
}
