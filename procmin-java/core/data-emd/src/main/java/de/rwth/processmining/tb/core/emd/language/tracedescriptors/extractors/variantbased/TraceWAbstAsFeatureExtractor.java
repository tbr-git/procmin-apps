package de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import de.rwth.processmining.tb.core.data.variantlog.abstraction.CCCVariantAbstImpl;
import de.rwth.processmining.tb.core.data.variantlog.abstraction.CVariantAbst;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantDescriptionConstants;
import de.rwth.processmining.tb.core.data.variantlog.base.VariantKeys;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.AbstTraceCC;
import gnu.trove.map.TObjectFloatMap;

public class TraceWAbstAsFeatureExtractor implements VariantBasedFeatureExtractor<CVariantAbst, AbstTraceCC> {

  @Override
  public boolean isProjectionInvariant() {
    return true;
  }

  @Override
  public String getShortDescription() {
		return "CF";
  }

  @Override
  public Multiset<AbstTraceCC> getTraceDescriptor(CVariantAbst variant, CVariantLog<? extends CVariantAbst> log) {
    Multiset<AbstTraceCC> features = HashMultiset.create();
    AbstTraceCC f = new AbstTraceCC(variant, 
				log.getCategoryMapper());
    features.add(f, variant.getSupport());
    return features;
  }

  @Override
  public int addTraceDescriptor(CVariantAbst variant, CVariantLog<? extends CVariantAbst> log,
      Multiset<AbstTraceCC> features) {
    AbstTraceCC f = new AbstTraceCC(variant, 
				log.getCategoryMapper());
    features.add(f, variant.getSupport());
    return variant.getSupport();
  }

  @Override
  public float addTraceDescriptor(CVariantAbst variant, CVariantLog<? extends CVariantAbst> log,
      TObjectFloatMap<AbstTraceCC> features) {
    AbstTraceCC f = new AbstTraceCC(variant, 
				log.getCategoryMapper());
    features.adjustOrPutValue(f, variant.getSupport(), variant.getSupport());
    return variant.getSupport();
  }

  @Override
  public float addTraceDescriptor(CVariantAbst variant, CVariantLog<? extends CVariantAbst> log,
      TObjectFloatMap<AbstTraceCC> features, float increment) {
    AbstTraceCC f = new AbstTraceCC(variant, 
				log.getCategoryMapper());
    features.adjustOrPutValue(f, variant.getSupport() * increment, variant.getSupport() * increment);
    return variant.getSupport() * increment;
  }


  @Override
  public AbstTraceCC getEmptyCVariant(CVariantLog<? extends CVariantAbst> contextLog) {
		return new AbstTraceCC(new CCCVariantAbstImpl(new int[] {}, 1), contextLog.getCategoryMapper());
  }


  @Override
  public VariantKeys getRequiredVariantInfo() {
		return new VariantKeys(VariantDescriptionConstants.ACTIVITY 
				| VariantDescriptionConstants.ABSTRACTIONS);
  }
}
