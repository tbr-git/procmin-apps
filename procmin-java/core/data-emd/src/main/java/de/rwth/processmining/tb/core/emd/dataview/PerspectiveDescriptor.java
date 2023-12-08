package de.rwth.processmining.tb.core.emd.dataview;

public abstract class PerspectiveDescriptor {
	
	public abstract String getID();

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		else if(this.getClass() == obj.getClass()) {
			return getID().equals(((PerspectiveDescriptor) obj).getID());
		}
		else {
			return false;
		}
	}

	@Override
	public abstract int hashCode(); 
	
}
