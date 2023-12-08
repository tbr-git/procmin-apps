package de.rwth.processmining.tb.core.data.xlogutil;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Loop unrolling transformer that unrolls loops in-place within traces.
 * 
 * Within a trace, repeated occurrences of the same activity will be append with their number of occurrence. 
 * @author brockhoff
 *
 */
public class LoopUnrolling implements TraceTransformerInplace {

	@Override
	public void transform(XTrace t, XEventClassifier classifier) {
		TObjectIntHashMap<String> mapActCount = new TObjectIntHashMap<>(50);
		
		for(XEvent e: t) {
			int reoccurence = mapActCount.adjustOrPutValue(classifier.getClassIdentity(e), 1, 0);
			if(reoccurence > 0) {
				XAttribute attr = e.getAttributes().get(XConceptExtension.KEY_NAME);
				String oldName = ((XAttributeLiteral) attr).getValue();
				String newName = oldName + " " + reoccurence;
				XAttributeLiteralImpl updatedConceptName = 
						new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, newName, attr.getExtension());
				e.getAttributes().put(XConceptExtension.KEY_NAME, updatedConceptName);
			}
			
		}
	}

	@Override
	public String getDescription() {
		return "Loop unrolling";
	}

}
