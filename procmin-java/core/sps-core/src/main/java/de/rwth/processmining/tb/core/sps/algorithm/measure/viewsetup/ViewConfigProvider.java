package de.rwth.processmining.tb.core.sps.algorithm.measure.viewsetup;

import java.util.List;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.ViewConfigVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.HFDDLogTransformStep;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;

public interface ViewConfigProvider<V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> {
	
	/**
	 * 
	 * Provide a view configuration based on logs.
	 * 
	 * @param v Vertex for which the view config will be created
	 * @param vertexLogFilterStack Stack of applied log transformations.
	 * @return
	 */
	public<B extends V> ViewConfigVariant<V, F, D> provideViewConfig(HFDDVertex v, 
	    List<HFDDLogTransformStep<B>> vertexLogFilterStack);

}
