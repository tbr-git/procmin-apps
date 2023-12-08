package de.rwth.processmining.tb.core.emd.dataview.xlogbased;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;

import com.google.common.collect.Iterators;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDistEditDiagnose;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public class MultiViewConfigXLog<F extends TraceDescriptor, D extends TraceDistEditDiagnose<F>> {
	private final static Logger logger = LogManager.getLogger( MultiViewConfigXLog.class );
	
	private ViewConfigXlog<F, D> topLevelView;

	private List<ViewConfigXlog<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>>> subViews;


	public MultiViewConfigXLog() {
		topLevelView = null;
		subViews = new LinkedList<>();
	}

	public MultiViewConfigXLog(ViewConfigXlog<F, D> topLevelView, 
	    List<ViewConfigXlog<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>>> subViews) {
		super();
		this.topLevelView = topLevelView;
		this.subViews = subViews;
	}

	public MultiViewConfigXLog(MultiViewConfigXLog<F, D> multiViewConfig) {
		this.topLevelView = new ViewConfigXlog<>(multiViewConfig.getTopLevelView());
		this.subViews = new LinkedList<ViewConfigXlog<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>>>();
		multiViewConfig.getSubViews().stream().forEach(v -> this.subViews.add(
		    new ViewConfigXlog<>(v)));
	}
	
	public MultiViewConfigXLog<F, D> setTopLevelView(
	    ViewConfigXlog<F, D> topLevelView) {
		this.topLevelView = topLevelView;
		return this;
	}

	public MultiViewConfigXLog<F, D> addSubView(
	    ViewConfigXlog<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>> subView) {
		this.subViews.add(subView);
		return this;
	}

	public MultiViewConfigXLog<F, D> addSubViews(
	    Collection<ViewConfigXlog<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>>> subViews) {
		this.subViews.addAll(subViews);
		return this;
	}
	
	public Iterator<ViewConfigXlog<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>>> getViewIterator() {
		return Iterators.concat(Collections.singleton(topLevelView).iterator(), subViews.iterator());
	}
	
	public ViewConfigXlog<F, D> getTopLevelView() {
		return topLevelView;
	}

	public List<ViewConfigXlog<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>>> getSubViews() {
		return subViews;
	}
	
	public boolean isConsistent4LogProjection(XLog xlog) {
		Iterator<ViewConfigXlog<? extends TraceDescriptor, ? extends TraceDescDistCalculator<?>>> itView = 
		    getViewIterator();
		boolean isConsistent = true;
		while(isConsistent && itView.hasNext()) {
			if(!itView.next().isConsistent4LogProjection(xlog)) {
				isConsistent = false;
			}
		}
		return isConsistent;
	}
	
}
