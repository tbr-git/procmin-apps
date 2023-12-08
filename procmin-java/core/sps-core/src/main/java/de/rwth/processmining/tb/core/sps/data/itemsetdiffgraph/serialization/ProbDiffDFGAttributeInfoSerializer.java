package de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.serialization;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFG;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFGEdge;

public class ProbDiffDFGAttributeInfoSerializer extends StdSerializer<ProbDiffDFG> { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3548622377105123915L;

	private final static Logger logger = LogManager.getLogger( ProbDiffDFGAttributeInfoSerializer.class );

	public ProbDiffDFGAttributeInfoSerializer() {
		this(null);
	}
	
	public ProbDiffDFGAttributeInfoSerializer(Class<ProbDiffDFG> t) {
		super(t);
	}

	@Override
	public void serialize(ProbDiffDFG value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeObjectFieldStart("vertices");
		value.getGraph().vertexSet().stream()
			.forEach(v -> {
				try {
					gen.writeFieldId(v.getCategoryCode());
					gen.writeObject(v);
					//gen.writeEndObject();
				} catch (IOException e) {
					logger.error("Error during ProbDiffDFG vertex serialization. Aborting vertex set serialization");
					e.printStackTrace();
				}
		});
		gen.writeEndObject();
		gen.writeObjectFieldStart("edges");
		for(ProbDiffDFGEdge e : value.getGraph().edgeSet()) {
			gen.writeFieldId(e.getId());
			//gen.writeObjectFieldStart(Integer.toString(e.getId()));
			gen.writeObject(e);
			//gen.writeEndObject();
		}
		gen.writeEndObject();
		gen.writeEndObject();
	}
}
