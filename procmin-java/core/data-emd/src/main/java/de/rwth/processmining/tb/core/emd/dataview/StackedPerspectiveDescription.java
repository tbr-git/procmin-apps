package de.rwth.processmining.tb.core.emd.dataview;

import java.util.Optional;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class StackedPerspectiveDescription extends PerspectiveDescriptor {
	
	private final Optional<PerspectiveDescriptor> parent;
	
	private final PerspectiveDescriptor description;
	
	private final String id;
	
	public StackedPerspectiveDescription(PerspectiveDescriptor parent, PerspectiveDescriptor description) {
		this.description = description;
		if(parent != null) {
			this.parent = Optional.of(parent);
			this.id = parent.getID() + " - " + description.getID();
		}
		else {
			this.parent = Optional.empty();
			this.id = description.getID();
		}
	}

	public StackedPerspectiveDescription(PerspectiveDescriptor description) {
		this(null, description);
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		if(parent.isPresent()) {
			builder.append(parent.get());
		}
		builder.append(description);
		return builder.build();
	}
}
