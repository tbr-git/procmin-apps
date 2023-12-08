package de.rwth.processmining.tb.core.sps.data.abstraction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ComparisonAbstraction {
	private final static Logger logger = LogManager.getLogger( ComparisonAbstraction.class );
	
	/**
	 * Abstraction to be applied to the left event log.
	 */
	private AbstractionSpecification abstractionLeft;
	
	/**
	 * Abstraction to be applied to the right event log.
	 */
	private AbstractionSpecification abstractionRight;
	
	/**
	 * Flag if the abstraction for the left and right hand side are equal.
	 */
	private boolean leftRightAbstractionEqual;
	
	/**
	 * Default constructor. Required by jackson.
	 */
	public ComparisonAbstraction() {
		abstractionLeft = null;
		abstractionRight = null;
		leftRightAbstractionEqual = true;
	}


	/**
	 * Constructor that applies a single abstraction to "both" logs.
	 * 
	 * Applies to:
	 * Left log: Free delete
	 * Right log: Free insert
	 * Both logs: Free rename
	 * 
	 * @param abstractionSpec Abstraction specification to be applied 
	 */
	public ComparisonAbstraction(AbstractionSpecification abstractionSpec) {
		super();
		switch(abstractionSpec.getAbstractionType()) {
		case FREEDELETE:
			this.abstractionLeft = abstractionSpec;
			this.abstractionRight = null;
			this.leftRightAbstractionEqual = false;
			break;
		case FREEINSERT:
			this.abstractionLeft = null;
			this.abstractionRight = abstractionSpec;
			this.leftRightAbstractionEqual = false;
			break;
		case FREERENAME:
			this.abstractionLeft = abstractionSpec;
			this.abstractionRight = abstractionSpec;
			this.leftRightAbstractionEqual = true;
			break;
		default:
			this.abstractionLeft = null;
			this.abstractionRight = null;
			this.leftRightAbstractionEqual = false;
			logger.error("Unknown Abstraction specification. Cannot create comparsion abstraction! The created comparison construction won't do anything");
		}
	}

	/**
	 * Constructor that should be used if the data abstraction for the left and right log differ 
	 * (i.e., only for free rename).
	 * 
	 * Should only be used for free rename and the abstraction id should be the same for both abstractions.
	 * If above recommendation is violated, this will be ignored but a warning is issued.
	 * 
	 * 
	 * @param abstractionLeft Abstraction to be applied to the left data source
	 * @param abstractionRighpt Abstraction to be applied to the right data source

	 */
	public ComparisonAbstraction(AbstractionSpecification abstractionLeft, AbstractionSpecification abstractionRight) {
		super();
		/* If we model free rename via free insert delete, 
		 * different abstractions and ids make sense.
		 * <A,B,C,D> -> <A,B,C,E>:
		 * If we use rename only, <A,B,C,D> vs <> still receives a high score
		 */
		// Abstraction ids should match and type should be rename. 
		//if ((abstractionLeft.getAbstractionID() != abstractionRight.getAbstractionID()) || 
		//		!(abstractionLeft.getAbstractionType() == AbstractionType.FREERENAME)) {
		//	logger.warn("Abstraction ids or tpyes do not match. However, I'll ignore the mismatch");
		//}
		this.abstractionLeft = abstractionLeft;
		this.abstractionRight = abstractionRight;
		this.leftRightAbstractionEqual = abstractionLeft.equals(abstractionRight);
	}

	/**
	 * Do we have to adapt the left data source?
	 * @return True iff we need to adapt the left data source
	 */
	public boolean requiresDataAbstractionLeft() {
		return this.abstractionLeft != null;
	}

	/**
	 * Do we have to adapt the right data source?
	 * @return True iff we need to adapt the right data source
	 */
	public boolean requiresDataAbstractionRight() {
		return this.abstractionRight != null;
	}

	@JsonIgnore
	public AbstractionType getAbstractionType() {
		if (this.abstractionLeft != null) {
			return this.abstractionLeft.getAbstractionType();
		}
		else {
			return this.abstractionRight.getAbstractionType();
		}
	}

	@JsonIgnore
	public int getAbstractionID() {
		if (this.abstractionLeft != null) {
			return this.abstractionLeft.getAbstractionID();
		}
		else {
			return this.abstractionRight.getAbstractionID();
		}
	}

	public AbstractionSpecification getAbstractionLeft() {
		return abstractionLeft;
	}

	public void setAbstractionLeft(AbstractionSpecification abstractionLeft) {
		this.abstractionLeft = abstractionLeft;
	}

	public AbstractionSpecification getAbstractionRight() {
		return abstractionRight;
	}

	public void setAbstractionRight(AbstractionSpecification abstractionRight) {
		this.abstractionRight = abstractionRight;
	}

	public boolean isLeftRightAbstractionEqual() {
		return leftRightAbstractionEqual;
	}

	public void setLeftRightAbstractionEqual(boolean leftRightAbstractionEqual) {
		this.leftRightAbstractionEqual = leftRightAbstractionEqual;
	}
	

}
