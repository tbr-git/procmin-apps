package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.prefixspan;

import java.util.ArrayList;
import java.util.List;



/**
 * Implementation of a sequence database, where each sequence is implemented
 * as a list of integers and should have a unique id.
*
 * @author Philipe-Fournier-Viger
 */
public class SequenceDatabase {

	/** a matrix to store the sequences in this database */
	protected List<WeightedSequence> sequences = new ArrayList<>();

	/** the total number of item occurrences in this database
	 * (variable to be used for statistics) */
	protected long itemOccurrenceCount = 0;
	
	public SequenceDatabase()  {
		
	}
	
	public void addSequence(WeightedSequence s) {
		sequences.add(s);
	}

	/**
	 * Print this sequence database to System.out.
	 */
	public void print() {
		System.out.println("============  SEQUENCE DATABASE ==========");
		System.out.println(toString());
	}
	
	/**
	 * Print statistics about this database.
	 */
	public void printDatabaseStats() {
		System.out.println("============  STATS ==========");
		System.out.println("Number of sequences : " + sequences.size());
		
		// Calculate the average size of sequences in this database
		double meansize = ((float)itemOccurrenceCount) / ((float)sequences.size());
		System.out.println("mean size" + meansize);
	}

	/**
	 * Return a string representation of this sequence database.
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		// for each sequence
		for (int i=0; i < sequences.size(); i++) { 
			buffer.append(i + ":  ");
			
			// get that sequence
			int[] sequence = sequences.get(i).getSequence();
			
			// for each token in that sequence (items, or separators between items)
			// we will print it in a human-readable way
			
			boolean startingANewItemset = true;
			for(int token : sequence){
				// if it is an item
				if(token >=0){
					// if this is a new itemset, we start with a parenthesis
					if(startingANewItemset == true){
						startingANewItemset = false;
						buffer.append("(");
					}else{
						// otherwise we print a space
						buffer.append(" ");
					}
					// then we print the item
					buffer.append(token);
					
					// increase the number of item occurrences for statistics
					itemOccurrenceCount++;
				}else if(token == -1){
					// if it is an itemset separator
					buffer.append(")");
					// remember that we have just finished reading a full itemset
					startingANewItemset = true;
				}else if(token == -2){
					// if it is the end of the sequence we break, in case there
					// would be something stored after in the array.
					break;
				}
			}
		
			// print each item print eac
			buffer.append(System.lineSeparator());
		}
		return buffer.toString();
	}
	
	/**
	 * Get the sequence count in this database.
	 * @return the sequence count.
	 */
	public int size() {
		return sequences.size();
	}
	
	/**
	 * Get the sequences from this sequence database.
	 * @return A list of sequences (int[]) in SPMF format.
	 */
	public List<WeightedSequence> getSequences() {
		return sequences;
	}

}
