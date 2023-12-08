package de.rwth.processmining.tb.core.data.variantlog.base;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Class that is used to map between category codes and activity names.
 * @author brockhoff
 *
 */
@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id")
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class CategoryMapperImpl implements CategoryMapper {
	// Even though this is tightly coupled to VariantLogs, we do define this as an inner class
	// of the variant log.
	// Otherwise, this class would keep a reference to its variant log preventing garbage collection
	// Sometimes we want to share the class instances (e.g., transformed variant log or trace descriptor translation)
	// In this cases, this class can be considered as "standalone" and we may still want to garbage collect 
	// the outer instance.
	
	private String id;
	
	protected Map<String, Integer> mapAct2Cat;

	protected String[] id2Activity;
	
	// Constructors
	
	public CategoryMapperImpl() {
		this.id = UUID.randomUUID().toString();
		this.mapAct2Cat = null;
		this.id2Activity = null;
	}
	
	public CategoryMapperImpl(Map<String, Integer> mapAct2Cat, String[] id2Activity) {
		this(UUID.randomUUID().toString(), mapAct2Cat, id2Activity);
	}
	
	@JsonCreator
	public CategoryMapperImpl(@JsonProperty("id") String id, @JsonProperty("mapAct2Cat") Map<String, Integer> mapAct2Cat, @JsonProperty("id2Activity") String[] id2Activity) {
		super();
		this.id = id;
		this.mapAct2Cat = mapAct2Cat;
		this.id2Activity = id2Activity;
	}

	@Override
	public Integer getCategory4Activity(String activity) {
		return mapAct2Cat.get(activity);
	}
	
	@Override
	public String getActivity4Category(int category) {
		if(category < 0 || category >= this.id2Activity.length) {
			return null;
		}
		else {
			return this.id2Activity[category];
		}
	}
	
	@JsonIgnore
	public int getMaxCategoryCode() {
		return this.id2Activity.length - 1;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int getCategoryCount() {
		return this.id2Activity.length;
	}

}
