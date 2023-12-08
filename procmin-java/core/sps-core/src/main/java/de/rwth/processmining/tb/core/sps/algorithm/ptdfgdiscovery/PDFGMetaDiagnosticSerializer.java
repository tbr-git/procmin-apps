package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery;

import java.io.IOException;

import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.DiffCandidate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class PDFGMetaDiagnosticSerializer extends StdSerializer<PDFGMetaDiagnostic>{

	/**
   * 
   */
  private static final long serialVersionUID = -3844231256594720392L;

  public PDFGMetaDiagnosticSerializer() {
		this(null);
	}
	
	public PDFGMetaDiagnosticSerializer(Class<PDFGMetaDiagnostic> t) {
		super(t);
	}

  @Override
  public void serialize(PDFGMetaDiagnostic value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartObject();
    gen.writeObjectFieldStart("differenceCandidate");
    gen.writeArrayFieldStart("activities");
    String[] activities = value.diffCandidates().diffCandidate().v().getVertexInfo().getItemsetHumanReadable();
    gen.writeArray(activities, 0, activities.length);
    gen.writeEndArray();
    gen.writeEndObject();

    gen.writeArrayFieldStart("complementaryDifferences");
    // For each difference candidate
    for (DiffCandidate c : value.diffCandidates().complDifferences()) {
      // Write the activities t
      gen.writeStartObject();
      gen.writeArrayFieldStart("activities");

      activities = c.v().getVertexInfo().getItemsetHumanReadable();
      gen.writeArray(activities, 0, activities.length);

      gen.writeEndArray();
      gen.writeEndObject();
    }
    gen.writeEndArray();
    gen.writeEndObject();
  }

}
