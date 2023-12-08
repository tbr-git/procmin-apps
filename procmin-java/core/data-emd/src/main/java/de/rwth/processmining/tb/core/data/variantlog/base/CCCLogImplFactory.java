package de.rwth.processmining.tb.core.data.variantlog.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.data.variantlog.util.CategoricalLogBuildingUtil;
import de.rwth.processmining.tb.core.data.variantlog.util.LogBuildingException;

public class CCCLogImplFactory extends CVariantLogFactory<CCCVariantImpl> {
	private final static Logger logger = LogManager.getLogger( CCCLogImplFactory.class );
	
	public CVariantLog<CCCVariantImpl> build() throws LogBuildingException {
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

		// Compute variants	
		ArrayList<CCCVariantImpl> variants = getVariants(log, classifier, catMapper);
		
		return new CVariantLogImpl<CCCVariantImpl>(classifier, catMapper, variants);
		
	}

	public ArrayList<CCCVariantImpl> getVariants(XLog log, XEventClassifier classifier, CategoryMapper catMapper) throws LogBuildingException {
		Map<List<String>, Integer> listTraces = CategoricalLogBuildingUtil.getVariantInfoRaw(log, classifier);

		ArrayList<CCCVariantImpl> variants = new ArrayList<>(listTraces.size());
	
		Integer idTmp = null;
		for(Map.Entry<List<String>, Integer> entry : listTraces.entrySet()) {
			int[] categoricalTrace = new int[entry.getKey().size()];
			int i = 0;
			for(String s: entry.getKey()) {
				idTmp = catMapper.getCategory4Activity(s);
				if(idTmp == null) {
					throw new LogBuildingException("Activity-to-id mapping does not contain a category for " + s + "!");
				}
				categoricalTrace[i] = idTmp;
				i++;
			}
			variants.add(new CCCVariantImpl(categoricalTrace, entry.getValue()));
		}
		return variants;
	}
	
	public static CCCVariantImpl transformTraceToVariant(XTrace trace, 
        XEventClassifier classifier, 
        CategoryMapper catMapper) throws LogBuildingException {
	  
			int[] categoricalTrace = new int[trace.size()];
			int i = 0;
			Integer idTmp;
			for(XEvent e: trace) {
			  
			  String s = classifier.getClassIdentity(e);
				idTmp = catMapper.getCategory4Activity(s);
				if(idTmp == null) {
					throw new LogBuildingException("Activity-to-id mapping does not contain a category for " + s + "!");
				}
				categoricalTrace[i] = idTmp;
				i++;
			}
			return new CCCVariantImpl(categoricalTrace, 1);
	}

}
