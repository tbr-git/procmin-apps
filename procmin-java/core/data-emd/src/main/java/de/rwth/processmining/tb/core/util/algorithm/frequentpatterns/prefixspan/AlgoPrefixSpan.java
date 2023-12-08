package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.prefixspan;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/*** 
 * This is a 2016 implementation of the PrefixSpan algorithm.
 * PrefixSpan was proposed by Pei et al. 2001.
 * 
 * NOTE: This implementation saves the pattern  to a file as soon 
 * as they are found or can keep the pattern into memory, depending
 * on what the user choose.
 * 
 * This implementation was done in 2016. It is different than the previous implementation
 * in SPMF which was implemented in 2008, and the AGP implementation. I have re-implemented the code to make
 * it more efficient. This new implementation can be 10 times faster than the 2008 implementation, since
 * I have added more optimizations
 *
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 */

public class AlgoPrefixSpan{
		
	/** for statistics **/
	long startTime;
	long endTime;
	
	/** the number of pattern found */
	int patternCount;
	
	/** absolute minimum support */
//	private int minsuppAbsolute;
	
	private double minSuppWeight;	

	/** writer to write output file */
	BufferedWriter writer = null;
	
	/** The sequential patterns that are found  (if the user want to keep them into memory) */
	private SequentialPatterns patterns = null;
	
	/** maximum pattern length in terms of item count */
	private int maximumPatternLength = 1000;
	
	/** if true, sequence identifiers of each pattern will be shown*/
	boolean showSequenceIdentifiers = false;
	
	/** buffer for storing the current pattern that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. **/
	final int BUFFERS_SIZE = 2000;
	private int[] patternBuffer = new int[BUFFERS_SIZE];
	
	/** original sequence count **/
	int sequenceCount = 0;
	
	/** the sequence database **/
	SequenceDatabase sequenceDatabase;
	
	/** boolean indicating whether this database contains itemsets with multiple items or not */
	boolean containsItemsetsWithMultipleItems = false;
	
	/**
	 * Default constructor
	 */
	public AlgoPrefixSpan(){
	}
	

	public SequentialPatterns runAlgorithm(SequenceDatabase sequenceDatabase, double minsupWeight) {
		// record start time
		startTime = System.currentTimeMillis();
		
		// Load the sequence database
		this.sequenceDatabase = sequenceDatabase;
		sequenceCount = sequenceDatabase.size();
		
		// convert to a absolute minimum support
//		this.minsuppAbsolute = (int) Math.ceil(minsupRelative * sequenceCount);
//		if(this.minsuppAbsolute == 0){ // protection
//			this.minsuppAbsolute = 1;
//		}
		this.minSuppWeight = minsupWeight;
		
		// run the algorithm
		try {
			prefixSpan(sequenceDatabase, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sequenceDatabase = null;
		
		// record end time
		endTime = System.currentTimeMillis();
		return patterns;
	}
	
	
	/**
	 * This is the main method for the PrefixSpan algorithm that is called
	 * to start the algorithm
	 * @param outputFilePath  an output file path if the result should be saved to a file
	 *                        or null if the result should be saved to memory.
	 * @param sequenceDatabase a sequence database
	 * @throws IOException exception if an error while writing the output file
	 */
	private void prefixSpan(SequenceDatabase sequenceDatabase, String outputFilePath) throws IOException{
		// if the user want to keep the result into memory
		if(outputFilePath == null){
			writer = null;
			patterns = new SequentialPatterns("FREQUENT SEQUENTIAL PATTERNS");
		}else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		}
		
		sequenceCount = sequenceDatabase.size();
		
		//============== CALCULATE FREQUENCY OF SINGLE ITEMS =============
		// We have to scan the database to find all frequent sequential patterns of size 1.
		// We note the sequences in which the items appear.
		Map<Integer, List<IndexWeightPair>> mapSequenceID = findSequencesContainingItems();

		//====== Remove infrequent items and explore each projected database ================
		// if this database have multiple items per itemset
		prefixspanWithSingleItems(mapSequenceID);	
//		if(containsItemsetsWithMultipleItems){
//			prefixspanWithMultipleItems(mapSequenceID);	
//		}else{
//			// if this database does not have multiple items per itemset
//			// we use an optimize version of the same code
//			prefixspanWithSingleItems(mapSequenceID);	
//		}
	}
	
	/**
	 * Remove infrequent items and explore each projected databas for itemsets of size 1
	 * @param mapSequenceID the set of items with their frequencies
	 * @throws IOException if error writing to file
	 */
	private void prefixspanWithSingleItems(
			Map<Integer, List<IndexWeightPair>> mapSequenceID) throws IOException {
		//=============== REMOVE INFREQUENT ITEMS ========================
		// We scan the database to remove infrequent items  and resize sequences after removal
		// for each sequence in the current database
		for(int i=0; i < sequenceDatabase.size(); i++){
			WeightedSequence wSequence = sequenceDatabase.getSequences().get(i);
			int[] sequence  = wSequence.getSequence();
			
			// we will copy the frequent items one by one but not those items that are infrequent
			// The following variable will be used to remember the position were to copy (we start at 0).
			int currentPosition = 0;
			
			// for each token in this sequence (item, separator between itemsets (-1) or end of sequence (-2)
			for(int j =0; j < sequence.length; j++){
				int token = sequence[j];
				
				// if it is an item
				if(token > 0){
					boolean isFrequent = mapSequenceID.get(token).stream().map(p -> p.weight).reduce(0.0, Double::sum) >= minSuppWeight;
					
					// if the item is frequent
					if(isFrequent){
						// copy the item to the current position
						sequence[currentPosition] = token;
						// increment the current position
						currentPosition++;
					}	
				}else if(token == -2){
					// if the sequence is not empty after having removed the infrequent items
					if(currentPosition >0){
						// copy the item to the current position
						sequence[currentPosition] = -2;
						
						// now replace the previous array with the new array
						int[] newSequence = new int[currentPosition+1];
						System.arraycopy(sequence, 0, newSequence, 0, currentPosition+1);
//						sequenceDatabase.getSequences().set(i, newSequence);
						wSequence.setSequence(newSequence);
						// continue to next sequence
						continue; 
					}else{
						// if the sequence is  empty, delete this sequence by replacing it with null
						sequenceDatabase.getSequences().set(i, null);
					}
				}
			}
		}
		
		//============= WE EXPLORE EACH PROJECTED DATABASE  ================================
		// For each frequent item
		for(Entry<Integer, List<IndexWeightPair>> entry : mapSequenceID.entrySet()){
//			int support = entry.getValue().size();
			double weightedSupport = entry.getValue().stream().map(p -> p.weight).reduce(0.0, Double::sum);
			// if the item is frequent  (has a weighted support >= minSuppWeight)
			if(weightedSupport >= minSuppWeight){ 
				int item = entry.getKey();

				// The prefix is a frequent sequential pattern.
				// We save it in the result.
				savePattern(item, weightedSupport, entry.getValue());  

				// We make a recursive call to try to find larger sequential
				// patterns starting with this prefix
				if(maximumPatternLength >1){
					
					// Create the prefix for this projected database by copying the item in the buffer
					patternBuffer[0] = item;

					// build the projected database for that item
					List<PseudoSequence> projectedDatabase  = buildProjectedDatabaseSingleItems(item, entry.getValue());
					
					// recursive call
					recursionSingleItems(projectedDatabase, 2, 0); 
				}
			}
		}
	}


	/**
	 * This method saves a sequential pattern containing a single item to the output file or
	 * in memory, depending on if the user provided an output file path or not
	 * when he launched the algorithm
	 * @param item the pattern to be saved.
	 * @param support the support of this item
	 * @param sequenceIDs the list of sequences containing this item
	 * @throws IOException exception if error while writing the output file.
	 */
	private void savePattern(int item, double weightedSupport, List<IndexWeightPair> sequenceIDs) throws IOException {
		// increase the number of pattern found for statistics purposes
		patternCount++; 
	
		// if the result should be saved to a file
		if(writer != null){
			// create a StringBuilder
			StringBuilder r = new StringBuilder();
			r.append(item);
			r.append(" -1 #SUP: ");
			r.append(weightedSupport);
			if(showSequenceIdentifiers) {
	        	r.append(" #SID: ");
	        	for (IndexWeightPair sid: sequenceIDs) {
	        		r.append(sid.index);
	        		r.append(" ");
	        	}
			}
			// write the string to the file
			writer.write(r.toString());
			// start a new line
			writer.newLine();
		}
		// otherwise the result is kept into memory
		else{
			SequentialPattern pattern = new SequentialPattern();
			pattern.addItem(item);
			pattern.setSequenceIDs(sequenceIDs);
			patterns.addSequence(pattern, 1);
		}
	}
	
	/**
	 * Save a pattern containing two or more items to the output file (or in memory, depending on what the user prefer)
	 * @param lastBufferPosition the last position in the buffer for this pattern
	 * @param pseudoSequences the list of pseudosequences where this pattern appears.
	 * @param length the pattern length in terms of number of items.
	 * @throws IOException if error when writing to file
	 */
	private void savePattern(int lastBufferPosition, List<PseudoSequence> pseudoSequences) throws IOException {
		// increase the number of pattern found for statistics purposes
		patternCount++;  
	
		// if the result should be saved to a file
		if(writer != null){

			// create a StringBuilder
			StringBuilder r = new StringBuilder();
			for(int i=0; i <= lastBufferPosition; i++){
				r.append(patternBuffer[i]);
				r.append(" ");
			}
			//-------------------------------------
			// PHILIPPE: BUG FIX 2017-10 : some -1 were missing in the output file
			// for some patterns. This fixes the problem.
			if(patternBuffer[lastBufferPosition] != -1){
				r.append("-1 ");
			}
			//-------------------------------------
			r.append("#SUP: ");
			r.append(pseudoSequences.size());
			if(showSequenceIdentifiers) {
	        	r.append(" #SID: ");
	        	for (PseudoSequence sequence: pseudoSequences) {
	        		r.append(sequence.sequenceID);
	        		r.append(" ");
	        	}
			}
			// write the string to the file
			writer.write(r.toString());
			// start a new line
			writer.newLine();
		}
		// otherwise the result is kept into memory
		else{
			SequentialPattern pattern = new SequentialPattern();
			int itemsetCount  =0;
			int currentItem = -1;
			for(int i=0; i <= lastBufferPosition; i++){
				int token = patternBuffer[i];
				if(token>0){
					currentItem = token;
				}else if(token == -1){
					pattern.addItem(currentItem);
					itemsetCount++;
				}
			}
			pattern.addItem(currentItem);
			itemsetCount++;
			
			List<IndexWeightPair> sequencesIDs = new ArrayList<IndexWeightPair>(pseudoSequences.size());
			for (int i = 0; i< pseudoSequences.size(); i++) {
				sequencesIDs.add(new IndexWeightPair(pseudoSequences.get(i).sequenceID, pseudoSequences.get(i).seqWeight));
        	}
			pattern.setSequenceIDs(sequencesIDs);
//			System.out.println(pattern);
			patterns.addSequence(pattern, itemsetCount);
		}
	}
	
	
	/**
	 * For each item, calculate the sequence id of sequences containing that item
	 * @return Map of items to sequence IDs that contains each item
	 */
	private Map<Integer, List<IndexWeightPair>> findSequencesContainingItems() {
		// number of items in the current itemset 
		int itemCountInCurrentItemset;
		// We use a map to store the sequence IDs where an item appear
		// Key : item   Value :  a set of sequence IDs
		Map<Integer, List<IndexWeightPair>> mapSequenceID = new HashMap<Integer, List<IndexWeightPair>>(); 
		// for each sequence in the current database
		for(int i=0; i < sequenceDatabase.size(); i++){
			int[] sequence  = sequenceDatabase.getSequences().get(i).getSequence();
			
			 itemCountInCurrentItemset = 0;
			 
			// for each token in this sequence (item, separator between itemsets (-1) or end of sequence (-2)
			for(int token : sequence){
				// if it is an item
				if(token > 0){
					// get the set of sequence IDs for this item until now
					List<IndexWeightPair> sequenceIDs = mapSequenceID.get(token);
					if(sequenceIDs == null){
						// if the set does not exist, create one
						sequenceIDs = new ArrayList<IndexWeightPair>();
						mapSequenceID.put(token, sequenceIDs);
					}
					// add the sequence ID to the 
					// set of sequences IDs of this item
					// if it is not already there
					if(sequenceIDs.size() == 0 || sequenceIDs.get(sequenceIDs.size() - 1).index != i){
						sequenceIDs.add(new IndexWeightPair(i, sequenceDatabase.getSequences().get(i).getWeight()));
					}
					itemCountInCurrentItemset++;
					// if this itemset contains more than 1 item, we will remember that this database
					// contains sequence with multiple items for optimization purpose.
					if(itemCountInCurrentItemset > 1){
						containsItemsetsWithMultipleItems = true;
					}
				}else if(token == -1){
					itemCountInCurrentItemset = 0;
				}
			}
		}
//		System.out.println(mapSequenceID);
		return mapSequenceID;
	}
	
	/**
	 * Create a projected database by pseudo-projection with the initial database and a given item.
	 * @param item The item to use to make the pseudo-projection
	 * @param sequenceDatabase2 The current database.
	 * @param list  The set of sequence ids containing the item
	 * @return the projected database.
	 */
	private List<PseudoSequence> buildProjectedDatabaseSingleItems(int item, List<IndexWeightPair> sequenceIDs) {
		// We create a new projected database
		List<PseudoSequence> projectedDatabase = new ArrayList<PseudoSequence>();

		// for each sequence that contains the current item
loopSeq:for(IndexWeightPair sequenceID : sequenceIDs){ 
			int[] sequence = sequenceDatabase.getSequences().get(sequenceID.index).getSequence();
			
			// for each token in this sequence (item  or end of sequence (-2)
			for(int j=0; sequence[j] != -2; j++){
				int token = sequence[j];
				
				// if it is the item that we want to use for projection
				if(token == item){
					// if it is not the end of the sequence
					if(sequence[j+1] != -2){
						PseudoSequence pseudoSequence = new PseudoSequence(sequenceID.index, sequenceID.weight, j+1);
						projectedDatabase.add(pseudoSequence);
					}
					
					// break because we have found what we have created the pseudosequence for the current sequence
					continue loopSeq;
				}
			}
		}
		return projectedDatabase; // return the projected database
	}
	

	/**
	 * Method to recursively grow a given sequential pattern.
	 * @param database the current projected sequence database
	 * @param k  the prefix length in terms of items
	 * @param lastBufferPosition the last position used in the buffer for storing the current prefix
	 * @throws IOException exception if there is an error writing to the output file
	 */
	private void recursionSingleItems(List<PseudoSequence> database, int k, int lastBufferPosition) throws IOException {	
//		for(int i=0; i<= lastBufferPosition; i++){
//			System.out.print(patternBuffer[i] + " ");
//		}
//		System.out.println();
//		System.out.println("---");
		
		
		// find frequent items of size 1 in the current projected database, and at the same
		// time create their respective projected databases
		Map<Integer,List<PseudoSequence>> itemsPseudoSequences = findAllFrequentPairsSingleItems(database, lastBufferPosition);
		
		// release the memory used by the database
		database = null;
		
//		for(Pair pair : pairs){
//			System.out.print(pair.item + " isPostfix? " + pair.isPostfix() + "    " );
//			for(PseudoSequence seq: pair.getPseudoSequences()){
//				System.out.print( seq.getOriginalSequenceID() + "," + seq.indexFirstItem + " ");
//			}
//			System.out.println();
//		}
//		System.out.println("DEBUG");
	
		// For each pair found (a pair is an item with a boolean indicating if it
		// appears in an itemset that is cut (a postfix) or not, and the sequence IDs
		// where it appears in the projected database).
		for(Entry<Integer, List<PseudoSequence>> entry : itemsPseudoSequences.entrySet()){
			// if the item is frequent in the current projected database
			if(entry.getValue().stream().map(p -> p.seqWeight).reduce(0.0, Double::sum) >= minSuppWeight){

				//Create the new pattern by appending the item as a new itemset to the sequence
				patternBuffer[lastBufferPosition+1] = -1;
				patternBuffer[lastBufferPosition+2] = entry.getKey();
				
				// save the pattern
				savePattern(lastBufferPosition+2, entry.getValue());
				
				// make a recursive call
				if( k < maximumPatternLength){
					recursionSingleItems(entry.getValue(), k+1, lastBufferPosition+2);
				}
			}
		}
		
	}
	/**
	 * Method to recursively grow a given sequential pattern.
	 * @param database the current projected sequence database
	 * @param k  the prefix length in terms of items
	 * @param patternBuffer  the current sequential pattern that we want to try to grow
	 * @param lastBufferPosition the last position used in the buffer for storing the current prefix
	 * @throws IOException exception if there is an error writing to the output file
	 */
	private void recursion(int[] patternBuffer, List<PseudoSequence> database, int k, int lastBufferPosition) throws IOException {	
//		for(int i=0; i<= lastBufferPosition; i++){
//			System.out.print(patternBuffer[i] + " ");
//		}
//		System.out.println();
//		System.out.println("---");
		
		
		// FIND FREQUENT PAIRS
		// find frequent items of size 1 in the current projected database, and at the same
		// time create their respective projected databases
		// We create some maps of pairs for storing the frequent items. The following object
		// contains two maps. The first one is for item extending the current pattern as an i-extension,
		// while the second is for item extending the current pattern as an s-extension.
		MapFrequentPairs mapsPairs = findAllFrequentPairs(database, lastBufferPosition);
		
		// release the memory used by the database
		database = null;
		
//		for(Pair pair : pairs){
//			System.out.print(pair.item + " isPostfix? " + pair.isPostfix() + "    " );
//			for(PseudoSequence seq: pair.getPseudoSequences()){
//				System.out.print( seq.getOriginalSequenceID() + "," + seq.indexFirstItem + " ");
//			}
//			System.out.println();
//		}
//		System.out.println("DEBUG");
	
		// For each pair found that is in a postfix itemset(a pair is an item with a boolean indicating if it
		// appears in an itemset that is cut (a postfix) or not, and the sequence IDs
		// where it appears in the projected database).
		for(Entry<Pair,Pair> entry : mapsPairs.mapPairsInPostfix.entrySet()){
			Pair pair = entry.getKey();
			
			// if the item is frequent in the current projected database
			if(pair.getWeight() >= minSuppWeight){

				int newBuferPosition = lastBufferPosition;
				// we append it to the last itemset of the prefix
				newBuferPosition++;
				patternBuffer[newBuferPosition] = pair.item;
				
				// save the pattern
				savePattern(newBuferPosition, pair.getPseudoSequences());
				
				// make a recursive call
				if( k < maximumPatternLength){
					recursion(patternBuffer, pair.getPseudoSequences(), k+1, newBuferPosition);
				}
			}
		}
		
		// For each pair found representing an item that is not in a postfix
		for(Entry<Pair,Pair> entry : mapsPairs.mapPairs.entrySet()){
			Pair pair = entry.getKey();
			// if the item is frequent in the current projected database
			if(pair.getWeight() >= minSuppWeight){

				int newBuferPosition = lastBufferPosition;
				// we append it to the last itemset of the prefix
				newBuferPosition++;
				patternBuffer[newBuferPosition] = -1;
				newBuferPosition++;
				patternBuffer[newBuferPosition] = pair.item;
				
				// save the pattern
				savePattern(newBuferPosition, pair.getPseudoSequences());
				
				// make a recursive call
				if( k < maximumPatternLength){
					recursion(patternBuffer, pair.getPseudoSequences(), k+1, newBuferPosition);
				}
			}
		}
		
	}
	
	/**
	 * Method to find all frequent items in a projected sequence database
	 * @param sequences  the set of sequences
	 * @param patternBuffer  the current sequential pattern that we want to try to grow
	 * @param lastBufferPosition the last position used in the buffer for storing the current prefix
	 * @return A list of pairs, where a pair is an item with (1) a boolean indicating if it
	 *         is in an itemset that is "cut" and (2) the sequence IDs where it occurs.
	 */
	protected Map<Integer,List<PseudoSequence>>  findAllFrequentPairsSingleItems(List<PseudoSequence> sequences, int lastBufferPosition){
		// We use a Map the store the pairs.
		Map<Integer,List<PseudoSequence>>  mapItemsPseudoSequences = new HashMap<Integer,List<PseudoSequence>>();

		// for each sequence
		for(PseudoSequence pseudoSequence : sequences){
			
			// for each sequence
			int sequenceID = pseudoSequence.getOriginalSequenceID();
			WeightedSequence wSequence = sequenceDatabase.getSequences().get(sequenceID);
			int[] sequence = wSequence.getSequence();
			
			// for each token in this sequence 
			for(int i = pseudoSequence.indexFirstItem;  sequence[i] != -2 ; i++){
				int token = sequence[i];
				
				// if it is an item
				if(token > 0){
					// get the pair object stored in the map if there is one already
					List<PseudoSequence> listSequences = mapItemsPseudoSequences.get(token);
					// if there is no pair object yet
					if(listSequences == null){
						listSequences = new ArrayList<PseudoSequence>();
						// store the pair object that we created
						mapItemsPseudoSequences.put(token, listSequences);
					}
					
					// Check if that sequence as already been added to the projected database of this item
					boolean ok = true;
					if(listSequences.size() > 0){
						ok = listSequences.get(listSequences.size() - 1).sequenceID != sequenceID;
					}
					// if not we add it
					if(ok){
						listSequences.add(new PseudoSequence(sequenceID, wSequence.getWeight(), i+1));
					}
				}
			}
		}
		// return the map of pairs
		return mapItemsPseudoSequences;
	}
	
	
	/**
	 * This class contains two maps, which are used for counting the
	 * frequencies of items, whether in a postfix itemset or a normal itemset.
	 */
	public class MapFrequentPairs{
	    public final Map<Pair, Pair>  mapPairs = new HashMap<Pair, Pair>();
	    public final Map<Pair, Pair>  mapPairsInPostfix = new HashMap<Pair, Pair>();
	};

	/**
	 * Method to find all frequent items in a projected sequence database
	 * @param sequences  the set of sequences
	 * @param lastBufferPosition the last position used in the buffer for storing the current prefix
	 * @return A list of pairs, where a pair is an item with (1) a boolean indicating if it
	 *         is in an itemset that is "cut" and (2) the sequence IDs where it occurs.
	 */
	protected MapFrequentPairs findAllFrequentPairs(List<PseudoSequence> sequences, int lastBufferPosition){
		// We use an object containing two maps the store the pairs.
		MapFrequentPairs mapsPairs = new MapFrequentPairs();
		
		// find the position of the first item of the last itemset of the current sequential pattern that is grown
		int firstPositionOfLastItemsetInBuffer = lastBufferPosition;
		while(lastBufferPosition >0){
			firstPositionOfLastItemsetInBuffer--;
			if(firstPositionOfLastItemsetInBuffer < 0 || patternBuffer[firstPositionOfLastItemsetInBuffer] == -1){
				firstPositionOfLastItemsetInBuffer++;
				break;
			}
		};
		
		// use a variable to try to match the last itemset of the pattern in the buffer
		int positionToBeMatched = firstPositionOfLastItemsetInBuffer;
		
		// for each sequence
		for(PseudoSequence pseudoSequence : sequences){
			
			// for each sequence
			int sequenceID = pseudoSequence.getOriginalSequenceID();
			WeightedSequence weightedSequence = sequenceDatabase.getSequences().get(sequenceID);
			int[] sequence = weightedSequence.getSequence();
			
			// check if the first itemset of that sequence is a postfix
			// It is a postfix (the itemset is cut) if the previous item is a -1 indicating
			// the end of an itemset
			int previousItem = sequence[pseudoSequence.indexFirstItem - 1];
			boolean currentItemsetIsPostfix = (previousItem != - 1);
			boolean isFirstItemset = true;
			
			// for each token in this sequence (item, separator between itemsets (-1) or end of sequence (-2)
			for(int i = pseudoSequence.indexFirstItem;  sequence[i] != -2 ; i++){
				int token = sequence[i];
				
				// if it is an item
				if(token > 0){
					
					// create the pair corresponding to this item
					Pair pair = new Pair(token);   
					// get the pair object store in the map if there is one already
					Pair oldPair;
					if(currentItemsetIsPostfix){
						oldPair = mapsPairs.mapPairsInPostfix.get(pair);
					}else{
						oldPair = mapsPairs.mapPairs.get(pair);
					}
					// if there is no pair object yet
					if(oldPair == null){
						// store the pair object that we created
						if(currentItemsetIsPostfix){
							mapsPairs.mapPairsInPostfix.put(pair, pair);
						}else{
							mapsPairs.mapPairs.put(pair, pair);
						}
					}else{
						// otherwise use the old one
						pair = oldPair;
					}
					
					// Check if that sequence as already been added to the projected database of this item
					boolean ok = true;
					if(pair.getPseudoSequences().size() > 0){
						ok = pair.getPseudoSequences().get(pair.getPseudoSequences().size() - 1).sequenceID != sequenceID;
					}
					// if not we add it
					if(ok){
						pair.getPseudoSequences().add(new PseudoSequence(sequenceID, pseudoSequence.seqWeight, i+1));
					}
					
					///////// ====== IMPORTANT =========
					// if the current itemset is a postfix and it is not the first itemset
					// we must also consider that it may not be a postfix for extending the current prefix
					if(currentItemsetIsPostfix && isFirstItemset == false){
						// create the pair corresponding to this item
						 pair = new Pair(token);     // FALSE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
						// get the pair object store in the map if there is one already
						 oldPair = mapsPairs.mapPairs.get(pair);
						// if there is no pair object yet
						if(oldPair == null){
							// store the pair object that we created
							mapsPairs.mapPairs.put(pair, pair);
						}else{
							// otherwise use the old one
							pair = oldPair;
						}
						
						// Check if that sequence as already been added to the projected database of this item
						ok = true;
						if(pair.getPseudoSequences().size() > 0){
							ok = pair.getPseudoSequences().get(pair.getPseudoSequences().size() - 1).sequenceID != sequenceID;
						}
						// if not we add it
						if(ok){
							pair.getPseudoSequences().add(new PseudoSequence(sequenceID, pseudoSequence.seqWeight, i+1));
						}
						
					}
					//////////////////////////////////////////////////////////

					
					//  try to match this item with the last itemset in the prefix
					if(currentItemsetIsPostfix == false && patternBuffer[positionToBeMatched] == token){
						positionToBeMatched++;
						if(positionToBeMatched > lastBufferPosition){
							currentItemsetIsPostfix = true;
						}
					}

				}else if(token == -1){
					isFirstItemset = false;
					currentItemsetIsPostfix = false;
					positionToBeMatched = firstPositionOfLastItemsetInBuffer;
				}
			}
		}
		// return the map of pairs
		return mapsPairs;
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 * @param size  the size of the database
	 */
	public void printStatistics() {
		StringBuilder r = new StringBuilder(200);
		r.append("=============  PREFIXSPAN 0.99-2016 - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : " + patternCount);
		r.append('\n');
		r.append(" Max memory (mb) : ");
		r.append('\n');
		r.append(" minsupWeight = " + minSuppWeight + " sequences.");
		r.append('\n');
		r.append(" Pattern count : ");
		r.append(patternCount);
		r.append('\n');
		r.append("===================================================\n");
		// if the result was save into memory, print it
		if(patterns !=null){
			patterns.printFrequentPatterns(sequenceCount, showSequenceIdentifiers);
		}
		System.out.println(r.toString());
	}
	
	/**
	 * Get the maximum length of patterns to be found (in terms of item count)
	 * @return the maximumPatternLength
	 */
	public int getMaximumPatternLength() {
		return maximumPatternLength;
	}

	/**
	 * Set the maximum length of patterns to be found (in terms of item count)
	 * @param maximumPatternLength the maximumPatternLength to set
	 */
	public void setMaximumPatternLength(int maximumPatternLength) {
		this.maximumPatternLength = maximumPatternLength;
	}
	
	/**
	 * Set that the sequence identifiers should be shown (true) or not (false) for each
	 * pattern found
	 * @param showSequenceIdentifiers true or false
	 */
	public void setShowSequenceIdentifiers(boolean showSequenceIdentifiers) {
		this.showSequenceIdentifiers = showSequenceIdentifiers;
	}

}
