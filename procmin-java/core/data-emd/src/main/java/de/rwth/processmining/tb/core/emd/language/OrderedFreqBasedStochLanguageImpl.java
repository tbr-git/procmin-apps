package de.rwth.processmining.tb.core.emd.language;

import java.util.ArrayList;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.map.TObjectFloatMap;

public class OrderedFreqBasedStochLanguageImpl<F extends TraceDescriptor> extends StochasticLanguageImpl<F> 
    implements OrderedStochasticLanguage<F> {
	/**
	 */
	protected final ArrayList<F> variantsOrder;
	
	public OrderedFreqBasedStochLanguageImpl(TObjectFloatMap<F> sLog, double totalWeight, int aboluteNbrTraces) {
		super(sLog, totalWeight, aboluteNbrTraces);
		variantsOrder = new ArrayList<>(sLog.size());
		TObjectFloatIterator<F> it = sLog.iterator();
		while(it.hasNext()) {
			it.advance();
			variantsOrder.add(it.key());
		}
	}
	
	public F get(int index) {
		return variantsOrder.get(index);
	}
	
	@Override
	public StochasticLanguageIterator<F> iterator() {
		double normalizationWeight = this.getTotalWeight();
		return new StochasticLanguageIterator<F>() {
			
			private int i = -1;

			public F next() {
				i++;
				return variantsOrder.get(i);
			}

			public boolean hasNext() {
				return i < variantsOrder.size() - 1;
			}

			public double getProbability() {
				return sLog.get(variantsOrder.get(i)) / normalizationWeight;
			}
		};
	}
}
