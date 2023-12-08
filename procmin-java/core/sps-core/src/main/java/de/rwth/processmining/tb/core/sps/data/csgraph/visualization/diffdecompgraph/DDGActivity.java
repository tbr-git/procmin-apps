package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

import java.util.Optional;

public class DDGActivity {
	
	/**
	 * Activity as human-readable string
	 */
	private final String activity;
	
	/**
	 * Activity code (optional)
	 */
	private final Optional<Integer> activityCode;
	
	/**
	 * Activity abbreviation (optional)
	 */
	private final Optional<String> activityAbbrev;
	
	public DDGActivity(String activity, Optional<Integer> activityCode, Optional<String> activityAbbrev) {
		this.activity = activity;
		this.activityAbbrev = activityAbbrev;
		this.activityCode = activityCode;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// Getter
	////////////////////////////////////////////////////////////////////////////////

	public String getActivity() {
		return activity;
	}

	public Optional<Integer> getActivityCode() {
		return activityCode;
	}

	public Optional<String> getActivityAbbrev() {
		return activityAbbrev;
	}

}
