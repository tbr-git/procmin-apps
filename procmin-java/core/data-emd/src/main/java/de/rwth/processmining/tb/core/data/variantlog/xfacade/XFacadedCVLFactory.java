package de.rwth.processmining.tb.core.data.variantlog.xfacade;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeMapImpl;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;

public class XFacadedCVLFactory {
  
  public static XLog createXFacade4VariantLog(CVariantLog<? extends CVariant> cVarLog, String logName) {
    
    // Create empty log
    XFactory factory = XFactoryRegistry.instance().currentDefault();
    XConceptExtension conceptInstance = XConceptExtension.instance();

    XLog log = factory.createLog();;
    if (log != null) {
      conceptInstance.assignName(log, logName);
    }
    
    // Create Prototypes
    XEventCPrototype[] prototypes = createPrototypeEvents(cVarLog.getCategoryMapper());

    // Add facaded traces
    int index = 0;
    for (CVariant v : cVarLog) {
      // Use index as id
      log.add(new XTraceFacadeCVariant(new XAttributeMapImpl(), v, index, prototypes));
			//conceptInstance.assignName(currentTrace, name);
      index++;
    }
    return log;
  }
  
  public static XEventCPrototype[] createPrototypeEvents(CategoryMapper cm) {
    
    XEventCPrototype[] prototypes = new XEventCPrototype[cm.getCategoryCount()];
    
    for (int c = 0; c < cm.getCategoryCount(); c++) {
      prototypes[c] = XEventCPrototype.createXEventCPrototype(c, 
          cm.getActivity4Category(c));
    }
    
    return prototypes;
    
  }

}
