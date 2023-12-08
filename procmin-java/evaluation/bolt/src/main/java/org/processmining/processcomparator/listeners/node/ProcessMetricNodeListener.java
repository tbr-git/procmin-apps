package org.processmining.processcomparator.listeners.node;

import java.awt.MouseInfo;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.tsanalyzer2.TSAnalyzer;
import org.processmining.plugins.tsanalyzer2.annotation.Annotation;
import org.processmining.processcomparator.algorithms.Utils;
import org.processmining.processcomparator.listeners.NodeListener;
import org.processmining.processcomparator.view.ComparatorPanel;
import org.processmining.processcomparator.view.dialog.processmetric.NodeDetailDialog;

import com.kitfox.svg.SVGDiagram;

public class ProcessMetricNodeListener extends NodeListener {

	private Annotation ref, a, b;

	public ProcessMetricNodeListener(ComparatorPanel parentComponent, State state, Annotation ref, Annotation a,
			Annotation b) {
		super(parentComponent, state);
		this.ref = ref;
		this.a = a;
		this.b = b;

	}

	public void selected(DotNode element, SVGDiagram diagram) {

		DotPanel.setCSSAttributeOf(DotPanel.getSVGElementOf(diagram, element).getChild(1), "stroke-dasharray", "5,2"); // 1 else

		NodeDetailDialog dialog = (NodeDetailDialog) Utils.nodePopups.get(element);
		if (dialog != null) {
			dialog.setLocation(MouseInfo.getPointerInfo().getLocation());
			dialog.setVisible(true);
		} else { //transforms variances into standard deviations with Math.sqrt
			dialog = new NodeDetailDialog(state.getLabel(),
					ref.getElement(TSAnalyzer.trace_frequency) != null
							? ref.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
					ref.getElement(TSAnalyzer.trace_frequency) != null
							? ref.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
					ref.getElement(TSAnalyzer.elapsed_time) != null ? ref.getElement(TSAnalyzer.elapsed_time).getMean()
							: 0,
					ref.getElement(TSAnalyzer.elapsed_time) != null
							? ref.getElement(TSAnalyzer.elapsed_time).getStandardDeviation() : 0,
					ref.getElement(TSAnalyzer.remaining_time) != null
							? ref.getElement(TSAnalyzer.remaining_time).getMean() : 0,
					ref.getElement(TSAnalyzer.remaining_time) != null
							? ref.getElement(TSAnalyzer.remaining_time).getStandardDeviation() : 0,
					ref.getElement(TSAnalyzer.sojourn_time) != null ? ref.getElement(TSAnalyzer.sojourn_time).getMean()
							: 0,
					ref.getElement(TSAnalyzer.sojourn_time) != null
							? ref.getElement(TSAnalyzer.sojourn_time).getStandardDeviation() : 0,
					ref.getElement(TSAnalyzer.trace_frequency) != null
							? ref.getElement(TSAnalyzer.trace_frequency).getN() : 0,
					a.getElement(TSAnalyzer.trace_frequency) != null
							? a.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
					a.getElement(TSAnalyzer.trace_frequency) != null
							? a.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
					a.getElement(TSAnalyzer.elapsed_time) != null ? a.getElement(TSAnalyzer.elapsed_time).getMean() : 0,
					a.getElement(TSAnalyzer.elapsed_time) != null
							? a.getElement(TSAnalyzer.elapsed_time).getStandardDeviation() : 0,
					a.getElement(TSAnalyzer.remaining_time) != null ? a.getElement(TSAnalyzer.remaining_time).getMean()
							: 0,
					a.getElement(TSAnalyzer.remaining_time) != null
							? a.getElement(TSAnalyzer.remaining_time).getStandardDeviation() : 0,
					a.getElement(TSAnalyzer.sojourn_time) != null ? a.getElement(TSAnalyzer.sojourn_time).getMean() : 0,
					a.getElement(TSAnalyzer.sojourn_time) != null
							? a.getElement(TSAnalyzer.sojourn_time).getStandardDeviation() : 0,
					a.getElement(TSAnalyzer.trace_frequency) != null ? a.getElement(TSAnalyzer.trace_frequency).getN()
							: 0,
					b.getElement(TSAnalyzer.trace_frequency) != null
							? b.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
					b.getElement(TSAnalyzer.trace_frequency) != null
							? b.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
					b.getElement(TSAnalyzer.elapsed_time) != null ? b.getElement(TSAnalyzer.elapsed_time).getMean() : 0,
					b.getElement(TSAnalyzer.elapsed_time) != null
							? b.getElement(TSAnalyzer.elapsed_time).getStandardDeviation() : 0,
					b.getElement(TSAnalyzer.remaining_time) != null ? b.getElement(TSAnalyzer.remaining_time).getMean()
							: 0,
					b.getElement(TSAnalyzer.remaining_time) != null
							? b.getElement(TSAnalyzer.remaining_time).getStandardDeviation() : 0,
					b.getElement(TSAnalyzer.sojourn_time) != null ? b.getElement(TSAnalyzer.sojourn_time).getMean() : 0,
					b.getElement(TSAnalyzer.sojourn_time) != null
							? b.getElement(TSAnalyzer.sojourn_time).getStandardDeviation() : 0,
					b.getElement(TSAnalyzer.trace_frequency) != null ? b.getElement(TSAnalyzer.trace_frequency).getN()
							: 0);

			dialog.setUndecorated(true);
			dialog.setAlwaysOnTop(true);
			dialog.setLocation(MouseInfo.getPointerInfo().getLocation());

			dialog.setVisible(true);

			Utils.nodePopups.put(element, dialog);
		}
		refreshPanel();
	}

	public void deselected(DotNode element, SVGDiagram diagram) {
		DotPanel.setCSSAttributeOf(DotPanel.getSVGElementOf(diagram, element).getChild(1), "stroke-dasharray", ""); // 1 else
		NodeDetailDialog dialog = (NodeDetailDialog) Utils.nodePopups.get(element);
		if (dialog != null)
			dialog.setVisible(false);

		refreshPanel();
	}

	private void refreshPanel() {
		parentComponent.doLayout();
		parentComponent.repaint();
	}

}
