package de.rwth.processmining.tb.core.emd.dataview.variantbased;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased.VariantBasedFeatureExtractor;

public class FeatureExtractorDistancePairVariant<F extends TraceDescriptor, V extends CVariant, 
    D extends TraceDescDistCalculator<F>> {

  private final VariantBasedFeatureExtractor<V, F> featureExtractor;
  
  private final D distance;
  
  public FeatureExtractorDistancePairVariant(VariantBasedFeatureExtractor<V, F> variantFeatureExtractor,
      D distance) {
    super();
    this.featureExtractor = variantFeatureExtractor;
    this.distance = distance;
  }

  public VariantBasedFeatureExtractor<V, F> getFeatureExtractor() {
    return featureExtractor;
  }

  public D getDistance() {
    return distance;
  }
  
  public String getShortDescription() {
		return featureExtractor.getShortDescription() + " - " + distance.getShortDescription();
	}
  
}
