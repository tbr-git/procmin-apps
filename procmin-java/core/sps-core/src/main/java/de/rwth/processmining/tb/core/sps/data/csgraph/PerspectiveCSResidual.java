package de.rwth.processmining.tb.core.sps.data.csgraph;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptionLog.LogType;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;

public class PerspectiveCSResidual extends PerspectiveDescriptor {

	public PerspectiveCSResidual() {
		super();
	}

	@Override
	public String getID() {
		return "Cornerstone Graph Residual Perspective";
	}

	@Override
	public int hashCode() {
		int hash = LogType.FOCUS.hashCode();
		//TODO
		return hash;
	}

}
