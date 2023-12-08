package de.rwth.processmining.tb.core.sps.data.hfddgraph;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import cern.colt.Arrays;

@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id")
public class HFDDVertex {
	
	/** 
	 * ID of the vertex.
	 * In the current use it is absolutely critical that 
	 * ids can be used to index into arrays!  
	 */
	private final int id;

	private final HFDDVertexInfo vertexInfo;

	public HFDDVertex(int id, HFDDVertexInfo vertexInfo) {
		super();
		this.id = id;
		this.vertexInfo = vertexInfo;
	}

	/**
	 * Hashing only considers {@link #id};
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * Equality only considers {@link #id};
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(!obj.getClass().equals(this.getClass())) {
			return false;
		}
		else {
			HFDDVertex other = (HFDDVertex) obj;
			return other.id == this.id;
		}
	}
	

	@Override public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getId());
		builder.append(": ");
		builder.append(Arrays.toString(this.vertexInfo.getItemsetHumanReadable()));
		return builder.toString();
	}

	//---------- Getter and Setter ----------
	public int getId() {
		return id;
	}

	public HFDDVertexInfo getVertexInfo() {
		return vertexInfo;
	}


	
}
