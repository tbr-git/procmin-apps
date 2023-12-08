package de.rwth.processmining.tb.core.sps.data.hfddgraph;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.projection.SLDSProjectionCategoryFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection.SLDSFilterMandatoryCategoryFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.serialization.PerspectiveDescriptorSerializer;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurement;

public class HFDDVertexInfo {
	private static final Logger logger = LogManager.getLogger(HFDDVertexInfo.class);

	/**
	 * Frequent activity set associated with this vertex.
	 */
	private final BitSet activities;
	
	/**
	 * Category mapper reference that can be used to translate categories to activity names.
	 */
	private final CategoryMapper categoryMapper;
	
	/**
	 * Measurement map.
	 */
	@JsonSerialize(keyUsing = PerspectiveDescriptorSerializer.class)
	private Map<PerspectiveDescriptor, HFDDMeasurement> measurements;
	
	/**
	 * Base measurement.
	 */

	private HFDDMeasurement baseMeasurement = null; 
	
	public HFDDVertexInfo(BitSet activities, CategoryMapper categoryMapper) {
		this.activities = activities;
		this.categoryMapper = categoryMapper;
		this.measurements = new HashMap<>();
	}

	/**
	 * Builds the initial vertex information container.
	 * @param pattern Weighted activity itemset
	 * @param categoryMapper Category mapper for the itemset
	 * @param nbrCategories Maximum number of categories for the graph in which this vertex will reside
	 * @return Initial vertex information container
	 */
	public static HFDDVertexInfo buildBaseInfo(Collection<Integer> itemset, 
			CategoryMapper categoryMapper, int nbrCategories) {
		BitSet activities = new BitSet(nbrCategories);
		for(int c : itemset) {
			activities.set(c);
		}
		return new HFDDVertexInfo(activities, categoryMapper);
	}

	/**
	 * Creates a cached variant log by applying the transformation associated with this vertex.
	 * @param <T>
	 * @param biCompDS Log that will be transformed
	 * @return Transformed (i.e., the vertex-specific transformation) log 
	 * @throws SLDSTransformerBuildingException Error when transformation fails
	 */
	public<T extends CVariant> BiComparisonDataSource<T> createVertexLog(BiComparisonDataSource<T> biCompDS) 
			throws SLDSTransformerBuildingException {
		// Create copy of datasource
		biCompDS = new BiComparisonDataSource<>(biCompDS);
		// Reduce data on vertex sublog
		// Mandatory activity filtering -> Each trace must contain the vertex' activities
		SLDSFilterMandatoryCategoryFactory<T> factoryMandatory = new SLDSFilterMandatoryCategoryFactory<>();
		factoryMandatory.setClassifier(biCompDS.getClassifier())
			.setCategoryMapper(categoryMapper)
			.setActivities(activities);
		biCompDS.applyTransformation(factoryMandatory);
		// Mandatory activity projection -> Project remaining activities on vertex' activities
		SLDSProjectionCategoryFactory<T> factoryProjection = new SLDSProjectionCategoryFactory<>();
		factoryProjection.setClassifier(biCompDS.getClassifier())
			.setCategoryMapper(categoryMapper)
			.setActivities(activities);
		biCompDS.applyTransformation(factoryProjection);
		// Add caching
		biCompDS.ensureCaching();
		return biCompDS;
	}

	/**
	 * Creates a cached variant log by applying the <b>selection</b> transformation associated with this vertex.
	 * @param <T>
	 * @param biCompDS Log that will be transformed
	 * @return Transformed (i.e., the vertex-specific transformation) log 
	 * @throws SLDSTransformerBuildingException Error when transformation fails
	 */
	public<T extends CVariant> BiComparisonDataSource<T> createLogVertexSelection(BiComparisonDataSource<T> biCompDS) 
			throws SLDSTransformerBuildingException {
		// Create copy of datasource
		biCompDS = new BiComparisonDataSource<>(biCompDS);
		// Reduce data on vertex sublog
		// Mandatory activity filtering -> Each trace must contain the vertex' activities
		SLDSFilterMandatoryCategoryFactory<T> factoryMandatory = new SLDSFilterMandatoryCategoryFactory<>();
		factoryMandatory.setClassifier(biCompDS.getClassifier())
			.setCategoryMapper(categoryMapper)
			.setActivities(activities);
		biCompDS.applyTransformation(factoryMandatory);
		// Add caching
		biCompDS.ensureCaching();
		return biCompDS;
	}
	
	

	/**
	 * Return the itemset in human readable form.
	 * 
	 * Creates array of translated activity codes.
	 * @return Array of translated activities in itemset.
	 */
	public String[] getItemsetHumanReadable() {
		return this.activities.stream()
				.mapToObj(c -> categoryMapper.getActivity4Category(c))
				.toArray(String[]::new);
	}

	////////////////////////////////////////////////////////////
	// ---------- Getter and Setter ----------
	////////////////////////////////////////////////////////////
	public void addMeasurement(HFDDMeasurement m) {
		this.measurements.put(m.getPerspectiveDescription(), m);
		if (this.baseMeasurement == null) {
			this.baseMeasurement = m;
		}
	}
	
	public void setBaseMeasurement(HFDDMeasurement baseMeasurement) {
		this.baseMeasurement = baseMeasurement;
	}
	
	public HFDDMeasurement getBaseMeasurement() {
		return this.baseMeasurement;
	}

	public BitSet getActivities() {
		return activities;
	}

	public BitSet getActivitiesCopy() {
		return (BitSet) activities.clone();
	}

	public double getProbabilityLeft() {
		return this.baseMeasurement.getProbLeftNonEmpty();
	}

	public double getProbabilityRight() {
		return this.baseMeasurement.getProbRightNonEmpty();
	}

	public CategoryMapper getCategoryMapper() {
		return categoryMapper;
	}

	public Map<PerspectiveDescriptor, HFDDMeasurement> getMeasurements() {
		return measurements;
	}

}
