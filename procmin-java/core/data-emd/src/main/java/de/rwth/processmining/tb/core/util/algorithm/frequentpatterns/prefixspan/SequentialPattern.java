package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.prefixspan;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a sequential pattern.
 * A sequential pattern is a list of itemsets.
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
 
public class SequentialPattern implements Comparable<SequentialPattern>{
	
	// the list of items
	private final List<Integer> items;
	
	// IDs of sequences containing this pattern
	private List<IndexWeightPair> sequencesIds;
	
	// whether the sequence was found (used in ProSecCo)
	private boolean isFound = false;
	

	// total weight of the sequential pattern
	private double totalWeight = 0;

	
	/**
	 * Set the set of IDs of sequence containing this prefix
	 * @param a set of integer containing sequence IDs
	 */
	public void setSequenceIDs(List<IndexWeightPair> sequencesIds) {
		this.sequencesIds = sequencesIds;
		this.totalWeight = sequencesIds.stream().map(p -> p.weight).reduce(0.0, Double::sum);
	}

	/**
	 * Defaults constructor
	 */
	public SequentialPattern(){
		items = new ArrayList<Integer>();
	}

	
	/**
	 * Get the relative support of this pattern (a percentage)
	 * @param sequencecount the number of sequences in the original database
	 * @return the support as a string
	 */
	public String getRelativeSupportFormated(int sequencecount) {
		double relSupport = ((double)sequencesIds.size()) / ((double) sequencecount);
		// pretty formating :
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5); 
		return format.format(relSupport);
	}
	
	/**
	 * Get the absolute support of this pattern.
	 * @return the support (an integer >= 1)
	 */
	public double getAbsoluteWeight(){
		return totalWeight;
	}

	/**
	 * Add an item to this sequential pattern
	 * @param item the item to be added
	 */
	public void addItem(Integer item) {
//		itemCount += item.size();
		items.add(item);
	}


	/**
	 * Print this sequential pattern to System.out
	 */
	public void print() {
		System.out.print(toString());
	}
	
	/**
	 * Get a string representation of this sequential pattern, 
	 * containing the sequence IDs of sequence containing this pattern.
	 */
	public String toString() {
		StringBuilder r = new StringBuilder("");
		// For each item in this sequential pattern
		for(Integer item : items){
			r.append(item.toString());
			r.append(" ");
		}
//
//		//  add the list of sequence IDs that contains this pattern.
//		if(getSequencesID() != null){
//			r.append("  Sequence ID: ");
//			for(Integer id : getSequencesID()){
//				r.append(id);
//				r.append(' ');
//			}
//		}
		return r.append("    ").toString();
	}
	

	/**
	 * Get the items in this sequential pattern
	 * @return a list of items.
	 */
	public List<Integer> getItems() {
		return items;
	}
	
	/**
	 * Get an item at a given position.
	 * @param index the position
	 * @return the item
	 */
	public int get(int index) {
		return items.get(index);
	}

	
	public List<IndexWeightPair> getSequenceIDs() {
		return sequencesIds;
	}

	@Override
	public int compareTo(SequentialPattern o) {
		if(o == this){
			return 0;
		}
		int compRes = Double.compare(this.getAbsoluteWeight(), o.getAbsoluteWeight());
		if(compRes != 0){
			return compRes;
		}

		return this.hashCode() - o.hashCode();
	}

	public boolean setIsFound(boolean b) {
		return isFound;
		
	}
	
	public boolean isFound() {
		return isFound;
	}
	
}