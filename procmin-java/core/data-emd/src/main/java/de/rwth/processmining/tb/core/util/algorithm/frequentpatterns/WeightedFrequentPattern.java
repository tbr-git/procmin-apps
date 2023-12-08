package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
/**
 * This code is based on the code found in  https://github.com/PySualk/fp-growth-java 
 * @author brockhoff
 * @author PySualk
 *
 */
public class WeightedFrequentPattern<T> implements Collection<T> {

	private List<T> items = new ArrayList<>();

	private Double totalSupportWeight;

	private Double support;

	private static final String DELIMITER = ",";

	public WeightedFrequentPattern(List<T> items, Double totalSupportWeight, Double support) {
		this.items = Collections.unmodifiableList(items);
		this.totalSupportWeight = totalSupportWeight;
		this.support = support;
	}

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public Double getTotalSupportWeight() {
		return totalSupportWeight;
	}

	public void setTotalSupportWeight(Double totalSupportWeight) {
		this.totalSupportWeight = totalSupportWeight;
	}

	public Double getSupport() {
		return support;
	}

	public void setSupport(Double support) {
		this.support = support;
	}

	public String toString() {
		return "FrequentPattern[" + items + ":" + totalSupportWeight + "]";
	}
	
	@Override
	public int size() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		return this.items.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return this.items.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return this.items.iterator();
	}

	@Override
	public Object[] toArray() {
		return this.items.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.items.toArray(a);
	}

	@Override
	public boolean add(T e) {
		return false;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.items.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {
	}

}
