package de.rwth.processmining.tb.core.data.variantlog.sliding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import org.deckfour.xes.classification.XEventClassifier;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogImpl;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import gnu.trove.map.hash.TObjectIntHashMap;

public class RollingVariantLogImpl<T extends CVariant> 
    extends CVariantLogImpl<T> implements RollingVariantLog<T> {
  // For simplicity, keep the filtering methods from the super type
  // Filtering results in a normal variant log because update 
  // of case arrival to variant index is non-trivial and quite expensive.
  // Currently, I don't need this functionality anyway.
  
  /**
   * Indices in variant log of arriving variants 
   */
  private LinkedList<Integer> arrivalVIndices;
  
  /**
   * Mapping variants to indices in variant list.
   */
  private TObjectIntHashMap<T> variant2Index;
  
  public RollingVariantLogImpl(XEventClassifier classifier, CategoryMapper categoryMapper) {
		super(classifier, categoryMapper, new ArrayList<T>(), 0);
    this.variant2Index = new TObjectIntHashMap<>();
    this.arrivalVIndices = new LinkedList<Integer>();
  }
  
	public RollingVariantLogImpl(XEventClassifier classifier, CategoryMapper categoryMapper, 
	    ArrayList<T> variants, LinkedList<Integer> arrivalVIndices) {
		super(classifier, categoryMapper, variants);
		this.arrivalVIndices = arrivalVIndices;
		this.variant2Index = initVariantIndexMap(variants);
	}

	public RollingVariantLogImpl(XEventClassifier classifier, CategoryMapper categoryMapper, 
	    ArrayList<T> variants, LinkedList<Integer> arrivalVIndices, int size) {
		super(classifier, categoryMapper, variants, size);
		this.arrivalVIndices = arrivalVIndices;
		this.variant2Index = initVariantIndexMap(variants);
	}
	
	private TObjectIntHashMap<T> initVariantIndexMap(ArrayList<T> variants) {
	  TObjectIntHashMap<T> variant2Index = new TObjectIntHashMap<>(2 * variants.size());
	  int i = 0;
	  for (T v : variants) {
	    variant2Index.put(v, i);
	  }
	  return variant2Index;
	}

  @Override
  public void add(T variant) {
    int index = -1;
    if (variant2Index.containsKey(variant)) {
      // Update support
      index = variant2Index.get(variant);
      CVariant v = this.variants.get(index);
      v.setSupport(v.getSupport() + variant.getSupport());
    }
    else {
      // Add new variant and save index
      this.variants.addLast(variant);
      index = this.variants.size() - 1;
      this.variant2Index.put(variant, index);
    }
    // Save index
    this.arrivalVIndices.add(index);
    this.size++;
  }

  @Override
  public T getRemoveOldest() {
    // Can't dequeue from an empty list
    if (this.arrivalVIndices.size() == 0) {
      return null;
    }
    int indexOldest = this.arrivalVIndices.pollFirst();
    T vOldest = this.variants.get(indexOldest);
    this.size--;
    // Last "variant"
    if (vOldest.getSupport() == 1) {
      // Remove from data structures
      this.variants.remove(indexOldest);
      this.variant2Index.remove(vOldest);
      // Need to update all larger indices in map due to shift
      variant2Index.transformValues(i -> {
        if (i > indexOldest) {
          return i - 1;
        }
        else {
          return i;
        }
      });
      // Likewise for the arrival position -> variant index mapping
      ListIterator<Integer> itArrival = arrivalVIndices.listIterator();
      while (itArrival.hasNext()) {
        int oldVariantIndex = itArrival.next();
        if (oldVariantIndex > indexOldest) {
          itArrival.set(oldVariantIndex - 1);
        }
      }
      return vOldest;
    }
    else {
      // Just decrement
      vOldest.setSupport(vOldest.getSupport() - 1);
      return vOldest;
    }
  }
}
