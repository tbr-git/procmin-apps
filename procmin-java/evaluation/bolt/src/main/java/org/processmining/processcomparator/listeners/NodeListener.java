package org.processmining.processcomparator.listeners;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;
import org.processmining.processcomparator.view.ComparatorPanel;

import com.kitfox.svg.SVGDiagram;

public abstract class NodeListener implements DotElementSelectionListener {

	protected State state;
	protected ComparatorPanel parentComponent;
	
	protected NodeListener(ComparatorPanel parentComponent, State state){
		this.parentComponent = parentComponent;
		this.state = state;
	}
	
	public abstract void selected(DotNode node, SVGDiagram image);
	
	public abstract void deselected(DotNode node, SVGDiagram image);
	
	public void selected(DotElement element, SVGDiagram image) {
		if(element instanceof DotNode)
			selected((DotNode)element, image);
	}

	public void deselected(DotElement element, SVGDiagram image) {
		if(element instanceof DotNode)
			deselected((DotNode)element, image);
	}
}
