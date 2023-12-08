package de.rwth.processmining.tb.core.emd.dataview;

public class ViewIdentifier {
	
	public static final ViewIdentifier DEFAULT_VIEW = new ViewIdentifier("DEFAULT");
	
	public String id;
	
	public ViewIdentifier(String name) {
		id = name;
	}
	
	public String getID() {
		return id;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		else if(this.getClass() == obj.getClass()) {
			return getID().equals(((ViewIdentifier) obj).getID());
		}
		else {
			return false;
		}
	}
	
	
	
}
