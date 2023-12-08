package de.rwth.processmining.tb.core.data.variantlog.sliding;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;

public interface RollingVariantLog<T extends CVariant> extends CVariantLog<T> {
  
  public void add(T variant);
  
  /**
   * Remove and return the oldest elements.
   * <p>
   * <b>Don't modify the returned variant. 
   * The count corresponds to the current count in the log when removed.</b>
   * 
   * @return Oldest removed element
   */
  public T getRemoveOldest();
  
}
