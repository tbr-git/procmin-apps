package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph.serialization;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph.DDGEdge;
import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph.DDGVertex;
import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph.DiffDecompGraph;

public class DDGraphSerializer extends StdSerializer<DiffDecompGraph> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1334378331630049196L;
	/**
	 * Logger
	 */
	private final static Logger logger = LogManager.getLogger( DDGraphSerializer.class );
	
	public DDGraphSerializer(Class<DiffDecompGraph> t) {
	 super(t);	
	}

	public DDGraphSerializer() {
		this(null);
	}

	@Override
	public void serialize(DiffDecompGraph ddGraph, JsonGenerator gen, SerializerProvider provider) 
			throws IOException {

		Graph<DDGVertex, DDGEdge> g = ddGraph.getGraph();
		
		gen.writeStartObject();
		////////////////////
		// Serialize the vertex array
		////////////////////
		gen.writeArrayFieldStart("Vertices");
		g.vertexSet().forEach(v -> {
			try {
				// Serialize the vertex
				gen.writeObject(v);
				// gen.writeStartObject();
				// Vertex id
				// gen.writeStringField("id", Integer.toString(v.getId()));
				// Vertex data object
				// gen.writeObjectField("data", v);
				// Parent data 
				// serializeD3DagParentData(gen, g, v);
				// gen.writeEndObject();
			} catch (IOException e) {
				logger.error("Error during difference decomposition graph vertex serialization. "
						+ "Aborting vertex set serialization");
				e.printStackTrace();
				return;
			}
			// Parent Information
		});
		gen.writeEndArray();

		////////////////////
		// Serialize edges
		////////////////////
		gen.writeArrayFieldStart("Edges");
		g.edgeSet().forEach(e -> {
			DDGVertex source = g.getEdgeSource(e);
			DDGVertex target = g.getEdgeTarget(e);
			try {
				gen.writeStartObject();
				gen.writeNumberField("Source", source.getId());
				gen.writeNumberField("Target", target.getId());
				gen.writeObjectField("EdgeInfo", e);
				gen.writeEndObject();
			} catch (IOException e1) {
				logger.error("Error during difference decomposition graph edge serialization. "
						+ "Skipping edge");
				e1.printStackTrace();
			}
		});
		gen.writeEndArray();
		gen.writeEndObject();
	}
	
	/**
	 * Serialize the parent data for a given vertex so that it can be easily passed by 
	 * <a href="https://erikbrinkman.github.io/d3-dag/index.html">d3Dag</a>.
	 * 
	 * @param gen Generator used to write the json objects
	 * @param g Handle to the graph (we extract the edges from it) 
	 * @param v Vertex for which the parent data should be added.
	 * @throws IOException 
	 */
	private void serializeD3DagParentData(JsonGenerator gen, Graph<DDGVertex, DDGEdge> g, DDGVertex v) throws IOException {
		gen.writeArrayFieldStart("parentData");
		g.incomingEdgesOf(v).stream()
			.sequential()
			.forEach(e -> {
				try {
					gen.writeStartArray();
					gen.writeString(Integer.toString(g.getEdgeSource(e).getId()));
					gen.writeObject(e);
					gen.writeEndArray();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
		gen.writeEndArray();
	}

}
