package de.rwth.processmining.tb.core.emd.solver;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.OrderedStochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased.VariantBasedFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.xlogbased.XLogTraceFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;
import de.rwth.processmining.tb.core.emd.solver.akp.core.AKPFactoryArraySimple;
import de.rwth.processmining.tb.core.emd.solver.akp.core.AKPSolverArraySimple;

public class EMDSolver {

	/**
	 * 
	 * @param xlog Sorted event log
	 * @param traceIndex
	 * @param w_size Window size
	 */
	public static<F extends TraceDescriptor> EMDSolContainer<F> getLPSolution(
	    Iterator<XTrace> itL, Iterator<XTrace> itR,  
			XLogTraceFeatureExtractor<F> langFac, TraceDescDistCalculator<F> trDescDist, 
			Window2OrderedStochLangTransformer langTransformer) {
		
		Pair<OrderedStochasticLanguage<F>, OrderedStochasticLanguage<F>> langs = langTransformer.transformWindow(itL, itR, langFac);

		EMDSolContainer.Builder<F> emdSolBuilder = new EMDSolContainer.Builder<>();
		emdSolBuilder.addLangLeft(langs.getLeft()).addLangRight(langs.getRight());

		AKPFactoryArraySimple fac = new AKPFactoryArraySimple(trDescDist);
		fac.setupNewSolver(langs.getLeft(), langs.getRight(), emdSolBuilder);
		AKPSolverArraySimple solver = fac.getSolver();
		double emd = solver.solve();

		return  emdSolBuilder.addEMD(emd).addNonZeroFlows(solver.getNonZeroFlows()).build();
	}

	/**
	 * Compute EMD between two logs provided as variants.
	 * Comprises the following steps:
	 * <p><ul>
	 * <li> Extract Features
	 * <li> Transform feature multiset/map into stochastic language (potentially additional scaling)
	 * <li> Create EMD Solver (compute distances)
	 * <li> Compute EMD
	 * </ul>
	 * 
	 * @param <V> Required "minimum" variant type
	 * @param <F> Extracted feature
	 * @param variantLogLeft Left log (left signature in EMD)
	 * @param variantLogRight Right log (right signature in EMD)
	 * @param featureExtractor Feature extractor
	 * @param trDescDist Feature distance for pairwise feature distances
	 * @param langTransformer Transforms a multiset/map of features into stochastic language
	 * @return Container containing EMD and additional diagnostics
	 */
	public static<V extends CVariant, F extends TraceDescriptor> EMDSolContainer<F> getLPSolution(
	    CVariantLog<? extends V> variantLogLeft, 
			CVariantLog<? extends V> variantLogRight, VariantBasedFeatureExtractor<V, F> featureExtractor, 
			TraceDescDistCalculator<F> trDescDist, Window2OrderedStochLangTransformer langTransformer) {
		
	  // Extract features and transform into stocastic language
		Pair<OrderedStochasticLanguage<F>, OrderedStochasticLanguage<F>> langs = 
		    langTransformer.transformWindow(variantLogLeft, variantLogRight, featureExtractor);

		// Instantiate solution container
		EMDSolContainer.Builder<F> emdSolBuilder = new EMDSolContainer.Builder<>();
		emdSolBuilder.addLangLeft(langs.getLeft()).addLangRight(langs.getRight());

		// Instantiate solver
		AKPFactoryArraySimple<F, TraceDescDistCalculator<F>> fac = new AKPFactoryArraySimple<>(trDescDist);
		fac.setupNewSolver(langs.getLeft(), langs.getRight(), emdSolBuilder);
		AKPSolverArraySimple solver = fac.getSolver();
		// EMD
		double emd = solver.solve();

		return  emdSolBuilder.addEMD(emd).addNonZeroFlows(solver.getNonZeroFlows()).build();
	}

}
