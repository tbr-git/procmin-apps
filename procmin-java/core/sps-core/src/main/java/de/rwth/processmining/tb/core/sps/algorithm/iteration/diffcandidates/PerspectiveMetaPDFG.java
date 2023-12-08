package de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptionLog.LogType;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;

public class PerspectiveMetaPDFG extends PerspectiveDescriptor {
  
  private final int iteration;
  
  private final String pdfgName; 

  public PerspectiveMetaPDFG(int iteration, String pdfgName) {
    super();
    this.iteration = iteration;
    this.pdfgName = pdfgName;
  }

  @Override
  public String getID() {
		return "MetaPDFG " + pdfgName + " (iteration " + this.iteration + ")";
  }

  @Override
  public int hashCode() {
		int hash = LogType.FOCUS.hashCode();
		hash = 31 * hash + pdfgName.hashCode();
		hash = 31 * hash + iteration;
		return hash;
  }

}
