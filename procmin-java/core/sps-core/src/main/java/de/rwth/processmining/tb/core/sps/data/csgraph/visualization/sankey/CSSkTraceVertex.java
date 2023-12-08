package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey;

import java.util.Comparator;
import java.util.function.Function;

import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;

public class CSSkTraceVertex extends CSSkVertex {
	
	/**
	 * Sequence of activity descriptors (possibly abbreviations) that will be displayed.
	 */
	private String[] activityDescriptors;
	
	/**
	 * Number of the variant that this {@link #trace} belongs to.
	 */
	private int variant;
	
	/**
	 * Weight value associated with this trace in a 
	 * matching-based visualization.
	 * Except for empty traces (if there is flow to another empty trace),
	 * this will always be equal to the standard probability
	 */
	private double matchingWeight;

	public CSSkTraceVertex(int id, boolean isLeft, double probabilityMass, CSGraphVertex csGraphVertex,
			String[] activityDescriptors) {
		super(id, isLeft, probabilityMass, probabilityMass, csGraphVertex);
		this.activityDescriptors = activityDescriptors;
		// Initialize with probability
		// Will be adapted after the graph is fully constructed (and all edge information is available)
		this.matchingWeight = probabilityMass;
	}
	
	@Override
	public void formatActivities(Comparator<String> itemSorter, Function<String, String> activityAbbrev) {
		super.formatActivities(itemSorter, activityAbbrev);

		// Abbreviate activity names
		for (int i = 0; i < activityDescriptors.length; i++) {
			activityDescriptors[i] = activityAbbrev.apply(activityDescriptors[i]);
		}
	}

	//================================================================================
	// Getters and Setters
	//================================================================================

	public String[] getActivityDescriptors() {
		return activityDescriptors;
	}

	public void setActivityDescriptors(String[] activityDescriptors) {
		this.activityDescriptors = activityDescriptors;
	}

	public int getVariant() {
		return variant;
	}

	public void setVariant(int variant) {
		this.variant = variant;
	}

	public double getMatchingWeight() {
		return matchingWeight;
	}

	public void setMatchingWeight(double matchingWeight) {
		this.matchingWeight = matchingWeight;
	}

	@Override
	public String toString() {
		return String.format("TraceVertex(id=%d, isLeft=%b, probMass=%f, refId=%d, trace=<%s>)", 
				getId(), this.isLeft(), this.getProbabilityMass(), this.getCsGraphVertex().getHfddVertexRef().getId(),
				String.join(", ", activityDescriptors));
	}
}
