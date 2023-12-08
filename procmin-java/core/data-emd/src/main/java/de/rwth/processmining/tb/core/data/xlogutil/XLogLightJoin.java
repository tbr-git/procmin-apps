package de.rwth.processmining.tb.core.data.xlogutil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.XVisitor;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.processmining.log.utils.XLogBuilder;

public class XLogLightJoin implements XLog {

	private LinkedList<XLog> logs;
	
	private int totalRefLogSize;

	/**
	 * Map of attributes for this log.
	 */
	private XAttributeMap attributes;
	/**
	 * Extensions.
	 */
	private Set<XExtension> extensions;
	/**
	 * Classifiers.
	 */
	private List<XEventClassifier> classifiers;
	/**
	 * Global trace attributes.
	 */
	private List<XAttribute> globalTraceAttributes;
	/**
	 * Global event attributes.
	 */
	private List<XAttribute> globalEventAttributes;

	/**
	 * Single-item cache. Only the last info is cached. 
	 * Typically, only one classifier will be used for a log.
	 */
	private XEventClassifier cachedClassifier;
	private XLogInfo cachedInfo;
	
	public XLogLightJoin(Collection<XLog> logs) {
		this.logs = new LinkedList<>(logs);
		totalRefLogSize = logs.stream().map(l -> l.size()).reduce(0, Integer::sum);
		XLog logAdd = XLogBuilder.newInstance().startLog("Light join: Additional trace log").build();
		logs.add(logAdd);

		this.attributes = new XAttributeMapImpl();
		this.extensions = logs.stream().map(l -> l.getExtensions()).flatMap(s -> s.stream()).collect(Collectors.toSet());
		this.globalTraceAttributes = new ArrayList<XAttribute>();
		this.globalEventAttributes = new ArrayList<XAttribute>();
	}
	
	
	/* (non-Javadoc)
	 * @see org.deckfour.xes.model.XAttributable#getAttributes()
	 */
	@Override
	public XAttributeMap getAttributes() {
		return attributes;
	}

	/* (non-Javadoc)
	 * @see org.deckfour.xes.model.XAttributable#setAttributes(java.util.Map)
	 */
	@Override
	public void setAttributes(XAttributeMap attributes) {
		this.attributes = attributes;
	}

	/* (non-Javadoc)
	 * @see org.deckfour.xes.model.XAttributable#getExtensions()
	 */
	@Override
	public Set<XExtension> getExtensions() {
		return extensions;
	}

	/* (non-Javadoc)
	 * @see java.util.ArrayList#clone()
	 */
	public Object clone() {
		XLogLightJoin clone = new XLogLightJoin(logs);
		clone.attributes = (XAttributeMap) this.attributes.clone();
		clone.classifiers = new LinkedList<>(this.classifiers);
		clone.extensions = new HashSet<>(this.extensions);
		clone.globalTraceAttributes = new LinkedList<>(this.globalTraceAttributes);
		clone.globalEventAttributes = new LinkedList<>(this.globalEventAttributes);

		return clone;
	}

	/* (non-Javadoc)
	 * @see org.deckfour.xes.model.XLog#getClassifiers()
	 */
	@Override
	public List<XEventClassifier> getClassifiers() {
		return classifiers;
	}

	/* (non-Javadoc)
	 * @see org.deckfour.xes.model.XLog#getGlobalEventAttributes()
	 */
	@Override
	public List<XAttribute> getGlobalEventAttributes() {
		return globalEventAttributes;
	}

	/* (non-Javadoc)
	 * @see org.deckfour.xes.model.XLog#getGlobalTraceAttributes()
	 */
	@Override
	public List<XAttribute> getGlobalTraceAttributes() {
		return globalTraceAttributes;
	}

	@Override
	public int size() {
		return logs.stream().map(l -> l.size()).reduce(0, Integer::sum);
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		return logs.stream().anyMatch(l -> l.contains(o));
	}

	@Override
	public Iterator<XTrace> iterator() {
		return new Iterator<XTrace>() {
			
			Iterator<XLog> itLog = logs.iterator();
			
			Iterator<XTrace> itTrace = itLog.next().iterator();
			
			int size = size();
			
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < size;
			}

			@Override
			public XTrace next() {
				if(!itTrace.hasNext()) {
					itTrace= itLog.next().iterator();
				}
				i++;
				return itTrace.next();
			}
		};
//		return IteratorUtils.chainedIterator(logs.stream().map(l -> l.iterator()).collect(Collectors.toList()));
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(XTrace e) {
		logs.getLast().add(e);
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Object o) {
		return logs.getLast().remove(o);
//		.stream().anyMatch(l -> l.remove(o));
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		Set<?> tofind = new HashSet<>(c);
		Iterator<XTrace> itT = this.iterator();
		XTrace t;
		while(itT.hasNext() && tofind.size() != 0) {
			t = itT.next();
			if(tofind.contains(t)) {
				tofind.remove(t);
			}
		}
		// TODO Auto-generated method stub
		return tofind.size() == 0;
	}

	@Override
	public boolean addAll(Collection<? extends XTrace> c) {
		return logs.getLast().addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends XTrace> c) {
		index = index - totalRefLogSize;
		if(index < 0) {
			return false;
		}
		else {
			return logs.getLast().addAll(index, c);
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return logs.getLast().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return logs.getLast().retainAll(c);
	}

	@Override
	public void clear() {
		logs = new LinkedList<>();
		XLog logAdd = XLogBuilder.newInstance().startLog("Light join: Additional trace log").build();
		logs.add(logAdd);
	}

	@Override
	public XTrace get(int index) {
		int sizeSum = 0;
		for(XLog xlog : logs) {
			if(sizeSum + xlog.size() > index) {
				return xlog.get(index - sizeSum);
			}
			sizeSum += xlog.size();
		}
		return null;
	}

	@Override
	public XTrace set(int index, XTrace element) {
		index = index - totalRefLogSize;
		if(index < 0) {
			return null;
		}
		else {
			return logs.getLast().set(index, element);
		}
	}

	@Override
	public void add(int index, XTrace element) {
		index = index - totalRefLogSize;
		if(index >= 0) {
			logs.getLast().set(index, element);
		}
	}

	@Override
	public XTrace remove(int index) {
		index = index - totalRefLogSize;
		if(index < 0) {
			return null;
		}
		else {
			return logs.getLast().remove(index);
		}
	}

	@Override
	public int indexOf(Object o) {
		int sizeSum = 0;
		for(XLog xlog : logs) {
			int i = xlog.indexOf(o);
			if(i != -1) {
				return sizeSum += i;
			}
			sizeSum += xlog.size();
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		int sizeSum = size();
		Iterator<XLog> itLreverse = logs.descendingIterator();
		XLog l; 
		int i;
		while(itLreverse.hasNext()) {
			l = itLreverse.next();
			sizeSum -= l.size();
			i = l.lastIndexOf(o);
			if(i != -1) {
				return sizeSum + i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<XTrace> listIterator() {
		throw new RuntimeException("List Iterator not supported for " + this.getClass() + "! Can not be modified");
	}

	@Override
	public ListIterator<XTrace> listIterator(int index) {
		throw new RuntimeException("List Iterator not supported for " + this.getClass() + "! Can not be modified");
	}

	@Override
	public List<XTrace> subList(int fromIndex, int toIndex) {
		List<XTrace> l = new LinkedList<>();
		for(int i = fromIndex; i < toIndex; i++) {
			l.add(get(i));
		}
		return l;
	}

	@Override
	public boolean accept(XVisitor arg0) {
		return logs.stream().allMatch(l -> l.accept(arg0));
	}
	
	/**
	 * Returns the cached info if the given classifier is the cached classifier.
	 * Returns null otherwise.
	 */
	public XLogInfo getInfo(XEventClassifier classifier) {
		return classifier.equals(cachedClassifier) ? cachedInfo : null;
	}
	
	/**
	 * Sets the cached classifier and info to the given objects.
	 */
	public void setInfo(XEventClassifier classifier, XLogInfo info) {
		cachedClassifier = classifier;
		cachedInfo = info;
	}

//	@Override
//	public XLogInfo getInfo(classifier) {
//		return classifier.equals(cachedClassifier) ? cachedInfo : null;
//		return XLogInfoImpl.create(this);
//	}

	@Override
	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}

}
