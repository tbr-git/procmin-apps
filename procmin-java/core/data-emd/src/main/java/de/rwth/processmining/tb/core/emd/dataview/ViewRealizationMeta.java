package de.rwth.processmining.tb.core.emd.dataview;

public class ViewRealizationMeta {

	private final ViewIdentifier viewIdentifier;
	
	private final PerspectiveDescriptor perspectiveDesc;
	
	public ViewRealizationMeta(ViewIdentifier viewIdentifier, PerspectiveDescriptor perspectiveDesc) {
		super();
		this.viewIdentifier = viewIdentifier;
		this.perspectiveDesc = perspectiveDesc;
	}

	public ViewIdentifier getViewIdentifier() {
		return viewIdentifier;
	}

	public PerspectiveDescriptor getPerspectiveDesc() {
		return perspectiveDesc;
	}
	
	public String getID() {
		return this.perspectiveDesc.getID() + " - " + this.viewIdentifier.getID();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		else if(this.getClass() == obj.getClass()) {
			return this.getID().equals(((ViewRealizationMeta) obj).getID());
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.getID();
	}

	@Override
	public int hashCode() {
		return getID().hashCode();
	}

	
	
	
	
	
	
	
	

}
