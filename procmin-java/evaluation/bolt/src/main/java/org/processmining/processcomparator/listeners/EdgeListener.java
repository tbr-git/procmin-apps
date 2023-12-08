package org.processmining.processcomparator.listeners;

import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;
import org.processmining.processcomparator.view.ComparatorPanel;

import com.kitfox.svg.SVGDiagram;

public abstract class EdgeListener implements DotElementSelectionListener {

	protected Transition transition;
	protected ComparatorPanel parentComponent;

	protected EdgeListener(ComparatorPanel parentComponent, Transition transition) {
		this.parentComponent = parentComponent;
		this.transition = transition;
	}

	public abstract void selected(DotEdge edge, SVGDiagram image);

	public abstract void deselected(DotEdge edge, SVGDiagram image);

	public void selected(DotElement element, SVGDiagram image) {
		if (element instanceof DotEdge)
			selected((DotEdge) element, image);
	}

	public void deselected(DotElement element, SVGDiagram image) {
		if (element instanceof DotEdge)
			deselected((DotEdge) element, image);
	}

}
