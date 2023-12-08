package de.rwth.processmining.tb.core.data.variantlog.xfacade;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;

/**
 * Class that establishes a connection between the "activities" in a categorical 
 * abstraction and the standard {@link XEvent} that is used all over ProM.
 * <p>
 * Idea: 
 * Mock event that holds:
 * <p>
 * <ul>
 * <li>The <b>activity</b></li>
 * <li>Category code</li>
 * </ul>
 * <p>
 * <b>Note:</b> It does not hold any time information
 */
public class XEventCPrototype extends XEventImpl {
  
  /**
   * Category code for which this event event is
   * the prototype
   */
  private int categoryCode;
  

  public static XEventCPrototype createXEventCPrototype(int category, String activity) {
    XEventCPrototype event = new XEventCPrototype(category);

    // Inspired by the XLogBuilder class
    XConceptExtension conceptInstance = XConceptExtension.instance();   
    // Assign it a name (and the corresponding concept extension)
    conceptInstance.assignName(event, activity);
    
    return event;
  }
  
  public XEventCPrototype(int categoryCode) {
    this(new XAttributeMapImpl(), categoryCode);
  }

  public XEventCPrototype(XAttributeMap attributes, int categoryCode) {
    super(attributes);
    this.categoryCode = categoryCode;
  }

  @Override
  public Object clone() {
    XAttributeMap attributes = (XAttributeMap)getAttributes().clone();
		XEventCPrototype clone = new XEventCPrototype(attributes, categoryCode);
		return clone;
  }

  public int getCategoryCode() {
    return categoryCode;
  }

  public void setCategoryCode(int categoryCode) {
    this.categoryCode = categoryCode;
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof XEventCPrototype other) && this.categoryCode == other.categoryCode;

    /*
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) 
      return false;
    XEventCPrototype other = (XEventCPrototype) o;
    return this.categoryCode == other.categoryCode;
    */
  }


}
