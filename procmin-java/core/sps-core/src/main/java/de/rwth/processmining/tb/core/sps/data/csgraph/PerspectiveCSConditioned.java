package de.rwth.processmining.tb.core.sps.data.csgraph;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptionLog.LogType;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;

public class PerspectiveCSConditioned extends PerspectiveDescriptor {

	
	@Override
	public String getID() {
		return "Cornerstone Graph Conditioned Perspective";
	}

	@Override
	public int hashCode() {
		int hash = LogType.FOCUS.hashCode();
		//TODO
		return hash;
	}


}
