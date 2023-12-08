package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.fpgrowth;

import java.util.ArrayList;
import java.util.List;

/**
 * This code is based on the code found in  https://github.com/PySualk/fp-growth-java 
 * @author brockhoff
 * @author PySualk
 *
 */
public class WeightedFPtree<T> {

	private T item;
	private List<WeightedFPtree<T>> children;
	private Double pathWeight = 0.0;
	private WeightedFPtree<T> parent;
	private Boolean root = false;

	// Links to the node with same item name or null
	private WeightedFPtree<T> next = null;

	public WeightedFPtree(T item, WeightedFPtree<T> parent) {
		this.item = item;
		this.parent = parent;
		this.children = new ArrayList<>();
	}

	public void addChild(WeightedFPtree<T> child) {
		this.children.add(child);
	}

	public WeightedFPtree<T> getParent() {
		return parent;
	}

	public void setParent(WeightedFPtree<T> parent) {
		this.parent = parent;
	}

	public List<WeightedFPtree<T>> getChildren() {
		return this.children;
	}

	public T getItem() {
		return this.item;
	}

	public Double getPathWeight() {
		return this.pathWeight;
	}

	public void setPathWeight(Double weight) {
		this.pathWeight = weight;
	}

	public void increasePathWeight(Double w) {
		this.pathWeight += w;
	}

	public WeightedFPtree<T> getNext() {
		return this.next;
	}

	public void setNext(WeightedFPtree<T> next) {
		this.next = next;
	}

	public void setRoot(Boolean root) {
		this.root = root;
	}

	public Boolean isRoot() {
		return this.root;
	}

	public String toString() {
		return "FPtree[" + item + ":" + pathWeight + ", Children: " + children + "]";
	}

}
