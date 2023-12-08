package de.rwth.processmining.tb.core.sps.data.abstraction;

/**
 * Abstraction types used for the difference-ignoring LVS.
 * 
 * @author brockhoff
 *
 */
public enum AbstractionType {
	FREEDELETE, 		// Zero cost deletion of letter
	FREEINSERT,			// Zero cost insertion of letter
	FREERENAME,			// Zero cost renaming of letter
	FREETRACEDELETE, 	// Zero cost for deleting the entire trace
	FREETRACEINSERT; 	// Zero cost for inserting the entire trace
}
