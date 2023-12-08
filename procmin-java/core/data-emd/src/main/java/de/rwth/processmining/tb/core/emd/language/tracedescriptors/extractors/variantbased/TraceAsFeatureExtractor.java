package de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import de.rwth.processmining.tb.core.data.variantlog.base.CCCVariantImpl;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantDescriptionConstants;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantKeys;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;
import gnu.trove.map.TObjectFloatMap;

public class TraceAsFeatureExtractor implements VariantBasedFeatureExtractor<CVariant, BasicTraceCC> {

  @Override
  public boolean isProjectionInvariant() {
    return true;
  }

  @Override
  public String getShortDescription() {
		return "CF";
  }

  @Override
  public Multiset<BasicTraceCC> getTraceDescriptor(CVariant variant, CVariantLog<? extends CVariant> log) {
    Multiset<BasicTraceCC> features = HashMultiset.create();
    BasicTraceCC f = new BasicTraceCC(variant, log.getCategoryMapper());
    features.add(f, variant.getSupport());
    return features;
  }

  public static BasicTraceCC getTraceDescriptor(CVariant variant, CategoryMapper catMapper) {
    BasicTraceCC f = new BasicTraceCC(variant, catMapper);
    return f;
  }

  @Override
  public int addTraceDescriptor(CVariant variant, CVariantLog<? extends CVariant> log,
      Multiset<BasicTraceCC> features) {
    BasicTraceCC f = new BasicTraceCC(variant, log.getCategoryMapper());
    features.add(f, variant.getSupport());
    return variant.getSupport();
  }

  @Override
  public float addTraceDescriptor(CVariant variant, CVariantLog<? extends CVariant> log,
      TObjectFloatMap<BasicTraceCC> features) {
    BasicTraceCC f = new BasicTraceCC(variant, log.getCategoryMapper());
    features.adjustOrPutValue(f, variant.getSupport(), variant.getSupport());
    return variant.getSupport();
  }

 @Override
  public float addTraceDescriptor(CVariant variant, CVariantLog<? extends CVariant> log,
      TObjectFloatMap<BasicTraceCC> features, float increment) {
    BasicTraceCC f = new BasicTraceCC(variant, log.getCategoryMapper());
    features.adjustOrPutValue(f, variant.getSupport() * increment, variant.getSupport() * increment);
    return variant.getSupport() * increment;
  }

  @Override
  public BasicTraceCC getEmptyCVariant(CVariantLog<? extends CVariant> contextLog) {
		return new BasicTraceCC(new CCCVariantImpl(new int[] {}, 1), contextLog.getCategoryMapper());
  }


  @Override
  public VariantKeys getRequiredVariantInfo() {
		return new VariantKeys(VariantDescriptionConstants.ACTIVITY);
  }

 

}
