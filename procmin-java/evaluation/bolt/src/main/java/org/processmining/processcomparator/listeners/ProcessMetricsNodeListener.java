//package org.processmining.processcomparator.listeners;
//
//import java.awt.MouseInfo;
//import java.util.Set;
//
//import org.deckfour.xes.model.XLog;
//import org.processmining.models.graphbased.directed.transitionsystem.State;
//import org.processmining.plugins.graphviz.dot.DotEdge;
//import org.processmining.plugins.graphviz.dot.DotElement;
//import org.processmining.plugins.graphviz.dot.DotNode;
//import org.processmining.plugins.graphviz.visualisation.DotPanel;
//import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;
//import org.processmining.plugins.tsanalyzer2.TSAnalyzer;
//import org.processmining.plugins.tsanalyzer2.annotation.Annotation;
//import org.processmining.processcomparator.algorithms.Utils;
//import org.processmining.processcomparator.view.ComparatorPanel;
//import org.processmining.processcomparator.view.dialog.EdgeDetailDialog;
//import org.processmining.processcomparator.view.dialog.NodeDetailDialog;
//
//import com.kitfox.svg.SVGDiagram;
//
//public class ProcessMetricsNodeListener implements DotElementSelectionListener {
//
//	private String label, source, target;
//	private Annotation ref, a, b;
//	private ComparatorPanel panel;
//	private State sourceState;
//	private Set<State> targetStates;
//	private XLog log;
//
//	public ProcessMetricsNodeListener(ComparatorPanel parent, String label, String source, String target, Annotation ref,
//			Annotation a, Annotation b, State sourceState, Set<State> targetStates, XLog log) {
//		this.label = label;
//		this.source = source;
//		this.target = target;
//		this.ref = ref;
//		this.a = a;
//		this.b = b;
//		this.panel = parent;
//
//		this.sourceState = sourceState;
//		this.targetStates = targetStates;
//		this.log = log;
//	}
//
//	public void selected(DotElement element, SVGDiagram diagram) {
//		if (element instanceof DotNode) {
//			if (element.getOption("style").compareTo("wedged") == 0)
//				DotPanel.setCSSAttributeOf(DotPanel.getSVGElementOf(diagram, element).getChild(3), "stroke-dasharray",
//						"5,2"); // 3 if filled with 2 colors
//			else
//				DotPanel.setCSSAttributeOf(DotPanel.getSVGElementOf(diagram, element).getChild(1), "stroke-dasharray",
//						"5,2"); // 1 else
//
//			NodeDetailDialog dialog = Utils.nodePopups.get(element);
//			if (dialog != null) {
//				dialog.setLocation(MouseInfo.getPointerInfo().getLocation());
//				dialog.setVisible(true);
//			} else { //transforms variances into standard deviations with Math.sqrt
//				dialog = new NodeDetailDialog(label,
//						ref.getElement(TSAnalyzer.trace_frequency) != null
//								? ref.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
//						ref.getElement(TSAnalyzer.trace_frequency) != null
//								? ref.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
//						ref.getElement(TSAnalyzer.elapsed_time) != null
//								? ref.getElement(TSAnalyzer.elapsed_time).getMean() : 0,
//						ref.getElement(TSAnalyzer.elapsed_time) != null
//								? ref.getElement(TSAnalyzer.elapsed_time).getStandardDeviation() : 0,
//						ref.getElement(TSAnalyzer.remaining_time) != null
//								? ref.getElement(TSAnalyzer.remaining_time).getMean() : 0,
//						ref.getElement(TSAnalyzer.remaining_time) != null
//								? ref.getElement(TSAnalyzer.remaining_time).getStandardDeviation() : 0,
//						ref.getElement(TSAnalyzer.sojourn_time) != null
//								? ref.getElement(TSAnalyzer.sojourn_time).getMean() : 0,
//						ref.getElement(TSAnalyzer.sojourn_time) != null
//								? ref.getElement(TSAnalyzer.sojourn_time).getStandardDeviation() : 0,
//						ref.getElement(TSAnalyzer.trace_frequency) != null
//								? ref.getElement(TSAnalyzer.trace_frequency).getN() : 0,
//						a.getElement(TSAnalyzer.trace_frequency) != null
//								? a.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
//						a.getElement(TSAnalyzer.trace_frequency) != null
//								? a.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
//						a.getElement(TSAnalyzer.elapsed_time) != null ? a.getElement(TSAnalyzer.elapsed_time).getMean()
//								: 0,
//						a.getElement(TSAnalyzer.elapsed_time) != null
//								? a.getElement(TSAnalyzer.elapsed_time).getStandardDeviation() : 0,
//						a.getElement(TSAnalyzer.remaining_time) != null
//								? a.getElement(TSAnalyzer.remaining_time).getMean() : 0,
//						a.getElement(TSAnalyzer.remaining_time) != null
//								? a.getElement(TSAnalyzer.remaining_time).getStandardDeviation() : 0,
//						a.getElement(TSAnalyzer.sojourn_time) != null ? a.getElement(TSAnalyzer.sojourn_time).getMean()
//								: 0,
//						a.getElement(TSAnalyzer.sojourn_time) != null
//								? a.getElement(TSAnalyzer.sojourn_time).getStandardDeviation() : 0,
//						a.getElement(TSAnalyzer.trace_frequency) != null
//								? a.getElement(TSAnalyzer.trace_frequency).getN() : 0,
//						b.getElement(TSAnalyzer.trace_frequency) != null
//								? b.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
//						b.getElement(TSAnalyzer.trace_frequency) != null
//								? b.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
//						b.getElement(TSAnalyzer.elapsed_time) != null ? b.getElement(TSAnalyzer.elapsed_time).getMean()
//								: 0,
//						b.getElement(TSAnalyzer.elapsed_time) != null
//								? b.getElement(TSAnalyzer.elapsed_time).getStandardDeviation() : 0,
//						b.getElement(TSAnalyzer.remaining_time) != null
//								? b.getElement(TSAnalyzer.remaining_time).getMean() : 0,
//						b.getElement(TSAnalyzer.remaining_time) != null
//								? b.getElement(TSAnalyzer.remaining_time).getStandardDeviation() : 0,
//						b.getElement(TSAnalyzer.sojourn_time) != null ? b.getElement(TSAnalyzer.sojourn_time).getMean()
//								: 0,
//						b.getElement(TSAnalyzer.sojourn_time) != null
//								? b.getElement(TSAnalyzer.sojourn_time).getStandardDeviation() : 0,
//						b.getElement(TSAnalyzer.trace_frequency) != null
//								? b.getElement(TSAnalyzer.trace_frequency).getN() : 0);
//
//				dialog.setUndecorated(true);
//				dialog.setAlwaysOnTop(true);
//				dialog.setLocation(MouseInfo.getPointerInfo().getLocation());
//
//				dialog.setVisible(true);
//
//				Utils.nodePopups.put((DotNode) element, dialog);
//			}
//
//			panel.doLayout();
//			panel.repaint();
//
//			//print stuff about decision tree
////			if (log != null) TODO
////				DecisionTreeUtils.createDecisionTree(log, sourceState, targetStates);
//
//		} else if (element instanceof DotEdge) {
//			DotPanel.setCSSAttributeOf(DotPanel.getSVGElementOf(diagram, element).getChild(1), "stroke-dasharray",
//					"10,5");
//
//			EdgeDetailDialog dialog = Utils.edgePopups.get(element);
//			if (dialog != null) {
//				dialog.setLocation(MouseInfo.getPointerInfo().getLocation());
//				dialog.setVisible(true);
//			} else { //transforms variances into standard deviations with Math.sqrt
//				dialog = new EdgeDetailDialog(label, source, target,
//						ref.getElement(TSAnalyzer.trace_frequency) != null
//								? ref.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
//						ref.getElement(TSAnalyzer.trace_frequency) != null
//								? ref.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
//						ref.getElement(TSAnalyzer.duration) != null ? ref.getElement(TSAnalyzer.duration).getMean() : 0,
//						ref.getElement(TSAnalyzer.duration) != null
//								? ref.getElement(TSAnalyzer.duration).getStandardDeviation() : 0,
//						ref.getElement(TSAnalyzer.trace_frequency) != null
//								? ref.getElement(TSAnalyzer.trace_frequency).getN() : 0,
//						a.getElement(TSAnalyzer.trace_frequency) != null
//								? a.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
//						a.getElement(TSAnalyzer.trace_frequency) != null
//								? a.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
//						a.getElement(TSAnalyzer.duration) != null ? a.getElement(TSAnalyzer.duration).getMean() : 0,
//						a.getElement(TSAnalyzer.duration) != null
//								? a.getElement(TSAnalyzer.duration).getStandardDeviation() : 0,
//						a.getElement(TSAnalyzer.trace_frequency) != null
//								? a.getElement(TSAnalyzer.trace_frequency).getN() : 0,
//						b.getElement(TSAnalyzer.trace_frequency) != null
//								? b.getElement(TSAnalyzer.trace_frequency).getMean() : 0,
//						b.getElement(TSAnalyzer.trace_frequency) != null
//								? b.getElement(TSAnalyzer.trace_frequency).getStandardDeviation() : 0,
//						b.getElement(TSAnalyzer.duration) != null ? b.getElement(TSAnalyzer.duration).getMean() : 0,
//						b.getElement(TSAnalyzer.duration) != null
//								? b.getElement(TSAnalyzer.duration).getStandardDeviation() : 0,
//						b.getElement(TSAnalyzer.trace_frequency) != null
//								? b.getElement(TSAnalyzer.trace_frequency).getN() : 0);
//
//				dialog.setUndecorated(true);
//				dialog.setAlwaysOnTop(true);
//				dialog.setLocation(MouseInfo.getPointerInfo().getLocation());
//
//				dialog.setVisible(true);
//
//				Utils.edgePopups.put((DotEdge) element, dialog);
//			}
//			panel.doLayout();
//			panel.repaint();
//		}
//	}
//
//	public void deselected(DotElement element, SVGDiagram diagram) {
//		if (element instanceof DotNode) {
//			DotPanel.setCSSAttributeOf(DotPanel.getSVGElementOf(diagram, element).getChild(1), "stroke-dasharray", ""); // 1 else
//			NodeDetailDialog dialog = Utils.nodePopups.get(element);
//			if (dialog != null)
//				dialog.setVisible(false);
//			panel.doLayout();
//			panel.repaint();
//
//		} else if (element instanceof DotEdge) {
//			DotPanel.setCSSAttributeOf(DotPanel.getSVGElementOf(diagram, element).getChild(1), "stroke-dasharray", "");
//			EdgeDetailDialog dialog = Utils.edgePopups.get(element);
//			if (dialog != null)
//				dialog.setVisible(false);
//			panel.doLayout();
//			panel.repaint();
//		}
//	}
//}
