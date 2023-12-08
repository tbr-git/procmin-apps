package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class CSSankeyGraphSerializer extends StdSerializer<CSSankeyGraph> {
	private final static Logger logger = LogManager.getLogger( CSSankeyGraphSerializer.class );


	/**
	 * 
	 */
	private static final long serialVersionUID = 5091293152033592394L;


	public CSSankeyGraphSerializer() {
		this(null);
	}

	public CSSankeyGraphSerializer(Class<CSSankeyGraph> t) {
	 super(t);	
	}

	
	@Override
	public void serialize(CSSankeyGraph skGraph, JsonGenerator gen, SerializerProvider provider) throws IOException {
		Graph<CSSkVertex, CSSkEdge> g = skGraph.getG();
		gen.writeStartObject();
		// Serialize the vertex array
		gen.writeArrayFieldStart("Vertices");
		g.vertexSet().forEach(v -> {
			try {
				gen.writeObject(v);
			} catch (IOException e) {
				logger.error("Error during Sankey graph vertex serialization. Aborting vertex set serialization");
				e.printStackTrace();
				return;
			}
		});
		gen.writeEndArray();
		// Serialize edges
		gen.writeArrayFieldStart("Edges");
		g.edgeSet().forEach(e -> {
			CSSkVertex source = g.getEdgeSource(e);
			CSSkVertex target = g.getEdgeTarget(e);
			try {
				gen.writeStartObject();
				gen.writeNumberField("Source", source.getId());
				gen.writeNumberField("Target", target.getId());
				gen.writeObjectField("EdgeInfo", e);
				gen.writeEndObject();
			} catch (IOException e1) {
				logger.error("Error during Sankey graph edge serialization. Skipping edge");
				e1.printStackTrace();
			}
		});
		gen.writeEndArray();
		gen.writeEndObject();

	}

}
