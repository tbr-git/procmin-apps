package de.rwth.processmining.tb.core.sps.data.abstraction;

import java.util.Arrays;

/**
 * Specification of an abstraction for difference-ignoring Levenshtein.
 * @author brockhoff
 *
 */
public class AbstractionSpecification {
	
	/**
	 * Type of the instruction.
	 */
	private AbstractionType abstractionType;
	
	/**
	 * Categories that a trace must contain to be affected by this abstraction.
	 */
	private int[] conditionActivities;
	
	/**
	 * If an abstraction is applied to a trace, these activities will be affected 
	 * (i.e., {@link #abstractionID} is assigned to these categories)
	 */
	private int[] affectedActivities;
	
	/**
	 * Abstraction identifier.
	 */
	private int abstractionID;
	
	public AbstractionSpecification() {
		conditionActivities = null;
		abstractionID = -1;
		abstractionType = null;
		this.affectedActivities = null;
	}

	public AbstractionSpecification(AbstractionType abstractionType, int[] mandatoryActivities,
			int[] affectedActivities, int abstractionID) {
		super();
		this.abstractionType = abstractionType;
		this.conditionActivities = mandatoryActivities;
		this.affectedActivities = affectedActivities;
		this.abstractionID = abstractionID;
	}
	
	@Override
	public boolean equals(Object obj) {
        if (this == obj) {
        	return true;
        }
        if (this.getClass() == this.getClass()) {
        	AbstractionSpecification otherAbstraction = (AbstractionSpecification) obj;
        	if (this.getAbstractionID() == otherAbstraction.getAbstractionID() &&	// Id
        			this.abstractionType == otherAbstraction.getAbstractionType() &&	// Type
        			Arrays.equals(this.getConditionActivities(), 
        					otherAbstraction.getConditionActivities()) &&	// Condition activities
        			Arrays.equals(this.getAffectedActivities(), 
        					otherAbstraction.getAffectedActivities())) {	// Affected activities
        		return true;
        	}
        }
        return false;
	}

	//================================================================================
	// Getter and Setter
	//================================================================================
	public AbstractionType getAbstractionType() {
		return abstractionType;
	}

	public void setAbstractionType(AbstractionType abstractionType) {
		this.abstractionType = abstractionType;
	}

	public int[] getConditionActivities() {
		return conditionActivities;
	}

	public void setConditionActivities(int[] mandatoryActivities) {
		this.conditionActivities = mandatoryActivities;
	}

	public int getAbstractionID() {
		return abstractionID;
	}

	public void setAbstractionID(int abstractionID) {
		this.abstractionID = abstractionID;
	}

	public int[] getAffectedActivities() {
		return affectedActivities;
	}

	public void setAffectedActivities(int[] affectedActivities) {
		this.affectedActivities = affectedActivities;
	}


}
