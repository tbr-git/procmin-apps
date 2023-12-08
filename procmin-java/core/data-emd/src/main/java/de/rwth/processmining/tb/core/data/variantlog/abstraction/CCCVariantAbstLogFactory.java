package de.rwth.processmining.tb.core.data.variantlog.abstraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogImpl;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.data.variantlog.util.CategoricalLogBuildingUtil;
import de.rwth.processmining.tb.core.data.variantlog.util.LogBuildingException;

public class CCCVariantAbstLogFactory extends CVariantLogFactory<CCCVariantAbstImpl> {
	private final static Logger logger = LogManager.getLogger( CCCVariantAbstLogFactory.class );

	public CVariantLogImpl<CCCVariantAbstImpl> build() throws LogBuildingException {
		if(this.log == null) {
			logger.error("No log given! Cannot build variant log!");
			throw new RuntimeException("Log is null");
		}
		if(this.classifier == null) {
			logger.error("No classifier given! Cannot build variant log!");
			throw new RuntimeException("Classifier is null");
		}

		if(this.catMapper == null) {
			// Get activity mappings
			this.catMapper = CategoricalLogBuildingUtil.getActivityMapping(log, classifier);
		}
		
		// Calculate variants	
		ArrayList<CCCVariantAbstImpl> variants = getVariants(log, classifier, this.catMapper);
		
		return new CVariantLogImpl<CCCVariantAbstImpl>(classifier, this.catMapper, variants);
		
	}

	public ArrayList<CCCVariantAbstImpl> getVariants(XLog log, XEventClassifier classifier, CategoryMapper catMapper) {
		// TODO Hashing on Traces will be very slow
		Map<List<String>, Integer> listTraces = CategoricalLogBuildingUtil.getVariantInfoRaw(log, classifier);

		ArrayList<CCCVariantAbstImpl> variants = new ArrayList<>(listTraces.size());
	
		for(Map.Entry<List<String>, Integer> entry : listTraces.entrySet()) {
			int[] categoricalTrace = new int[entry.getKey().size()];
			int i = 0;
			for(String s: entry.getKey()) {
				categoricalTrace[i] = catMapper.getCategory4Activity(s);
				i++;
			}
			variants.add(new CCCVariantAbstImpl(categoricalTrace, entry.getValue()));
		}
		return variants;
	}

}
