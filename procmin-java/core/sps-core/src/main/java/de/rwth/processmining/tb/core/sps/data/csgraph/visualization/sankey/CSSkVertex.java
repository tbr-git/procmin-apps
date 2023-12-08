package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey;

import java.util.Comparator;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type"
)
@JsonSubTypes({
	@Type(value = CSSkFlowSplit.class, name = "flowSplit"),
	@Type(value = CSSkItemsetVertex.class, name = "itemset"),
	@Type(value = CSSkRoot.class, name = "root"),
	@Type(value = CSSkTraceVertex.class, name = "trace"),
	@Type(value = CSSkTraceWithAbractionsVertex.class, name = "traceAbstract")
})
public class CSSkVertex {
	
	/**
	 * Vertex id.
	 * 
	 * <b> Hashing and equality will be tested based on that! </b>
	 */
	private final int id;

	/**
	 * Is related to the left log?
	 * 
	 * Otherwise, considered to be related to the right log
	 */
	private boolean isLeft;
	
	/**
	 * Associated probability mass.
	 */
	private double probabilityMass;

	/**
	 * Associated residual probability mass.
	 */
	private double residualProbabilityMass;
	
	/**
	 * Handle to the {@link CSGraphVertex} that is associated to this vertex.
	 */
	@JsonIgnore
	private CSGraphVertex csGraphVertex;
	
	/**
	 * Level in the Sankey diagram.
	 */
	private int skLevel;
	
	/**
	 * Intra level sorting key for the Sankey diagram.
	 */
	private int intraLevelKey;
	
	/**
	 * Is this vertex relevant for the matching-based visualization.
	 * We may want to remove empty traces/flows that are only between 
	 * empty traces (defaults to true).
	 */
	private boolean matchingRelevant;
	
	
	public CSSkVertex(int id, boolean isLeft, double probabilityMass, double residualProbabilityMass, CSGraphVertex csGraphVertex) {
		super();
		this.id = id;
		this.isLeft = isLeft;
		this.probabilityMass = probabilityMass;
		this.residualProbabilityMass = residualProbabilityMass;
		this.csGraphVertex = csGraphVertex;
		this.skLevel = -1;
		this.intraLevelKey = -1;
		this.matchingRelevant = true;
	}

	@Override
	public String toString() {
		return String.format("CSSkVertex(id=%d, isLeft=%b, probMass=%f, refId=%d)", 
				this.id, this.isLeft, this.probabilityMass, this.csGraphVertex.getHfddVertexRef().getId());
	}
	
	/**
	 * Format the activities that are potentially associated with this vertex.
	 * @param itemSorter Comparator that can be used to sort activities.
	 * @param activityAbbrev Function that maps activities to abbreviations.
	 */
	public void formatActivities(Comparator<String> itemSorter, Function<String, String> activityAbbrev) { }

	//================================================================================
	// Getters and Setters
	//================================================================================

	public boolean isLeft() {
		return isLeft;
	}

	public double getProbabilityMass() {
		return probabilityMass;
	}

	public CSGraphVertex getCsGraphVertex() {
		return csGraphVertex;
	}

	public int getId() {
		return id;
	}

	public double getResidualProbabilityMass() {
		return residualProbabilityMass;
	}

	public void setResidualProbabilityMass(double residualProbabilityMass) {
		this.residualProbabilityMass = residualProbabilityMass;
	}

	public void setProbabilityMass(double probabilityMass) {
		this.probabilityMass = probabilityMass;
	}

	public int getSkLevel() {
		return skLevel;
	}

	public void setSkLevel(int skLevel) {
		this.skLevel = skLevel;
	}

	public int getIntraLevelKey() {
		return intraLevelKey;
	}

	public void setIntraLevelKey(int intraLevelKey) {
		this.intraLevelKey = intraLevelKey;
	}

	public boolean isMatchingRelevant() {
		return matchingRelevant;
	}

	public void setMatchingRelevant(boolean matchingRelevant) {
		this.matchingRelevant = matchingRelevant;
	}
	
}
