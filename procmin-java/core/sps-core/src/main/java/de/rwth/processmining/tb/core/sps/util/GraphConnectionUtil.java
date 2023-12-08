package de.rwth.processmining.tb.core.sps.util;

import java.util.ArrayList;
import java.util.Comparator;

import org.jgrapht.Graph;

public class GraphConnectionUtil {
	
	public static<V, E> void addSPOEdges(Graph<V, E> g, Comparator<V> vertexComparator) {
		ArrayList<V> vertices = new ArrayList<>(g.vertexSet());		// Fix order
		// Iterate over all pairs of vertices
		int compRes;
		for (int i = 0; i < vertices.size() - 1; i++) {
			V u = vertices.get(i);
			for (int j = i + 1; j < vertices.size(); j++) {
				V v = vertices.get(j);
				compRes = vertexComparator.compare(u, v);
				// Check if u < v or v < u 
				if (compRes < 0) {			// u < v
					g.addEdge(u, v);
				}
				else if (compRes > 0) {		// u < u
					g.addEdge(v, u);
				}
			}
		}
	}
}
