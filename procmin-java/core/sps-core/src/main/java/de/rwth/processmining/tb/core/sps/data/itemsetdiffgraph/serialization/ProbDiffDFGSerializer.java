package de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.serialization;


import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFG;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFGEdge;

public class ProbDiffDFGSerializer extends StdSerializer<ProbDiffDFG> { 

	/**
	 * 
	 */
	private static final long serialVersionUID = -6416843747240435012L;

	private final static Logger logger = LogManager.getLogger( ProbDiffDFGSerializer.class );
	
	public ProbDiffDFGSerializer() {
		this(null);
	}
	
	public ProbDiffDFGSerializer(Class<ProbDiffDFG> t) {
		super(t);
	}

	@Override
	public void serialize(ProbDiffDFG value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeArrayFieldStart("vertices");
		value.getGraph().vertexSet().stream()
			.forEach(v -> {
				try {
					gen.writeObject(v);
				} catch (IOException e) {
					logger.error("Error during ProbDiffDFG vertex serialization. Aborting vertex set serialization");
					e.printStackTrace();
				}
		});
		gen.writeEndArray();
		gen.writeArrayFieldStart("edges");
		for(ProbDiffDFGEdge e : value.getGraph().edgeSet()) {
			gen.writeStartArray();
			gen.writeNumber(value.getGraph().getEdgeSource(e).getCategoryCode());
			gen.writeNumber(value.getGraph().getEdgeTarget(e).getCategoryCode());
			gen.writeEndArray();
		}
		gen.writeEndArray();
		gen.writeEndObject();
		// TODO Auto-generated method stub
		
	}
}
