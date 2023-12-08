package de.rwth.processmining.tb.core.data.variantlog.xfacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.XVisitor;
import org.deckfour.xes.util.XAttributeUtils;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;

/**
 * Wrapper/Facade class that provides XTrace functionality 
 * to Categorical variants.
 */
public class XTraceFacadeCVariant implements XTrace {
  
  /**
   * Handle to event (XEvent) prototypes for the categories in the
   * wrapped categorical trace
   */
  final XEventCPrototype[] eventPrototypes;
  
  /**
   * Wrapped categorical variant
   */
  final CVariant v;

	/**
	 * Map of attributes for this trace.
	 */
	private XAttributeMap attributes;
	
	/**
	 * Direct convenience identifier.
	 * No need to query in a map or use the String name to
	 * uniquely identify this trace.
	 */
	private final int id;
	
	
	/**
	 * Creates a new trace.
	 * 
	 * @param attributeMap Attribute map used to 
	 * 	store this trace's attributes.
	 */
	public XTraceFacadeCVariant(XAttributeMap attributeMap, CVariant v, int id, XEventCPrototype[] eventPrototypes) {
		this.attributes = attributeMap;
		this.v = v;
		this.eventPrototypes = eventPrototypes;
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.deckfour.xes.model.XAttributable#getAttributes()
	 */
	public XAttributeMap getAttributes() {
		return attributes;
	}

	/* (non-Javadoc)
	 * @see org.deckfour.xes.model.XAttributable#getExtensions()
	 */
	public Set<XExtension> getExtensions() {
		return XAttributeUtils.extractExtensions(attributes);
	}

	/* (non-Javadoc)
	 * @see org.deckfour.xes.model.XAttributable#setAttributes(java.util.Map)
	 */
	public void setAttributes(XAttributeMap attributes) {
		this.attributes = attributes;
	}

  @Override
  public int size() {
    return v.getVariantLength();
  }

  @Override
  public boolean isEmpty() {
    return v.getVariantLength() == 0;
  }

  @Override
  public boolean contains(Object o) {
    if (o instanceof XEventCPrototype event) {
      return v.containsCategory(event.getCategoryCode());
    }
    return false;
  }

  @Override
  public Iterator<XEvent> iterator() {
    return new Iterator<XEvent> () {
      
      int cur = 0;

      @Override
      public boolean hasNext() {
        return cur < v.getVariantLength();
      }

      @Override
      public XEvent next() {
        int catNext = v.getTraceCategories()[cur];
        XEvent e = eventPrototypes[catNext];
        cur++;
        return e;
      }
      
    };
  }

  @Override
  public Object[] toArray() {
    XEventCPrototype[] arr = new XEventCPrototype[v.getVariantLength()];
    
    int i = 0;
    for (int c : v.getTraceCategories()) {
      arr[i] = eventPrototypes[c]; 
      i++;
    }
    return arr;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    // TODO Auto-generated method stub
    if (a.length < v.getVariantLength()) {
      return (T[]) this.toArray();
    }
    else {
      int i = 0;
      for (int c : v.getTraceCategories()) {
        a[i] = (T) eventPrototypes[c]; 
        i++;
      }
      while (i < a.length) {
        a[i] = null;
      }
    }
    return a;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object o : c) {
      if (! this.contains(o)) {
        return false;
      }
    }
    return false;
  }
  
  
  @Override
  public XEvent get(int index) {
    return eventPrototypes[v.getTraceCategories()[index]];
  }
  
  @Override
  public int indexOf(Object o) {
    // Can only contain prototype events
    if (o.getClass() != XEventCPrototype.class) {
      return -1;
    }
    XEventCPrototype other = (XEventCPrototype) o;

    // Search
    int i = 0;
    for (int c : v.getTraceCategories()) {
      if (other.getCategoryCode() == c) {
        return i;
      }
      i++;
    }
    return -1;
  }

  @Override
  public int lastIndexOf(Object o) {
    // Can only contain prototype events
    if (o.getClass() != XEventCPrototype.class) {
      return -1;
    }
    XEventCPrototype other = (XEventCPrototype) o;

    // Search
    int i = v.getVariantLength() - 1;
    while (i >= 0) {
      if (other.getCategoryCode() == v.getTraceCategories()[i]) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public ListIterator<XEvent> listIterator() {
    return this.listIterator(0);
  }

  @Override
  public ListIterator<XEvent> listIterator(int index) {

    return new ListIterator<XEvent> () {
      
      int cur = index;

      @Override
      public boolean hasNext() {
        return cur >= 0 && cur < v.getVariantLength();
      }

      @Override
      public XEvent next() {
        int catNext = v.getTraceCategories()[cur];
        XEvent e = eventPrototypes[catNext];
        cur++;
        return e;
      }

      @Override
      public boolean hasPrevious() {
        return cur >= 0 && cur < v.getVariantLength();
      }

      @Override
      public XEvent previous() {
        int cat = v.getTraceCategories()[cur];
        XEvent e = eventPrototypes[cat];
        cur--;
        return e;
      }

      @Override
      public int nextIndex() {
        return cur + 1;
      }

      @Override
      public int previousIndex() {
        return cur - 1;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Remove not supported on XTraceFacade");
      }

      @Override
      public void set(XEvent e) {
        throw new UnsupportedOperationException("Set not supported on XTraceFacade");
      }

      @Override
      public void add(XEvent e) {
        throw new UnsupportedOperationException("Add not supported on XTraceFacade");
      }
        
    };
  }

  @Override
  public List<XEvent> subList(int fromIndex, int toIndex) {
    if (fromIndex >= toIndex) {
      return List.of();
    }
    else {
      List<XEvent> res = new ArrayList<>(toIndex - fromIndex);
      
      for (int i = fromIndex; i < toIndex; i++) {
        res.add(this.get(i));
      }
      return res;
    }
  }

  @Override
  public Object clone() {
    XAttributeMap attributes = (XAttributeMap)getAttributes().clone();
    return new XTraceFacadeCVariant(attributes, v, this.id, this.eventPrototypes);
  }

  ////////////////////////////////////////////////////////////
  // Edit Not Supported (Ignored)
  ////////////////////////////////////////////////////////////

  @Override
  public boolean add(XEvent e) {
    // Not supported
    return false;
  }

  @Override
  public boolean remove(Object o) {
    // Not supported
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends XEvent> c) {
    // Not supported
    return false;
  }

  @Override
  public boolean addAll(int index, Collection<? extends XEvent> c) {
    // Not supported
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    // Not supported
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    // Not supported
    return false;
  }

  @Override
  public void clear() {
    // Not supported
  }


  @Override
  public XEvent set(int index, XEvent element) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void add(int index, XEvent element) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public XEvent remove(int index) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public int insertOrdered(XEvent arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean hasAttributes() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void accept(XVisitor arg0, XLog arg1) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

}
