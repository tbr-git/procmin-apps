package de.rwth.processmining.tb.core.sps.data.hfddgraph.serialization;

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertexInfo;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurement;

public class HFDDVertexInfoSerializer extends StdSerializer<HFDDVertexInfo> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean withCategoryMapper;
	
	/**
	 * Translate itemsets into activity strings.
	 */
	private boolean translateItemsets;

	private final static Logger logger = LogManager.getLogger( HFDDVertexInfoSerializer.class );
	
	public HFDDVertexInfoSerializer(boolean withCategoryMapper, boolean translateItemsets) {
		this(null);
		this.withCategoryMapper = withCategoryMapper;
		this.translateItemsets = translateItemsets;
	}

	public HFDDVertexInfoSerializer() {
		this(true, false);
	}
	
	public HFDDVertexInfoSerializer(Class<HFDDVertexInfo> t) {
		super(t);
	}

	@Override
	public void serialize(HFDDVertexInfo value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeNumberField("probabilityLeft", value.getProbabilityLeft());
		gen.writeNumberField("probabilityRight", value.getProbabilityRight());
		gen.writeArrayFieldStart("activityCategories");
		value.getActivities().stream().sequential().forEach(c -> {
			try {
				if (translateItemsets) {
					gen.writeString(value.getCategoryMapper().getActivity4Category(c));
				}
				else {
					gen.writeNumber(c);
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("Error during activity set serialization. Aborting activity set serialization");
			}
		});
		gen.writeEndArray();
		/* Write all key values pairs
		 * TODO Improve this brute force approach
		 * Problem is that for example 
		 * 
		 * provider.defaultSerializeField("measurements", value.getMeasurements(), gen);
		 * gen.writeObjectField("measurements", value.getMeasurements());
		 * 
		 * both are not aware of the proper perspective description (key) serializer from the annotation.
		 * Also adding a general perspective Descriptor serializer to the objectmapper does not help.
		 * 
		 */
		gen.writeObjectFieldStart("measurements");
        for (Entry<PerspectiveDescriptor, HFDDMeasurement> e : value.getMeasurements().entrySet()) {
        	gen.writeObjectField(e.getKey().getID(), e.getValue());
        }
        gen.writeEndObject();
		
		if(withCategoryMapper) {
			gen.writeObjectField("categoryMapper", value.getCategoryMapper());
		}
		gen.writeEndObject();
	}
	
	


}
