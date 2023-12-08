package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey;

import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;

//TODO 
// Use This
public class CSSkTraceWithAbractionsVertex extends CSSkTraceVertex {
	
	/**
	 * Abstraction codes;
	 */
	private int[] abstractions;

	public CSSkTraceWithAbractionsVertex(int id, boolean isLeft, double probabilityMass, CSGraphVertex csGraphVertex,
			String[] activityDescriptors, int[] abstractions) {
		super(id, isLeft, probabilityMass, csGraphVertex, activityDescriptors);
		this.abstractions = abstractions;
	}

	//================================================================================
	// Getters and Setters
	//================================================================================

	public int[] getAbstractions() {
		return abstractions;
	}

	public void setAbstractions(int[] abstractions) {
		this.abstractions = abstractions;
	}

}
