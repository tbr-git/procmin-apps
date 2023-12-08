package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;

/**
 * Vertex in the difference graph (tree).
 * Allows for:
 * <ul>
 * 	<li>Vertices that represent activity sets.
 * 	<li>Vertices that represent trace variants.
 * </ul>
 * @author brockhoff
 *
 */
public abstract sealed class DDGVertex permits DDGVertexSet, DDGVertexEMDSplit, DDGVertexTrace, DDGVertexArtificialRoot {

	/**
	 * Vertex id.
	 * 
	 * <b> Hashing and equality will be tested based on that! </b>
	 */
	private final int id;
	
	/**
	 * Identifier string. 
	 * Can be used to identify this vertex (same HFDD reference) among multiple 
	 * queries of an DDGraph.
	 */
	private final String idString;

	/**
	 * Handle to the {@link CSGraphVertex} that is associated to this vertex.
	 */
	@JsonIgnore
	private final CSGraphVertex csGraphVertex;
	
	/**
	 * Level in the tree visualization.
	 */
	private int visLevel;
	
	public DDGVertex(int id, String idString, CSGraphVertex csGraphVertex) {
		super();
		this.id = id;
		this.idString = idString;
		this.csGraphVertex = csGraphVertex;
		this.visLevel = -1;
	}

	////////////////////////////////////////////////////////////////////////////////
	// Important when using in JGrapht Graphs  
	////////////////////////////////////////////////////////////////////////////////

	@Override
	public final int hashCode() {
		return this.id;
	}

	@Override
	public final boolean equals(Object obj) {
		return obj == this
				|| obj instanceof DDGVertex v 
				&& this.id == v.getId();
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters
	////////////////////////////////////////////////////////////////////////////////

	public int getVisLevel() {
		return visLevel;
	}

	public void setVisLevel(int visLevel) {
		this.visLevel = visLevel;
	}

	public int getId() {
		return id;
	}

	public CSGraphVertex getCsGraphVertex() {
		return csGraphVertex;
	}
	
	public abstract DDGVertexType getVertexType();

	public String getIdString() {
		return idString;
	}
}
