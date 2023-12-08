package org.processmining.processcomparator.algorithms;

import java.util.Map;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.tsanalyzer2.TSAnalyzer;
import org.processmining.plugins.tsanalyzer2.annotation.Annotation;
import org.processmining.plugins.tsanalyzer2.annotation.AnnotationElement;
import org.processmining.processcomparator.listeners.edge.ProcessMetricEdgeListener;
import org.processmining.processcomparator.listeners.node.ProcessMetricNodeListener;
import org.processmining.processcomparator.model.ConstantDefinitions;
import org.processmining.processcomparator.model.ResultsObject;
import org.processmining.processcomparator.model.SettingsObject;
import org.processmining.processcomparator.tbutils.MetricExtractor;
import org.processmining.processcomparator.view.ComparatorPanel;

/**
 * This class is used to make statistical tests through static methods
 * 
 * @author abolt
 *
 */
public class StatisticsUtils {

	public static Dot enhanceWithStatisticalTests(ComparatorPanel root, Dot graph, ResultsObject results,
			SettingsObject settings, Map<NodeID, DotNode> nodes, Map<Object, DotEdge> edges) {

		if (settings.isComparison_isStatesSelected()) {
			graph = analyzeNodes(root, graph, results, settings.getComparison_selectedComparisonType(),
					settings.getComparison_alpha(), nodes);
		}
		if (settings.isComparison_isTransitionsSelected()) {
			graph = analyzeEdges(root, graph, results, settings.getComparison_selectedComparisonType(),
					settings.getComparison_alpha(), edges);
		}
		return graph;
	}

	/**
	 * This class analyzes the annotations in each node, performa a statistical
	 * test, and based on the test decides whether to highlight the node or not
	 * (depending on the effect size value (d)
	 * 
	 * @param graph
	 * @param results
	 * @param comparator
	 * @param alpha
	 * @param nodes
	 * @return the same Dot graph that was provided as input
	 */
	private static Dot analyzeNodes(ComparatorPanel root, Dot graph, ResultsObject results, String comparator,
			double alpha, Map<NodeID, DotNode> nodes) {

		for (State s : results.getTs().getNodes()) {

			if (nodes.get(s.getId()) != null) {

				Annotation aAnnotation = results.getAts_A().getNodeAnnotation(s);
				Annotation bAnnotation = results.getAts_B().getNodeAnnotation(s);
				Annotation refAnnotation = results.getAts_Union().getNodeAnnotation(s);

				boolean result = false;

				switch (comparator) {
					//non parametric test
					case TSAnalyzer.trace_frequency :
					case TSAnalyzer.ocurrence_frequency :
						result = doNonParametricTest(aAnnotation.getElement(comparator),
								bAnnotation.getElement(comparator), alpha);
						break;
					//parametric test
					case TSAnalyzer.elapsed_time :
					case TSAnalyzer.remaining_time :
					case TSAnalyzer.sojourn_time :
						result = doParametricTest(aAnnotation.getElement(comparator),
								bAnnotation.getElement(comparator), alpha);
						break;

					case ConstantDefinitions.NONE :
						//TODO generate and compare the decision trees
						break;
				}
        //////////////////////////////
        // TB: METRIC EXTRACTION
        //////////////////////////////
				MetricExtractor.instance().setStateSignificance(s, result);
        //////////////////////////////
				if (result) {
					double d = getEffectSize(aAnnotation.getElement(comparator), bAnnotation.getElement(comparator));
          //////////////////////////////
					// TB: METRIC EXTRACTION
          //////////////////////////////
          MetricExtractor.instance().setStateEffectSize(s, d);
          //////////////////////////////
					DrawUtils.highlightNode(nodes.get(s.getId()), d);
				}

				nodes.get(s.getId()).addSelectionListener(
						new ProcessMetricNodeListener(root, s, refAnnotation, aAnnotation, bAnnotation));
			}
		}
		return graph;
	}

	/**
	 * This class analyzes the annotations in each edge, performa a statistical
	 * test, and based on the test decides whether to highlight the edge or not
	 * (depending on the effect size value (d)
	 * 
	 * @param graph
	 * @param results
	 * @param comparator
	 * @param alpha
	 * @param edges
	 * @return the same Dot graph that was provided as input
	 */
	private static Dot analyzeEdges(ComparatorPanel root, Dot graph, ResultsObject results, String comparator,
			double alpha, Map<Object, DotEdge> edges) {

		for (Transition t : results.getTs().getEdges()) {

			if (edges.get(t) != null) {
				Annotation aAnnotation = results.getAts_A().getNodeAnnotation(t);
				Annotation bAnnotation = results.getAts_B().getNodeAnnotation(t);
				Annotation refAnnotation = results.getAts_Union().getNodeAnnotation(t);
				
				boolean result = false;

				switch (comparator) {
					//non parametric test
					case TSAnalyzer.trace_frequency :
					case TSAnalyzer.ocurrence_frequency :
						result = doNonParametricTest(aAnnotation.getElement(comparator),
								bAnnotation.getElement(comparator), alpha);
						break;
					//parametric test
					case TSAnalyzer.duration :
						result = doParametricTest(aAnnotation.getElement(TSAnalyzer.duration),
								bAnnotation.getElement(TSAnalyzer.duration), alpha);
						break;
				}
        //////////////////////////////
        // TB: METRIC EXTRACTION
        //////////////////////////////
				MetricExtractor.instance().setTransitionSignificance(t, result);
        //////////////////////////////
				if (result) {
					double d = getEffectSize(aAnnotation.getElement(comparator), bAnnotation.getElement(comparator));
          //////////////////////////////
					// TB: METRIC EXTRACTION
          //////////////////////////////
          MetricExtractor.instance().setTransitionEffectSize(t, d);
          //////////////////////////////
					DrawUtils.highlightEdge(edges.get(t), d);
				}

				edges.get(t).addSelectionListener(
						new ProcessMetricEdgeListener(root, t, refAnnotation, aAnnotation, bAnnotation));
			}
		}
		return graph;
	}

	/**
	 * The T test (Parametric)
	 * 
	 * @param a
	 * @param b
	 * @param alpha
	 * @return
	 */
	private static boolean doParametricTest(AnnotationElement a, AnnotationElement b, double alpha) {
		//if there is no variance, the means are different
		if (a == null || b == null)
			return true;

		if (a.getVariance() == 0 && b.getVariance() == 0) {
			if (a.getMean() != b.getMean())
				return true;
			else
				return false;
		}

		double t = (a.getMean() - b.getMean()) / Math.sqrt((a.getVariance() / a.getN()) + (b.getVariance() / b.getN()));
		double degrees_of_freedom = Math.rint(Math.pow((a.getVariance() / a.getN()) + (b.getVariance() / b.getN()), 2)
				/ ((Math.pow(a.getVariance(), 2) / (Math.pow(a.getN(), 2) * (a.getN() - 1)))
						+ (Math.pow(b.getVariance(), 2) / (Math.pow(b.getN(), 2) * (b.getN() - 1)))));

		if (t > 0)
			t = 0 - Math.abs(t);

		TDistribution t_dist = new TDistribution(degrees_of_freedom);

		double p_value = t_dist.cumulativeProbability(t);

		if (p_value <= (alpha / 200.0)) //two tailed check
		{
			//System.out.println(p_value);
			return true;
		}

		else
			return false;
	}

	/**
	 * The Mann-Whitney U test (Non-Parametric)
	 * 
	 * @param a
	 * @param b
	 * @param alpha
	 * @return
	 */
	private static boolean doNonParametricTest(AnnotationElement a, AnnotationElement b, double alpha) {

		//if there is no variance, the means are different
		if (a.getVariance() == 0 && b.getVariance() == 0) {
			if (a.getMean() != b.getMean())
				return true;
			else
				return false;
		}

		MannWhitneyUTest test = new MannWhitneyUTest();
		double p = 1;
		try {
			p = test.mannWhitneyUTest(a.getValues(), b.getValues());
		} catch (NoDataException e) {
			return true;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (p <= (alpha / 100.0))
			return true;
		return false;

	}

	/**
	 * this method returns the effect size in terms of Cohen's d value:
	 * Breakpoints are at d = 0.2 (small), 0.5 (medium) and 0.8 (large)
	 * 
	 */
	private static double getEffectSize(AnnotationElement a, AnnotationElement b) {

		//if there is no variance, the means are different
		if (a == null || b == null)
			return 0;

		//if there is no variance, don't even bother calculating stuff
		if (a.getVariance() == 0 && b.getVariance() == 0) {
			if (a.getMean() > b.getMean())
				return +4.0;
			else if (a.getMean() < b.getMean())
				return -4.0;
			else
				return 0;
		}

		double pooledStdDev = Math
				.sqrt(((a.getN() - 1) * a.getVariance() + (b.getN() - 1) * b.getVariance()) / (a.getN() + b.getN()));

		return (a.getMean() - b.getMean()) / pooledStdDev;
	}

}
