package de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.projection.SLDSProjectionCategoryFactory;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection.SLDSFilterVariantContainsAnyCategorySetBuilder;
import de.rwth.processmining.tb.core.data.variantlog.abstraction.CCCVariantAbstImpl;
import de.rwth.processmining.tb.core.data.variantlog.abstraction.CVariantAbst;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.emd.dataview.ViewDataException;
import de.rwth.processmining.tb.core.emd.dataview.ViewIdentifier;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.FeatureExtractorDistancePairVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.ViewConfigVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.controlflow.AdaptiveLVS;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.AbstTraceCC;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased.TraceWAbstAsFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2SimpleNormOrdStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ContextAwareEmptyTraceBalancedTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ScalingContext;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationBase;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationException;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagement;
import de.rwth.processmining.tb.core.sps.algorithm.measure.BiDSDiffMeasure;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFG;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFGBuilder;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFGType;


public class AutoIterationDiagnosticsExtractor {
	private final static Logger logger = LogManager.getLogger( AutoIterationDiagnosticsExtractor.class );
  
  private HFDDIterationManagement hfddItMan; 
 
  private int iteration;
  
  private IVFilterIndepentDiff diffFinder;
  
  private SimilarityBasedCompDiffFinder diffComplementer;
  
  private int nbrMetaDiagnostics;
  
  /**
   * Comparator for difference candidates. 
   * Sorts based on EMD.
   */
  private Comparator<DiffCandidate> diffCandidateComp;
  
  /**
   * Utility function to extract EMD for a candidate
   */
  private Function<DiffCandidate, Double> emdExtract;
  
  /**
   * Constructor
   * 
   * @param hfddItMan Handle iteration management
   * @param iteration Iteration
   * @param nbrMetaDiagnostics Target number of meta diagnostic (set/pictures) to extract
   * @param tMetric Difference Finder: Minimum metric value  (e.g, 0.05 -> 5% frequency difference)
   * @param tDom Difference Finder: Domination factor (dominating if > 0.9 * tDom) 
   * @param tPhi Difference Finder: Upper Phi coefficient threshold of occurrence (if approx 0, indpendent)
   * @param k Target number of complementary differences (knn)
   * @param jaccardUpper Jaccard index of occurrence between considered and potential complementary differences 
   * should not exceed this value
   */
  public AutoIterationDiagnosticsExtractor(HFDDIterationManagement hfddItMan, int iteration, 
      int nbrMetaDiagnostics,
      double tMetric, double tDom, double tPhi,
      int k, float jaccardUpper) {
    this.hfddItMan = hfddItMan;
    this.nbrMetaDiagnostics = nbrMetaDiagnostics;
    this.iteration = iteration;
    
    this.diffFinder = new IVFilterIndepentDiff(hfddItMan, iteration, tMetric, tDom, tPhi);
    this.diffComplementer = new SimilarityBasedCompDiffFinder(hfddItMan, iteration, jaccardUpper, k);
    
    PerspectiveDescriptor pDesc = hfddItMan.getPerspective4Iteration(iteration);
    emdExtract = (DiffCandidate c) -> 
      c.v().getVertexInfo().getMeasurements().get(pDesc).getMetric().get();
    diffCandidateComp = Comparator.comparing(emdExtract).reversed();
  }
  
  public List<MetaDifferenceVisContainer> getDiagnosticDFGs() {
    
    // 1. Group candidates
    List<CandidatesMetaDiagnostic> lMetaDiagnosticSets = this.getDiagnostics();
    
    return this.getDiagnosticDFGs(lMetaDiagnosticSets);
    
  }
  
  public List<MetaDifferenceVisContainer> getDiagnosticDFGs(List<CandidatesMetaDiagnostic> lMetaDiagnosticSets) {
    //////////////////////////////
    // Create ProbDiff DFGs
    //////////////////////////////
    List<MetaDifferenceVisContainer> probDFGs = new LinkedList<>();
    
    int i = 0;
    for (CandidatesMetaDiagnostic candidates : lMetaDiagnosticSets) {
      MetaDifferenceVisContainer vis = this.getPDDFG(candidates, "PDFG" + i);
      if (vis != null) {
        probDFGs.add(vis);
      }
      else {
        logger.error("Could not build visualization for {} (e.g., because difference too small and EMD computation not sound)", 
            Arrays.toString(candidates.diffCandidate().v().getVertexInfo().getItemsetHumanReadable()));
      }
      i++;
    }
    return probDFGs;
  }
  
  public MetaDifferenceVisContainer getPDDFG(CandidatesMetaDiagnostic candidates, String pdfgIdentifier) {
    HFDDIterationBase<CCCVariantAbstImpl> iterationData;
		try {
      iterationData = hfddItMan.getIteration(iteration);
    } catch (HFDDIterationException e) {
      logger.error("Failed due to invalid iteration!");
      return null;
    }

    BiComparisonDataSource<CCCVariantAbstImpl> dataSource = 
        new BiComparisonDataSource<>(iterationData.getPreparedCompDS());
    CategoryMapper cm = hfddItMan.getCategoryMapper();

    ////////////////////////////////////////
    // Variant Selection
    ////////////////////////////////////////
    SLDSFilterVariantContainsAnyCategorySetBuilder<CCCVariantAbstImpl> selectionFilterBuilder = 
      new SLDSFilterVariantContainsAnyCategorySetBuilder<>();
    // Add candidates as selection sets
    selectionFilterBuilder.addActivitySet(candidates.diffCandidate().v().getVertexInfo().getActivities());
    candidates.complDifferences().forEach(c -> selectionFilterBuilder.addActivitySet(c.v().getVertexInfo().getActivities()));
    selectionFilterBuilder
      .setCategoryMapper(cm)
      .setClassifier(dataSource.getClassifier());

    // Apply
    try {
      dataSource.applyTransformation(selectionFilterBuilder);
    } catch (SLDSTransformerBuildingException e) {
      logger.error("Failed to apply the variant selection!");
      e.printStackTrace();
      return null;
    }

    ////////////////////////////////////////
    // Projection
    ////////////////////////////////////////
    SLDSProjectionCategoryFactory<CCCVariantAbstImpl> projBuilder = new SLDSProjectionCategoryFactory<>();
    
    // Activities to project on:
    // UNION of a candidate sets 
    // -> Consider for projection even if the set is not completely contained in trace 
    BitSet activitiesProj = new BitSet(cm.getMaxCategoryCode());
    activitiesProj.or(candidates.diffCandidate().v().getVertexInfo().getActivities());
    candidates.complDifferences().forEach(c -> activitiesProj.or(c.v().getVertexInfo().getActivities()));

    projBuilder.setActivities(activitiesProj)
      .setCategoryMapper(cm)
      .setClassifier(dataSource.getClassifier());
    
    try {
      dataSource.applyTransformation(projBuilder);
    } catch (SLDSTransformerBuildingException e) {
      logger.error("Failed to project!");
      return null;
    }
    // Caching
    dataSource.ensureCaching();

    ////////////////////////////////////////
    // EMD Computation
    ////////////////////////////////////////
		////////////////////
		// View Config 
		////////////////////
		// Trace descriptor + distance
		FeatureExtractorDistancePairVariant<AbstTraceCC, CVariantAbst, AdaptiveLVS> desDistPair = 
		    new FeatureExtractorDistancePairVariant<AbstTraceCC, CVariantAbst, AdaptiveLVS>(
		        new TraceWAbstAsFeatureExtractor(), new AdaptiveLVS());
		//////////
		// Total
		//////////
		ContextAwareEmptyTraceBalancedTransformer langTransformerTotal;
		try {
      langTransformerTotal = new ContextAwareEmptyTraceBalancedTransformer(
          dataSource.getDataSourceLeftBase().getVariantLog().sizeLog(), 
      		dataSource.getDataSourceRightBase().getVariantLog().sizeLog(), ScalingContext.GLOBAL);
    } catch (SLDSTransformationError e) {
      e.printStackTrace();
      return null;
    }

		// Distance + Trace descriptor factory + transformer => view
		ViewConfigVariant<CVariantAbst, AbstTraceCC, AdaptiveLVS> viewConfigTotal = 
		    new ViewConfigVariant<>(langTransformerTotal, desDistPair,
				new ViewIdentifier(desDistPair.getShortDescription() + " - " + langTransformerTotal.getShortDescription()));

		//////////
		// Normalized
		//////////
    Window2OrderedStochLangTransformer langTransformerNormalized = new Window2SimpleNormOrdStochLangTransformer();
    // Distance + Trace descriptor factory + transformer => view
    ViewConfigVariant<CVariantAbst, AbstTraceCC, AdaptiveLVS> viewConfigNormalized = new ViewConfigVariant<>(
        langTransformerNormalized, desDistPair, new ViewIdentifier(
            desDistPair.getShortDescription() + " - " + langTransformerNormalized.getShortDescription()));
		
		////////////////////
		// EMD
		////////////////////
		//////////
    // Total
		//////////
		PerspectiveDescriptor pDescTotal = new PerspectiveMetaPDFG(iteration, pdfgIdentifier + "-Total");
		Optional<EMDSolContainer<AbstTraceCC>> emdSolTotal;
		try {
			emdSolTotal = BiDSDiffMeasure.measureEMD(dataSource, viewConfigTotal, pDescTotal);
		} catch (ViewDataException e) {
		  logger.error("Error during EMD computation");
		  return null;
		}
		
		if (emdSolTotal.isEmpty()) {
		  logger.error("Error during EMD computation");
		  return null;
		}
		
		//////////
    // Normalized
		//////////
		PerspectiveDescriptor pDescNormalized = new PerspectiveMetaPDFG(iteration, pdfgIdentifier + "-Normalized");
		Optional<EMDSolContainer<AbstTraceCC>> emdSolNormalized;
		try {
			emdSolNormalized = BiDSDiffMeasure.measureEMD(dataSource, viewConfigNormalized, pDescNormalized);
		} catch (ViewDataException e) {
		  logger.error("Error during EMD computation");
		  return null;
		}
		
		if (emdSolNormalized.isEmpty()) {
		  logger.error("Error in EMD realization for normalized view.");
		  return null;
		}
		
		////////////////////
		// Build PDFG
		////////////////////
    double coveredProbLeft = 1;
		double coveredProbRight = 1;
		try {
      coveredProbLeft = ((double) dataSource.getDataSourceLeft().getVariantLog().sizeLog())
          / langTransformerTotal.getContextLogSizeLeft();
      coveredProbRight = ((double) dataSource.getDataSourceRight().getVariantLog().sizeLog())
          / langTransformerTotal.getContextLogSizeRight();
    } catch (SLDSTransformationError e) {
      e.printStackTrace();
    }
		ProbDiffDFGBuilder pDFGBuilder = new ProbDiffDFGBuilder(cm);
    ProbDiffDFG pDFGTotal =  pDFGBuilder.buildProbDiffDFG(emdSolTotal.get(), coveredProbLeft, coveredProbRight, 
        ProbDiffDFGType.GLOBAL);
    // TODO Make class re-use safe!
    // Don't re-use
		pDFGBuilder = new ProbDiffDFGBuilder(cm);
    ProbDiffDFG pDFGNormalized =  pDFGBuilder.buildProbDiffDFG(emdSolNormalized.get(), coveredProbLeft, coveredProbRight, 
        ProbDiffDFGType.NORMALIZED);
		return new MetaDifferenceVisContainer(pDFGTotal, pDFGNormalized);		
  }

  /**
   * Automatically aggregate vertex-related diagnostics into larger sets. Each
   * returned collection corresponds to a set of vertices that are supposed to
   * together yield a good diagnostic (e.g., when visualized).
   * 
   * @return List (most relevant first) of vertex collections where each
   *         collection captures coherent log differences. 
   */
  public List<CandidatesMetaDiagnostic> getDiagnostics() {
    logger.info("Extracting interesting vertices...");
    // Find interesting difference candidates
    Collection<DiffCandidate> candidates = this.diffFinder.findInterestingVertices();
    // Sort by EMD value
    List<DiffCandidate> candidatesSorted = new LinkedList<>(candidates);
    candidatesSorted.sort(diffCandidateComp);
    // Clear
    candidates = null;
    logger.info("Extracted {} interesting vertices with EMD {} - {}", 
        candidatesSorted.size(), 
        emdExtract.apply(candidatesSorted.getLast()), emdExtract.apply(candidatesSorted.getFirst()));

    List<CandidatesMetaDiagnostic> metaDiagnosticSets = new LinkedList<>();
    Set<DiffCandidate> coveredCandidates = new HashSet<>();
    
    Iterator<DiffCandidate> candidateIterator = candidatesSorted.iterator();
    
    logger.info("Merging interesting vertices to meta sets...");
    // Iterate until
    // - there is no more candidate
    // - we found enough meta diagnostic sets
    while (candidateIterator.hasNext() && (metaDiagnosticSets.size() < this.nbrMetaDiagnostics)) {
      DiffCandidate c = candidateIterator.next();
      
      if (coveredCandidates.contains(c) ) {
        continue;
      }
      
      // Find complementary differences
      Collection<DiffCandidate> complCandidates = 
          this.diffComplementer.findComplementaryDifferences(candidatesSorted, c);
      
      // Add meta diagnostic
      // Don't forget to add c!
      metaDiagnosticSets.add(new CandidatesMetaDiagnostic(c, complCandidates));
      // Add to covered sets
      complCandidates.forEach(c2 -> coveredCandidates.add(c2));
    }
    
    logger.info("Done merging interesting vertices to meta sets");
    return metaDiagnosticSets;
  }

}
