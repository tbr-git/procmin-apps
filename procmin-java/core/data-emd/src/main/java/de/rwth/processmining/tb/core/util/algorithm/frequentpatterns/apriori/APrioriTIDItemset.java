package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.apriori;

import java.util.BitSet;

public class APrioriTIDItemset {
  
  private final int[] items;
  
  private final double support;
  
  private final BitSet tidSet;

  public APrioriTIDItemset(int item,
      double support, BitSet tidSet) {
    super();
    this.items = new int[] {item};
    this.support = support;
    this.tidSet = tidSet;
  }

  public APrioriTIDItemset(int[] items,
      double support, BitSet tidSet) {
    super();
    this.items = items;
    this.support = support;
    this.tidSet = tidSet;
  }

  public int[] getItems() {
    return items;
  }

  public double getSupport() {
    return support;
  }

  public BitSet getTidSet() {
    return tidSet;
  }
  
  public int getItem(int pos) {
    return this.items[pos];
  }

  public BitSet getTransactionsIds() {
    return tidSet;
  }

}
