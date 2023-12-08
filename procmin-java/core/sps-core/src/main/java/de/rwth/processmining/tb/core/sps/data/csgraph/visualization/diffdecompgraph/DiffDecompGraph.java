package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

import org.jgrapht.Graph;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph.serialization.DDGraphSerializer;

/**
 * Graph structure for a difference graph + matching visualization 
 *  (using a spanning tree for the set hierarchy).
 * 
 * Idea:
 * 				 -> matchLeft1 --- matchRight1
 *		-> set1	 -> matchLeft2 --- matchRight2
 * root  
* 		-> set1	 -> matchLeft1 --- matchRight1
 *				 -> matchLeft2 --- matchRight2
 * @author brockhoff
 *
 */
@JsonSerialize(using = DDGraphSerializer.class)
public class DiffDecompGraph {
	
	/**
	 * Handle to the JGraphtT graph.
	 */
	private final Graph<DDGVertex, DDGEdge> g;
	
	/**
	 * Entry to the structure (roots the DAG).
	 */
	private final DDGVertex root;
	
	public DiffDecompGraph(Graph<DDGVertex, DDGEdge> g, DDGVertex root) {
		this.g = g;
		this.root = root;
	}
	
	public Graph<DDGVertex, DDGEdge> getGraph() {
		return g;
	}
	
	public DDGVertex getRoot() {
		return this.root;
	}
	
	
	

}
