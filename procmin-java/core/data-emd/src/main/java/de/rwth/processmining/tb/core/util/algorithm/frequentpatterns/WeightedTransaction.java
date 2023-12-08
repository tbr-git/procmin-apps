package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns;

import java.util.ArrayList;
import java.util.List;
/**
 * This code is based on the code found in  https://github.com/PySualk/fp-growth-java 
 * @author brockhoff
 * @author PySualk
 *
 */
public class WeightedTransaction<T> {
	
	private Double weight;

	private List<T> items = new ArrayList<>();

	/**
	 * Creates a copy of the items and keeps distinct ones.
	 * @param items
	 * @param weight
	 */
	public WeightedTransaction(List<T> items, Double weight) {
		super();
		this.weight = weight;
		this.items = items.stream().distinct().toList();
	}

	/**
	 * Creates a copy of the items and keeps distinct ones.
	 * @param items
	 * @param weight
	 */
	public WeightedTransaction(List<T> items, int support) {
		super();
		this.weight = Double.valueOf(support);
		this.items = items.stream().distinct().toList();
	}

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}
	
	@Override
	public String toString() {
		return "WeightedTransaction [transactionWeight=" + weight + ", items=" + items.toString() + "]";
	}	

}
