package de.rwth.processmining.tb.core.emd.grounddistances;

import de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics.EditSequence;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;

public interface TraceDistEditDiagnose<F extends TraceDescriptor> extends TraceDescDistCalculator<F> {
	public EditSequence get_distance_op(F t1, F t2);
}
