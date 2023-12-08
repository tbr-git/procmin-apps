package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.StochasticLangDataSourceTransformer;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.contextaware.CVariantCatContSet;

/**
 * Group variants by the activity sequence and aggregate the condition sets. 
 * 
 * @author brockhoff
 *
 * @param <E>
 */
public class SLDSVariantCondSetAgg<E extends CVariantCatContSet> extends 
		StochasticLangDataSourceTransformer<E>  {

	/**
	 * Type of the performed aggregation. 
	 */
	private final SLDSCondSetAggType aggType;
	
	public SLDSVariantCondSetAgg(StochasticLanguageDataSource<E> stochLangDataSource, 
			SLDSCondSetAggType aggType) {
		super(stochLangDataSource);
		this.aggType = aggType;
	}

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		throw new RuntimeException("Condition set aggregation not implemented for XLog: "
				+ "Cannot aggregated XLogs");
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		CVariantLog<E> log = super.getVariantLog();

		// We cannot directly use the variant array as key
		// Even though the bitset is final, it is not immutable
		// -> We will carefully abuse that. 
		Map<CVariant, E> variant2CondSet = new HashMap<>();
		
		for (E v : log) {
			// Initialize
			if (! variant2CondSet.containsKey(v.behaveAsCFVariant())) {
				@SuppressWarnings("unchecked")
				E vCopy = (E) v.copyVariant();
				variant2CondSet.put(vCopy.behaveAsCFVariant(), vCopy);
			}
			else {
				E vSet = variant2CondSet.get(v.behaveAsCFVariant());
				// Aggregate
				switch (this.aggType) {
					case INTERSECTION:
						vSet.getCatContext().and(v.getCatContext());
						vSet.setSupport(vSet.getSupport() + v.getSupport());
						break;
					case UNION:
						vSet.getCatContext().or(v.getCatContext());
						vSet.setSupport(vSet.getSupport() + v.getSupport());
						break;
					default:
						throw new IllegalArgumentException("This aggregation type " 
							+ this.aggType + " cannot be handled");
				}
			}
		}
	
		// Collect new variants
		ArrayList<E> newVariants = variant2CondSet.entrySet().stream()
			.map(t -> t.getValue())
			.collect(Collectors.toCollection(ArrayList::new));

		return CVariantLogFactory.buildFrom(log, newVariants);
	}
}
