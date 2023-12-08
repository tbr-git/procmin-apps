package org.processmining.processcomparator.listeners.edge;

import java.awt.MouseInfo;

import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.tsanalyzer2.TSAnalyzer;
import org.processmining.plugins.tsanalyzer2.annotation.Annotation;
import org.processmining.processcomparator.algorithms.Utils;
import org.processmining.processcomparator.listeners.EdgeListener;
import org.processmining.processcomparator.view.ComparatorPanel;
import org.processmining.processcomparator.view.dialog.processmetric.EdgeDetailDialog;

import com.kitfox.svg.SVGDiagram;

public class ProcessMetricEdgeListener extends EdgeListener {

	private Annotation ref, a, b;

	public ProcessMetricEdgeListener(ComparatorPanel parentComponent, Transition transition, Annotation ref,
			Annotation a, Annotation b) {
		super(parentComponent, transition);
		this.ref = ref;
		this.a = a;
		this.b = b;
	}

	public void selected(DotEdge element, SVGDiagram diagram) {
		DotPanel.setCSSAttributeOf(DotPanel.getSVGElementOf(diagram, element).getChild(1), "stroke-dasharray", "10,5");

		EdgeDetailDialog dialog = (EdgeDetailDialog) Utils.edgePopups.get(element);
		if (dialog != null) {
			dialog.setLocation(MouseInfo.getPointerInfo().getLocation());
			dialog.setVisible(true);
		} else { //transforms variances into standard deviations with Math.sqrt
			dialog = new EdgeDetailDialog(transition.getLabel(), transition.getSource().getLabel(),
					transition.getTarget().getLabel(),
					ref.getElement(TSAnalyzer.trace_frequency) != null
							? ref.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
					ref.getElement(TSAnalyzer.trace_frequency) != null
							? ref.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
					ref.getElement(TSAnalyzer.duration) != null ? ref.getElement(TSAnalyzer.duration).getMean() : 0,
					ref.getElement(TSAnalyzer.duration) != null
							? ref.getElement(TSAnalyzer.duration).getStandardDeviation() : 0,
					ref.getElement(TSAnalyzer.trace_frequency) != null
							? ref.getElement(TSAnalyzer.trace_frequency).getN() : 0,
					a.getElement(TSAnalyzer.trace_frequency) != null
							? a.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
					a.getElement(TSAnalyzer.trace_frequency) != null
							? a.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
					a.getElement(TSAnalyzer.duration) != null ? a.getElement(TSAnalyzer.duration).getMean() : 0,
					a.getElement(TSAnalyzer.duration) != null ? a.getElement(TSAnalyzer.duration).getStandardDeviation()
							: 0,
					a.getElement(TSAnalyzer.trace_frequency) != null ? a.getElement(TSAnalyzer.trace_frequency).getN()
							: 0,
					b.getElement(TSAnalyzer.trace_frequency) != null
							? b.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
					b.getElement(TSAnalyzer.trace_frequency) != null
							? b.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
					b.getElement(TSAnalyzer.duration) != null ? b.getElement(TSAnalyzer.duration).getMean() : 0,
					b.getElement(TSAnalyzer.duration) != null ? b.getElement(TSAnalyzer.duration).getStandardDeviation()
							: 0,
					b.getElement(TSAnalyzer.trace_frequency) != null ? b.getElement(TSAnalyzer.trace_frequency).getN()
							: 0);

			dialog.setUndecorated(true);
			dialog.setAlwaysOnTop(true);
			dialog.setLocation(MouseInfo.getPointerInfo().getLocation());

			dialog.setVisible(true);

			Utils.edgePopups.put(element, dialog);
		}
		refreshPanel();
	}

	public void deselected(DotEdge element, SVGDiagram diagram) {
		DotPanel.setCSSAttributeOf(DotPanel.getSVGElementOf(diagram, element).getChild(1), "stroke-dasharray", "");
		EdgeDetailDialog dialog = (EdgeDetailDialog) Utils.edgePopups.get(element);
		if (dialog != null)
			dialog.setVisible(false);
		refreshPanel();
	}

	private void refreshPanel() {
		parentComponent.doLayout();
		parentComponent.repaint();
	}

}
