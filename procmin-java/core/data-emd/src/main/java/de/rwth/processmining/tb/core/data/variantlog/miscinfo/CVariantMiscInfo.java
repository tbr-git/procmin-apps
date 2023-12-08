package de.rwth.processmining.tb.core.data.variantlog.miscinfo;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

/**
 * Interface that flags variant implementations that comprise additional miscellaneous information.
 * In contrast to the the normal {@link CVariant}s, the miscellaneous information is not considered 
 * part of the variant - that is, a single variant can be associated with multiple traces that differ in 
 * their associated miscellaneous information.
 */
public interface CVariantMiscInfo extends CVariant {
  
  public String getMiscInfoDescription();

}
