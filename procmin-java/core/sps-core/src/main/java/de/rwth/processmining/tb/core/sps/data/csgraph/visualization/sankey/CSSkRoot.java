package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey;

public class CSSkRoot extends CSSkVertex {
	
	public CSSkRoot(int id) {
		super(id, true, 1, -1, null);
	}

	@Override
	public String toString() {
		return "CSSkRoot(id=" + getId() + ", isLeft=true)";
	}

}
