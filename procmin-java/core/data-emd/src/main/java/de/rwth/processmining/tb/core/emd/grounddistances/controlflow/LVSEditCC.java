package de.rwth.processmining.tb.core.emd.grounddistances.controlflow;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDistEditDiagnose;
import de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics.EditSequence;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;

public class LVSEditCC extends LevenshteinCCStateful implements TraceDistEditDiagnose<BasicTraceCC> {

  @Override
  public EditSequence get_distance_op(BasicTraceCC t1, BasicTraceCC t2) {
		return LevenshteinEdit.calcNormWeightedLevDistWithOp(
		    t1.getTraceCategories(), t2.getTraceCategories());
  }

}
