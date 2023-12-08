package de.rwth.processmining.tb.core.sps.algorithm.measure.viewsetup;

import java.util.List;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.ViewConfigVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.measure.logtransform.HFDDLogTransformStep;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;

public class ViewConfigConstantProvider
    <V extends CVariant, F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
  implements ViewConfigProvider<V, F, D> {
	
	private final ViewConfigVariant<V, F, D> viewConfig;
	
	public ViewConfigConstantProvider(ViewConfigVariant<V, F, D> viewConfig) {
		this.viewConfig = viewConfig;
	}

  @Override
  public<B extends V> ViewConfigVariant<V, F, D> provideViewConfig(HFDDVertex v,
      List<HFDDLogTransformStep<B>> vertexLogFilterStack) {
    return this.viewConfig;
  }


}
