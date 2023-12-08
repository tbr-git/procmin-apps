package de.rwth.processmining.tb.core.sps.algorithm;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptionLog.LogType;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;

public class PerspectiveIteration extends PerspectiveDescriptor {

	private final int iteration;

	public PerspectiveIteration(int iteration) {
		super();
		this.iteration = iteration;
	}

	@Override
	public String getID() {
		return "Focus Iteration " + this.iteration;
	}

	@Override
	public int hashCode() {
		int hash = LogType.FOCUS.hashCode();
		hash = 31 * hash + iteration;
		return hash;
	}

	public int getIteration() {
		return iteration;
	}

}
