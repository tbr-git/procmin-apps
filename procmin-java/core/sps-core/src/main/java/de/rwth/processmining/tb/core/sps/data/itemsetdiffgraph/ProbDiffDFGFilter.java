package de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph;

@FunctionalInterface
public interface ProbDiffDFGFilter {
  
  public ProbDiffDFG apply(ProbDiffDFG probDiffDfg);

}
