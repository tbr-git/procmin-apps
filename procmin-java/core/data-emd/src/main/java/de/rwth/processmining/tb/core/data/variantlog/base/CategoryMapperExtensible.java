package de.rwth.processmining.tb.core.data.variantlog.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Class that is used to map between category codes and activity names.
 *
 * Note that this implementations allows the category mapping to be updated (causing side effects).
 * 
 * However, adding categories via {@link this#getCategory4ActivityOrAdd(String)} ensures that old category mapping 
 * remain valid.
 * 
 * Categories will be created in consecutive order.
 * 
 * @author brockhoff
 *
 */
@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id")
public class CategoryMapperExtensible implements CategoryMapper {
	// Even though this is tightly coupled to VariantLogs, we do define this as an inner class
	// of the variant log.
	// Otherwise, this class would keep a reference to its variant log preventing garbage collection
	// Sometimes we want to share the class instances (e.g., transformed variant log or trace descriptor translation)
	// In this cases, this class can be considered as "standalone" and we may still want to garbage collect 
	// the outer instance.
	
	private String id;
	
	/**
	 * Category mapping. 
	 * Maps from activity names to category codes.
	 * Existing mappings should not be overriden.
	 */
	protected final Map<String, Integer> mapAct2Cat;
	
	/**
	 * Each position corresponds to an category code and contains the 
	 * corresponding activity name. 
	 */
	protected final ArrayList<String> id2Activity;
	
	// Constructors
	
	public CategoryMapperExtensible() {
		this.id = UUID.randomUUID().toString();
		this.mapAct2Cat = new HashMap<>();
		this.id2Activity = new ArrayList<String>(100);
	}
	
	public CategoryMapperExtensible(Map<String, Integer> mapAct2Cat, ArrayList<String> id2Activity) {
		this(UUID.randomUUID().toString(), mapAct2Cat, id2Activity);
	}
	
	@JsonCreator
	public CategoryMapperExtensible(@JsonProperty("id") String id, @JsonProperty("mapAct2Cat") Map<String, Integer> mapAct2Cat, @JsonProperty("id2Activity") ArrayList<String> id2Activity) {
		super();
		this.id = id;
		this.mapAct2Cat = mapAct2Cat;
		this.id2Activity = id2Activity;
	}

	@Override
	public Integer getCategory4Activity(String activity) {
		return mapAct2Cat.get(activity);
	}

	/**
	 * Get the category code for the requested activity or create and add a new category.
	 * @param activity
	 * @return
	 */
	public Integer getCategory4ActivityOrAdd(String activity) {
		Integer cat = mapAct2Cat.get(activity);
		if (cat == null) {
			cat = id2Activity.size();
			id2Activity.add(activity);
			mapAct2Cat.put(activity, cat);
		}
		return cat;
	}
	
	@Override
	public String getActivity4Category(int category) {
		if(category < 0 || category >= this.id2Activity.size()) {
			return null;
		}
		else {
			return this.id2Activity.get(category);
		}
	}
	
	@Override
	@JsonIgnore
	public int getMaxCategoryCode() {
		return this.id2Activity.size() - 1;
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
		return this.id2Activity.size();
	}
}
