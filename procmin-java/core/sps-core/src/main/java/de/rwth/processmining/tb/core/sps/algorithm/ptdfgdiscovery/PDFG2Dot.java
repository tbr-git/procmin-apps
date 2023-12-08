package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.data.Range;
import org.jgrapht.Graph;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;

import de.rwth.processmining.tb.core.data.statistics.ActivityOccurencePosition;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.CandidatesMetaDiagnostic;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.DiffCandidate;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFG;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFGEdge;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFGType;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFGVertex;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFGVertexEnd;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFGVertexStart;

public class PDFG2Dot {
  private final static Logger logger = LogManager.getLogger(PDFG2Dot.class);
  
  private float maxLineWidth = 5.5f;

  private float minLineWidth = 0.5f;

  private DecimalFormat formatProbDisplay;
  
  private BlueGrayRedPaintScale ps;
  
  public PDFG2Dot() {
    ps = new BlueGrayRedPaintScale(new Range(-1f, 1f));
    formatProbDisplay = new DecimalFormat("0.##");
  }

  public PDFG2Dot(float minLineWidth, float maxLineWidth, float colorRangeBound) {
    this.minLineWidth = minLineWidth;
    this.maxLineWidth = maxLineWidth;
    ps = new BlueGrayRedPaintScale(new Range(-1 * colorRangeBound, colorRangeBound));
    formatProbDisplay = new DecimalFormat("0.##");
  }
  
  public Dot pdfg2Dot(CandidatesMetaDiagnostic context, ProbDiffDFG pdfg, Optional<ActivityOccurencePosition> actPos) {
    Graph<ProbDiffDFGVertex, ProbDiffDFGEdge> g = pdfg.getGraph();

		////////////////////////////////////////////////////////////
		// Create Dot Image
		////////////////////////////////////////////////////////////
		Dot dot = new Dot();
		
		dot.setOption("forcelabels", "true");
		dot.setOption("ranksep", "0.2");
		dot.setOption("nodesep", "0.1");

		////////////////////
		// States
		////////////////////
		Map<ProbDiffDFGVertex, DotNode> state2dotNode = new HashMap<>(g.vertexSet().size());
		for (ProbDiffDFGVertex s : g.vertexSet()) {
		  String label;
		  if (s instanceof ProbDiffDFGVertexStart vStart) {
		    label = createStartVertexLabel(pdfg, context, actPos);
		  }
		  else if (s instanceof ProbDiffDFGVertexEnd) {
		    label = "&#8869;";
		  }
		  else {
		    StringBuilder builderLabel = new StringBuilder();
		    builderLabel.append("< " + s.getName());
		    builderLabel.append("<br/>");
		    builderLabel.append("(");
		    builderLabel.append(formatProbDisplay.format(s.getProbLeft()));
		    builderLabel.append(" | ");
		    builderLabel.append(formatProbDisplay.format(s.getProbRight()));
		    builderLabel.append(") >");
		    label = builderLabel.toString();
		  }
		  DotNode dotNode = dot.addNode(label);
		  dotNode.setOption("shape", "box");

		  // Width
		  dotNode.setOption("penwidth", Float.toString(getNodeLineWidth(s.getProbLeft(), s.getProbRight())));

		  Color c = (Color) ps.getPaint(s.getProbRight() - s.getProbLeft());
		  dotNode.setOption("color", color2String(c));
		  
		  state2dotNode.put(s, dotNode);
		}
    
		////////////////////
		// Transitions
		////////////////////
		// Edges for which we cannot find adjacent vertices
		int countFailedEdges = 0;
		for (ProbDiffDFGEdge e : g.edgeSet()) {
		  ProbDiffDFGVertex dfgVertexSource = g.getEdgeSource(e);
		  ProbDiffDFGVertex dfgVertexTarget = g.getEdgeTarget(e);
		  
		  // Should have endpoints in the PDFG
		  if (dfgVertexSource == null || dfgVertexTarget == null) {
		    countFailedEdges++;
		    continue;
		  }
		  
		  DotNode source = state2dotNode.get(dfgVertexSource);
		  DotNode target = state2dotNode.get(dfgVertexTarget);

		  // Endpoints should already have been added to the Dot picture
		  if (source == null || target == null) {
		    countFailedEdges++;
		    continue;
		  }
		  
			String label = "(" + formatProbDisplay.format(e.getProbLeft())  
			    + " | " + formatProbDisplay.format(e.getProbRight()) + ")";
		  DotEdge dotEdge = dot.addEdge(source, target, label);
		  
		  // Width
		  dotEdge.setOption("penwidth", 
          Float.toString(getEdgeLineWidth(e.getProbLeft(), e.getProbRight())));
		  // Color
		  Color c = (Color) ps.getPaint(e.getProbRight() - e.getProbLeft());
		  dotEdge.setOption("color", color2String(c));
		}
		
		if (countFailedEdges > 0) {
		  logger.warn("Failed to add {} edges", countFailedEdges);
		}
		
		return dot;
  }
  
  public String createStartVertexLabel(ProbDiffDFG pDfg, CandidatesMetaDiagnostic context, 
      Optional<ActivityOccurencePosition> actPos) {
    final BitSet actCandidate = context.diffCandidate().v().getVertexInfo().getActivities();
    
    // Comparator that puts activities that occur in 
    // the candidate set first (if sorted in ascending order)
    Comparator<Integer> candidateFirst = (c1, c2) -> {
      boolean actCandContainsC1 = actCandidate.get(c1);
      boolean actCandContainsC2 = actCandidate.get(c2);
      if (actCandContainsC1 == actCandContainsC2) {
        return 0;
      }
      else if (actCandContainsC1) {
        return -1;
      }
      else {
        return 1;
      }
    };

    // Comparator used to sort the complementary differences "sets"
    // 1. Put activities that occur in the LEADING candidate first
    // 2. (Sort on position of average first occurrence in trace)
    Comparator<Integer> sortCategories = candidateFirst;
    
    // If sorting by position enabled.
    // - Sort candidate vertex "set"
    // - Add sorting to complementary differences
    if (actPos.isPresent()) {
      Comparator<Integer> comparePositionAscending = 
          (c1, c2) -> Double.compare(actPos.get().getAvgFirstPosition(c1), actPos.get().getAvgFirstPosition(c2));
      
      // Extend comparator for the complementary sets
      sortCategories = sortCategories.thenComparing(comparePositionAscending);
    }
    final Comparator<Integer> sortCategoriesFrozen = sortCategories;
    
    StringBuilder builderLabel = new StringBuilder("<"); // Graphviz HTML
    // Add table header
    builderLabel.append("<TABLE border=\"0\" cellspacing=\"2\" cellpadding=\"5\" >");
    // Add table header row
    builderLabel.append("<TR><td bgcolor=\"#d9dbde\">Act.</td><td bgcolor=\"#d9dbde\">pLeft</td><td bgcolor=\"#d9dbde\">pRight</td></TR>");

    addVertexInfoTableRow(builderLabel, pDfg, context.diffCandidate(), sortCategoriesFrozen, "&#164;");
    for (DiffCandidate c : context.complDifferences()) {
      addVertexInfoTableRow(builderLabel, pDfg, c, sortCategoriesFrozen, "&#187;");
    }
    //////////////////////////////
    // Add Footer
    //////////////////////////////
    builderLabel.append("<tr>");
    builderLabel.append("<td colspan=\"3\" bgcolor=\"#d9dbde\">");
    if (pDfg.getType() == ProbDiffDFGType.NORMALIZED) {
      builderLabel.append("Normalized");
    }
    else {
      builderLabel.append("Frac. Log: (");
      builderLabel.append(formatProbDisplay.format(pDfg.getProbabilityNonEmptyLeft()));
      builderLabel.append(" | ");
      builderLabel.append(formatProbDisplay.format(pDfg.getProbabilityNonEmptyRight()));
      builderLabel.append(")");
    }
    builderLabel.append("</td>");
    builderLabel.append("</tr>");
    builderLabel.append("</TABLE>");

    builderLabel.append(">"); // Graphviz HTML-string end
    String label = builderLabel.toString();
    //logger.debug(label);
    return label;
  }
  
  private void addVertexInfoTableRow(StringBuilder builder, ProbDiffDFG pDfg, DiffCandidate c, Comparator<Integer> sortCategories, 
      String preSetSymbol) {
    // Category mapper
    final CategoryMapper cm = c.v().getVertexInfo().getCategoryMapper();
    builder.append("<TR>");
    
    //////////////////////////////
    // Activity Cell
    //////////////////////////////
    String activitySet = c.v().getVertexInfo().getActivities().stream().boxed()
        .sorted(sortCategories) // Sort
        .map(cm::getActivity4Category)  // Activity Label
        .collect(Collectors.joining(",", "{", "}")); // Join into set

    builder.append("<TD ");
    builder.append("align=\"left\">");
    builder.append(preSetSymbol);
    builder.append(activitySet);
    builder.append("</TD>");
  
    //////////////////////////////
    // Probabilities 
    //////////////////////////////
    double probLeft = c.v().getVertexInfo().getProbabilityLeft();
    double probRight = c.v().getVertexInfo().getProbabilityRight();
    
    // Adapt for normalized case
    // (Considering the current architecture, this approach is not super clean!
    //  We should query it from the data and don't exploit the implicit knowledge
    //  about the relation. Yet it is much easier.)
    if (pDfg.getType() == ProbDiffDFGType.NORMALIZED) {
      probLeft /= pDfg.getProbabilityNonEmptyLeft();
      probRight /= pDfg.getProbabilityNonEmptyRight();
    }
    
    // Left
    builder.append("<TD ");
    builder.append("bgcolor=\"");
    builder.append(color2String((Color) ps.getPaint(- 1* probLeft)));
    builder.append("\">");
    builder.append(formatProbDisplay.format(probLeft));
    builder.append("</TD>");
    // Right
    builder.append("<TD ");
    builder.append("bgcolor=\"");
    builder.append(color2String((Color) ps.getPaint(probRight)));
    builder.append("\">");
    builder.append(formatProbDisplay.format(probRight));
    builder.append("</TD>");
    
    builder.append("</TR>");
  }
  
  /**
   * Convert the color into a color string that can be used by graphviz.
   * Format: #ffdd00
   * @param c Color
   * @return Hex-color string
   */
  private String color2String(Color c) {
    StringBuilder builderColString = new StringBuilder("#");
    builderColString.append(String.format("%02X", c.getRed()));
    builderColString.append(String.format("%02X", c.getGreen()));
    builderColString.append(String.format("%02X", c.getBlue()));
    
    return builderColString.toString();
        
  }
  
  private float getEdgeLineWidth(float probL, float probR) {
    float maxProb = Math.max(probL, probR);
    // Limit due to repetitions
    maxProb = Math.min(maxProb, 2.5f);
    float lw = (1f - maxProb) * minLineWidth + maxProb * maxLineWidth;
    return lw;
    //return Math.max(Math.max(probL, probR), 0.001f) * maxLineWidth;
  }

  private float getNodeLineWidth(float probL, float probR) {
    return Math.max(Math.max(probL, probR), 0.001f) * maxLineWidth;
  }

}
