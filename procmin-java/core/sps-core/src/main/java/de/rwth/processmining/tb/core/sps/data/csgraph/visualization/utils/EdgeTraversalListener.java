package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.utils;

import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;

/**
 * Specification of the {@link TraversalListener} that only cares about edges.
 * 
 * Default all other method to empty.
 * @author brockhoff
 *
 * @param <V> Vertex type of the graph
 * @param <E> Edge type of the graph
 */
public interface EdgeTraversalListener<V, E> extends TraversalListener<V, E> {

	@Override
	default void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
	}

	@Override
	default void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
	}

	@Override
	default void vertexTraversed(VertexTraversalEvent<V> e) {
	}

	@Override
	default void vertexFinished(VertexTraversalEvent<V> e) {
	}

}
