package org.processmining.processcomparator.algorithms;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.tsanalyzer2.TSAnalyzer;
import org.processmining.plugins.tsanalyzer2.annotation.Annotation;
import org.processmining.processcomparator.model.ConstantDefinitions;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.model.ResultsObject;
import org.processmining.processcomparator.model.SettingsObject;
import org.processmining.processcomparator.view.ComparatorPanel;

/**
 * This class has static methods to transform ResultObjects into DotPanels (they
 * are visualizable)
 * 
 * @author abolt
 *
 */
public class DrawUtils {

	public static Dot createGraph(ResultsObject results, SettingsObject settings, ComparatorPanel root,
			InputObject input) {

		Dot graph = new Dot();
		Map<NodeID, DotNode> nodes = new HashMap<NodeID, DotNode>(); //store all the nodes
		Map<Object, DotEdge> edges = new HashMap<Object, DotEdge>(); //store all the edges	

		double maxDuration = 0;
		for (Transition t : results.getTs().getEdges()) // calculate the max duration so we know the max thickness of edges 
			if (results.getAts_Union().getNodeAnnotation(t).getElement(TSAnalyzer.duration) != null
					&& maxDuration < results.getAts_Union().getNodeAnnotation(t).getElement(TSAnalyzer.duration)
							.getMean())
				maxDuration = results.getAts_Union().getNodeAnnotation(t).getElement(TSAnalyzer.duration).getMean();

		double maxElapsedTime = 0, maxRemainingTime = 0, maxWaitingTime = 0; //lead time is for elapsed and remaining, waiting time is for soujourn.
		for (State s : results.getTs().getNodes()) {
			if (results.getAts_Union().getNodeAnnotation(s).getElement(TSAnalyzer.elapsed_time) != null
					&& maxElapsedTime < results.getAts_Union().getNodeAnnotation(s).getElement(TSAnalyzer.elapsed_time)
							.getMean())
				//saves the maximum lead time for the thickness of node borders.
				maxElapsedTime = results.getAts_Union().getNodeAnnotation(s).getElement(TSAnalyzer.elapsed_time)
						.getMean();
			if (results.getAts_Union().getNodeAnnotation(s).getElement(TSAnalyzer.remaining_time) != null
					&& maxRemainingTime < results.getAts_Union().getNodeAnnotation(s)
							.getElement(TSAnalyzer.remaining_time).getMean())
				//saves the maximum lead time for the thickness of node borders.
				maxRemainingTime = results.getAts_Union().getNodeAnnotation(s).getElement(TSAnalyzer.remaining_time)
						.getMean();
			if (results.getAts_Union().getNodeAnnotation(s).getElement(TSAnalyzer.sojourn_time) != null
					&& maxWaitingTime < results.getAts_Union().getNodeAnnotation(s).getElement(TSAnalyzer.sojourn_time)
							.getMean()) //saves the maximum waiting time for the thickness of node borders
				maxWaitingTime = results.getAts_Union().getNodeAnnotation(s).getElement(TSAnalyzer.sojourn_time)
						.getMean();

		}

		double threshold = 0;
		if (settings.isGraph_isFilterSelected())
			threshold = settings.getGraph_filterThreshold() / 100.0; // write an edge only if it is above the threshold

		//Analyze states
		for (State s : results.getTs().getNodes()) {

			s.setLabel(s.getAttributeMap().get(AttributeMap.TOOLTIP).toString());

			Annotation refAnnotation = results.getAts_Union().getNodeAnnotation(s);
			//Annotation aAnnotation = results.getAts_A().getNodeAnnotation(s);
			//Annotation bAnnotation = results.getAts_B().getNodeAnnotation(s);

			//add and initialize a state
			nodes.put(s.getId(), graph.addNode(s.getLabel()));
			nodes.get(s.getId()).setOption("shape", "oval");
			nodes.get(s.getId()).setOption("style", "solid");
			nodes.get(s.getId()).setSelectable(true);
			nodes.get(s.getId()).setLabel(s.getLabel());

			//			nodes.get(s.getId()).addSelectionListener(
			//					new ProcessMetricsNodeListener(root, s.getLabel(), null, null, refAnnotation, aAnnotation, bAnnotation, s,
			//							TransitionSystemUtils.getNextStates(results.getTs(), s), input.getMerged()));

			//set the thickness 

			switch (settings.getGraph_StateThicknessSelection()) {
				case TSAnalyzer.trace_frequency :
					nodes.get(s.getId()).setOption("penwidth",
							Integer.toString((int) (refAnnotation.getElement(TSAnalyzer.trace_frequency).getMean()
									* ConstantDefinitions.MAX_EDGE_WIDTH) + 1)); //border width
					break;
				case TSAnalyzer.elapsed_time :
					if (maxElapsedTime > 0)
						nodes.get(s.getId()).setOption("penwidth", Integer.toString(
								(int) ((refAnnotation.getElement(TSAnalyzer.elapsed_time).getMean() / maxElapsedTime)
										* ConstantDefinitions.MAX_EDGE_WIDTH) + 1)); //border width
					break;
				case TSAnalyzer.remaining_time :
					if (maxRemainingTime > 0)
						nodes.get(s.getId()).setOption("penwidth",
								Integer.toString((int) ((refAnnotation.getElement(TSAnalyzer.remaining_time).getMean()
										/ maxRemainingTime) * ConstantDefinitions.MAX_EDGE_WIDTH) + 1)); //border width
					break;
				case TSAnalyzer.sojourn_time :
					if (maxWaitingTime > 0)
						nodes.get(s.getId()).setOption("penwidth", Integer.toString(
								(int) ((refAnnotation.getElement(TSAnalyzer.sojourn_time).getMean() / maxWaitingTime)
										* ConstantDefinitions.MAX_EDGE_WIDTH) + 1)); //edge width
					break;
			}
		}

		// Analyze transitions
		for (Transition t : results.getTs().getEdges()) {

			//apply thresholds to the merged TS
			if (results.getAts_Union().getNodeAnnotation(t).getElement(TSAnalyzer.trace_frequency)
					.getMean() > threshold) {
				Annotation refAnnotation = results.getAts_Union().getNodeAnnotation(t);
				//				Annotation aAnnotation = results.getAts_A().getNodeAnnotation(t);
				//				Annotation bAnnotation = results.getAts_B().getNodeAnnotation(t);

				//add and initialize an edge
				edges.put(t, graph.addEdge(nodes.get(t.getSource().getId()), nodes.get(t.getTarget().getId())));
				edges.get(t).setOption("color", ColourMap.toHexString(Color.BLACK));
				edges.get(t).setSelectable(true);
				//				edges.get(t.getIdentifier())
				//						.addSelectionListener(new ProcessMetricsNodeListener(root, t.getLabel(), t.getSource().getLabel(),
				//								t.getTarget().getLabel(), refAnnotation, aAnnotation, bAnnotation, null, null, null));

				switch (settings.getGraph_TransitionThicknessSelection()) {
					case TSAnalyzer.trace_frequency :
						edges.get(t).setOption("penwidth",
								Integer.toString((int) (refAnnotation.getElement(TSAnalyzer.trace_frequency).getMean()
										* ConstantDefinitions.MAX_EDGE_WIDTH) + 1)); //edge width
						break;
					case TSAnalyzer.duration :
						if (maxDuration > 0)
							edges.get(t).setOption("penwidth", Integer.toString(
									(int) ((refAnnotation.getElement(TSAnalyzer.duration).getMean() / maxDuration)
											* ConstantDefinitions.MAX_EDGE_WIDTH) + 1)); //edge width
						break;
				}

				edges.get(t).setOption("style", "filled");

				if (settings.isGraph_isShowTransitionLabelsSelected())
					edges.get(t).setLabel(t.getLabel());
				else
					edges.get(t).setLabel("");
			}
		}

		Utils.nodePopups.clear();
		Utils.edgePopups.clear();

		if (settings.isComparison_metric_selected()) {
			clearUnconnectedNodes(graph, nodes, results);
			graph = StatisticsUtils.enhanceWithStatisticalTests(root, graph, results, settings, nodes, edges);

		} else if (settings.isComparison_tree_selected()) {
			clearUnconnectedNodes(graph, nodes, results);
			try {
				graph = DecisionTreeUtils.enhanceWithDecisionTrees(root, graph, results, settings, nodes, edges, input);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return graph;
	}

	public static DotPanel returnAsPanel(Dot graph) {
		return new DotPanel(graph);
	}

	public static void highlightEdge(DotEdge edge, double d) {
		edge.setOption("color", getColor(d));
		edge.setOption("fontcolor", getColor(d));
	}

	public static void highlightNode(DotNode node, double d) {
		node.setOption("style", "filled");
		node.setOption("fillcolor", getColor(d));
	}

	public static void highlightNode(DotNode node, Color c) {
		node.setOption("style", "filled");
		node.setOption("fillcolor", ColourMap.toHexString(c));
	}

	public static String getColor(double d) {

		String answer = "";

		if (d > 0) {
			if (d < 0.2)
				answer = "#abd9e9"; //light blue
			else if (d < 0.5)
				answer = "#74add1";
			else if (d < 0.8)
				answer = "#4575b4";
			else
				answer = "#313695"; //dark blue
		} else if (d < 0) {
			if (d > -0.2)
				answer = "#fdae61"; //light red
			else if (d > -0.5)
				answer = "#f46d43";
			else if (d > -0.8)
				answer = "#d73027";
			else
				answer = "#a50026"; //dark red
		}
		return answer;
	}

	private static Dot clearUnconnectedNodes(Dot graph, Map<NodeID, DotNode> nodes, ResultsObject results) {
		DotEdge aux;
		boolean connected;

		for (State s : results.getTs().getNodes()) { // if a node is not connected through edges, remove it from the graph
			DotNode node1 = nodes.get(s.getId());
			connected = false;
			for (DotNode node2 : graph.getNodesRecursive())
			//if(!node1.getLabel().matches(node2.getLabel())) // this is for not considering self-loops
			{
				aux = graph.getFirstEdge(node1, node2); // outgoing edges
				if (aux != null)
					connected = true;
				aux = graph.getFirstEdge(node2, node1); // incoming edges
				if (aux != null)
					connected = true;
			}
			if (!connected) {
				graph.removeNode(node1);
				nodes.remove(s.getId());
			}
		}
		return graph;
	}

	public static double get_perentageOfDifferences(Dot input) {
		double diff = 0;

		for (DotEdge edge : input.getEdgesRecursive())
			if (edge.getOption("color").compareTo(ColourMap.toHexString(Color.BLACK)) != 0) //if edge is not black, there were statistically significant differences
				diff++;
		for (DotNode node : input.getNodesRecursive())
			if (node.getOption("fillcolor") != null) //if node had a color, there were statistically significant differences
				diff++;

		return  (diff / (input.getEdgesRecursive().size() + input.getNodesRecursive().size())) * 100;
				
	}
}
