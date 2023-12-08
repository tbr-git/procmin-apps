package de.rwth.processmining.tb.core.sps.algorithm.iteration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cern.colt.Arrays;
import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.abstractions.SLDSAbstractConditionEffectSetFactory;
import de.rwth.processmining.tb.core.data.variantlog.abstraction.CVariantAbst;
import de.rwth.processmining.tb.core.emd.grounddistances.controlflow.AdaptiveLVS;
import de.rwth.processmining.tb.core.sps.algorithm.measure.DependantVertexFinder;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexCondition;
import de.rwth.processmining.tb.core.sps.algorithm.measure.VertexConditionType;
import de.rwth.processmining.tb.core.sps.data.abstraction.ComparisonAbstraction;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;

/**
 * Specification that describes and instantiates a single HFDD iteration.
 * 
 * @author brockhoff
 *
 */
public class HFDDIterationRefined<E extends CVariantAbst> extends HFDDIterationBase<E> {
	private final static Logger logger = LogManager.getLogger( HFDDIterationRefined.class );
	
	private HFDDIterationBase<E> parentIteration;

	/**
	 * The abstractions that will be applied to create this perspective and
	 * run this iteration.
	 */
	private final List<ComparisonAbstraction> appliedAbstractions;
	
	/**
	 * Aggregated (over previous iterations) data-based conditional occurrence base.
	 */
	private Optional<ArrayList<Set<VertexCondition>>> aggDataCBase;
	
	/**
	 * Vertex to condition on in this iteration, if there is any.
	 */
	private Optional<HFDDVertex> condVertex;
	
	/**
	 * Type of the condition. How fill {@link this#condVertex} affect the iteration.
	 */
	private Optional<VertexConditionType> conditionType;
	
	/**
	 * To find vertices that are fully dependent on {@link this#condVertex}, 
	 * we allow for this imprecision in terms of probability mass covered.
	 */
	private Optional<Double> condMaxPropCoverLoss;
	
	public HFDDIterationRefined(HFDDIterationBase<E> parentIteration, int iteration,
			List<ComparisonAbstraction> appliedAbstractions, Optional<HFDDVertex> condVertex, 
			Optional<VertexConditionType> conditionType, Optional<Double> condMaxPropCoverLoss) {
		super(parentIteration.hfddGraph, iteration, null);
		this.parentIteration = parentIteration;
		this.appliedAbstractions = appliedAbstractions;
		this.aLVS = null;
		this.condVertex = condVertex;
		this.aggDataCBase = Optional.empty();
		this.condMaxPropCoverLoss = condMaxPropCoverLoss;
		this.conditionType = conditionType;
	}

	public HFDDIterationRefined(HFDDIterationBase<E> parentIteration, int iteration,
			List<ComparisonAbstraction> appliedAbstractions) {
		this(parentIteration, iteration, appliedAbstractions, 
				Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	/**
	 * Initialize the adaptive LVS distance.
	 * @return True if initialization succeeded
	 */
	@Override
	public boolean initializeDistance() {
		// Apply abstractions recursively
		if (this.parentIteration != null) {
			this.aLVS = new AdaptiveLVS(this.parentIteration.getAdaptiveLVS());
		}
		else {
			logger.warn("Usually this hfdd iteration should have "
					+ "a parent iteration (at least the base iteration. "
					+ "Fallback to fresh adaptive LVS");
			this.aLVS = new AdaptiveLVS();

		}
		// Add abstractions for this iteration
		this.addAbstractions2Dist(aLVS);
		return true;
	}
	
	@Override
	public boolean prepareDataSource() {
		if (this.preparedCompDS == null) { 			// Data not prepared yet
			if (this.parentIteration == null) {
				logger.error("Failed to prepare the datasource. Can't get from parent");
				return false;
			}
			else {
				this.preparedCompDS = new BiComparisonDataSource<>(this.parentIteration.getPreparedCompDS());
				try {
					applyAbstractionToData(this.preparedCompDS);
				} catch (SLDSTransformerBuildingException e) {
					e.printStackTrace();
					logger.error("Failed to prepare the data source for the HFDD iteration: {}", this.getpDesc().toString());
					return false;
				}
				return true;
			}
		}
		else {
			return true;
		}
	}
		
	private void addAbstractions2Dist(AdaptiveLVS aLVS) {
		// If there are not abstractions for this vertex
		// Do nothing and return standard LVS
		if (appliedAbstractions == null || appliedAbstractions.size() == 0) {
			return;
		}
		// Apply Abstractions
		for(ComparisonAbstraction spec : appliedAbstractions) {
			switch(spec.getAbstractionType()) {
				case FREEDELETE:
					aLVS.addFreeDelete(spec.getAbstractionID());
					break;
				case FREEINSERT:
					aLVS.addFreeInsert(spec.getAbstractionID());
					break;
				case FREERENAME:
					aLVS.addFreeRename(spec.getAbstractionID());
					break;
				case FREETRACEDELETE:
					logger.warn("Unexpected use of FREETRACEDELETE");
					aLVS.conditionFreeTraceDelete(spec.getAbstractionLeft().getConditionActivities());
					break;
				case FREETRACEINSERT:
					logger.warn("Unexpected use of FREETRACEINSERT");
					aLVS.conditionFreeTraceInsert(spec.getAbstractionRight().getConditionActivities());
					break;
				default:
					logger.error("Unknown abstraction type. Ignoring it");
					break; 
			}
		}
	}
	
	private boolean applyAbstractionToData(BiComparisonDataSource<E> biCompDS) throws SLDSTransformerBuildingException {
		// If there are no abstractions
		// Do nothing and return 
		if (appliedAbstractions == null || appliedAbstractions.size() == 0) {
			return true;
		}
		// Apply Abstractions
		for(ComparisonAbstraction spec : appliedAbstractions) {
			SLDSAbstractConditionEffectSetFactory<E> abstractionAddFactory = null;
			// Requires that the abstraction is applied to the left data source
			if (spec.requiresDataAbstractionLeft()) {
				abstractionAddFactory = new SLDSAbstractConditionEffectSetFactory<>();
				abstractionAddFactory.setConditionSet(spec.getAbstractionLeft().getConditionActivities());		// Condition set
				abstractionAddFactory.setEffectSet(spec.getAbstractionLeft().getAffectedActivities());			// Effect set
				abstractionAddFactory.setAbstractionCode(spec.getAbstractionID());								// Abstraction id
				biCompDS.applyTransformationLeft(abstractionAddFactory);										// Apply
			}
			if (spec.requiresDataAbstractionRight()) {
				// Skip if  factory is already initialized and can be used for the right data source as well
				if (abstractionAddFactory == null || !spec.isLeftRightAbstractionEqual()) {
					abstractionAddFactory = new SLDSAbstractConditionEffectSetFactory<>();
					abstractionAddFactory.setConditionSet(spec.getAbstractionRight().getConditionActivities());		// Condition set
					abstractionAddFactory.setEffectSet(spec.getAbstractionRight().getAffectedActivities());			// Effect set
					abstractionAddFactory.setAbstractionCode(spec.getAbstractionID());								// Abstraction id
				}
				biCompDS.applyTransformationRight(abstractionAddFactory);
			}
		}
		return true;
	}
	
	public List<ComparisonAbstraction> getAppliedAbstractions() {
		return appliedAbstractions;
	}

	public Optional<ArrayList<Set<VertexCondition>>> getAggDataCBase(boolean copy) {
		// If not cached, get from parents and build new
		if (this.aggDataCBase.isEmpty()) {
			Optional<ArrayList<Set<VertexCondition>>> optParentAggDataCBase = 
					this.parentIteration.getAggDataCBase(true);
			
			// Do we need to add something
			if (this.condVertex.isPresent()) {
			
				// Get / create list to add this condition vertex to
				ArrayList<Set<VertexCondition>> buildAggDataCBase = null;
				if (optParentAggDataCBase.isEmpty()) {
					buildAggDataCBase = new ArrayList<>(this.hfddGraph.getNbrVertices());
					//Fill with non-existent per vertex lists
					// -> Afterwards you can at least query this position without
					// and index out of bounds exception (result will be null)
					for (int i = 0; i < this.hfddGraph.getNbrVertices(); i++) {
						buildAggDataCBase.add(null);
					}
				}
				else {
					buildAggDataCBase = optParentAggDataCBase.get();
				}
				
				try {
					HFDDVertex condVertex = this.condVertex.get();
					// Find dependent vertices
					Set<HFDDVertex> dependentVertices = DependantVertexFinder.findDependantVertices(hfddGraph, condVertex, 
							getPreparedCompDS(), condMaxPropCoverLoss.get());
					
					// Add dependent vertices to data-based condition/dependency base
					for (HFDDVertex v: dependentVertices) {
						Set<VertexCondition> s = buildAggDataCBase.get(v.getId());
						if (s == null) {
							// So far, this vertex did not depend on any of the selected vertices 
							// Add new set
							s = new HashSet<VertexCondition>();
							buildAggDataCBase.set(v.getId(), s);
						}
						// Add vertex to set (side effect)
						s.add(new VertexCondition(condVertex, conditionType.get()));
					}
				} catch (SLDSTransformerBuildingException | SLDSTransformationError e) {
					e.printStackTrace();
					logger.error("Failed to find vertices dependant on {}: {} - Continue working with what we have", 
							this.condVertex.get().getId(),
							Arrays.toString(this.condVertex.get().getVertexInfo().getItemsetHumanReadable()));
				}
				return Optional.of(buildAggDataCBase);
			}
			else {
				// Nothing to add, return parent copy
				return optParentAggDataCBase;
			}
		}
		else {
			// We already have cached it -> create copy if necessary
			if (copy) {
				ArrayList<Set<VertexCondition>> thisBase = this.aggDataCBase.get();
				ArrayList<Set<VertexCondition>> copyBase = new ArrayList<>(
						this.hfddGraph.getNbrVertices());
				for (int i = 0; i < copyBase.size(); i++) {
					// If there are vertices, copy and put
					if (thisBase.get(i) != null) {
						copyBase.set(i, new HashSet<>(thisBase.get(i)));
					}
				}
				return Optional.of(copyBase);
			}
			else {
				return this.aggDataCBase;
			}

			
		}
	}

	public Set<HFDDVertex> getConditionVertices(VertexConditionType conditionType) {
		Set<HFDDVertex> s = parentIteration.getConditionVertices(conditionType);
		if (this.conditionType.isPresent() && this.conditionType.get().equals(conditionType)) {
			s.add(this.condVertex.get());
		}
		return s;
	}
}
