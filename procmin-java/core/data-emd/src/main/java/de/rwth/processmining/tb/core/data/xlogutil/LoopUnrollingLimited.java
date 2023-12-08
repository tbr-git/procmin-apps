package de.rwth.processmining.tb.core.data.xlogutil;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

import gnu.trove.map.hash.TObjectIntHashMap;

public class LoopUnrollingLimited implements TraceTransformerInplace {

	/**
	 * Maximum number of iterations that are unrolled.
	 * every Iteration >= max Unroll will be extended by "LMax".  
	 */
	private final int maxUnroll;

	/**
	 * 
	 * @param maxUnroll Maximum number of repetitions that are renamed.
	 */
	public LoopUnrollingLimited(int maxUnroll) {
		this.maxUnroll = maxUnroll;		
	}

	@Override
	public void transform(XTrace t, XEventClassifier classifier) {
		TObjectIntHashMap<String> mapActCount = new TObjectIntHashMap<>(50);
		
		for(XEvent e: t) {
			int reoccurence = mapActCount.adjustOrPutValue(classifier.getClassIdentity(e), 1, 0);
			if(reoccurence > 0) {
				XAttribute attr = e.getAttributes().get(XConceptExtension.KEY_NAME);
				String oldName = ((XAttributeLiteral) attr).getValue();
				String newName;
				if (reoccurence < maxUnroll) {
					newName = oldName + " " + reoccurence;
				}
				else {
					newName = oldName + " LMax";
				}
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
