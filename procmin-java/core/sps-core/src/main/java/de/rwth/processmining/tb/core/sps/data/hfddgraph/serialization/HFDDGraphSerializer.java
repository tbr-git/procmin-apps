package de.rwth.processmining.tb.core.sps.data.hfddgraph.serialization;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraph;

public class HFDDGraphSerializer extends StdSerializer<HFDDGraph> { 

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger logger = LogManager.getLogger( HFDDGraphSerializer.class );
	
	public HFDDGraphSerializer() {
		this(null);
	}
	
	public HFDDGraphSerializer(Class<HFDDGraph> t) {
		super(t);
	}

	@Override
	public void serialize(HFDDGraph value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeObjectField("categoryMapper", value.getCategoryMapper());
		gen.writeArrayFieldStart("vertices");
		value.getVertices().stream()
			.sorted((u, v) -> Integer.compare(u.getVertexInfo().getActivities().cardinality(), 
					v.getVertexInfo().getActivities().cardinality()))
			.forEach(v -> {
				try {
					gen.writeObject(v);
				} catch (IOException e) {
					logger.error("Error during HFDD vertex serialization. Aborting vertex set serialization");
					e.printStackTrace();
				}
		});
		gen.writeEndArray();
		gen.writeArrayFieldStart("edges");
		for(DefaultEdge e : value.getEdges()) {
			gen.writeStartArray();
			gen.writeNumber(value.getGraph().getEdgeSource(e).getId());
			gen.writeNumber(value.getGraph().getEdgeTarget(e).getId());
			gen.writeEndArray();
		}
		gen.writeEndArray();
		gen.writeEndObject();
	}
}
