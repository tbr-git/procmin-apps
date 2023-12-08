package hfdd.evaluation.cdrift.evaluators.plainemd;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.SLDSPipelineBuildingUtil;
import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.base.CCCLogImplFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.CCCVariantImpl;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.util.LogBuildingException;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptionLog;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptionLog.LogType;
import de.rwth.processmining.tb.core.emd.dataview.ViewDataException;
import de.rwth.processmining.tb.core.emd.dataview.ViewIdentifier;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.FeatureExtractorDistancePairVariant;
import de.rwth.processmining.tb.core.emd.dataview.variantbased.ViewConfigVariant;
import de.rwth.processmining.tb.core.emd.grounddistances.controlflow.LevenshteinCCStateful;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.extractors.variantbased.TraceAsFeatureExtractor;
import de.rwth.processmining.tb.core.emd.language.transformer.Window2OrderedStochLangTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ContextAwareEmptyTraceBalancedTransformer;
import de.rwth.processmining.tb.core.emd.language.transformer.contextaware.ScalingContext;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;
import de.rwth.processmining.tb.core.sps.algorithm.measure.BiDSDiffMeasure;
import hfdd.evaluation.cdrift.evaluators.PVAOnCDEvaluator;
import hfdd.evaluation.cdrift.evaluators.bolt.BoltEvaluator;
import hfdd.evaluation.cdrift.taskcreation.PVAOnCDTestSpec;

public class PlainEMDEvaluator implements PVAOnCDEvaluator {

  private final static Logger logger = LogManager.getLogger(BoltEvaluator.class);

  /**
   * Writer to incrementally write runs
   */
  private BufferedWriter writer;

  /**
   * Path (csv-file) to write the results to
   */
  private final Path pathResult;

  /**
   * Event classifier that is used.
   */
  public static XEventClassifier classifier = XLogInfoImpl.NAME_CLASSIFIER;

  /**
   * Factory method
   * 
   * @param pathRestults Path (File) to write the results to
   * @return
   */
  public static PlainEMDEvaluator init(Path pathRestults) {
    return new PlainEMDEvaluator(pathRestults);
  }

  public PlainEMDEvaluator(Path pathResult) {
    this.pathResult = pathResult;
  }

  @Override
  public boolean init() {
    try {
      writer = new BufferedWriter(new FileWriter(pathResult.toFile()));
      // Write Result Header
      writer.write(PVAOnCDTestResultPlainEMD.getCSVHeader());
      writer.write("\n");
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public boolean evaluateOnTask(PVAOnCDTestSpec testSpec) throws Exception {
    
    Optional<Pair<Double, Long>> res;
    res = calculateEMDBetweenLogsWithTime(
        testSpec.logL(), testSpec.logR(), classifier);

    ////////////////////
    // Build Result Data Structure
    ////////////////////
    if (res.isEmpty()) {
      return false;
    }
    PVAOnCDTestResultPlainEMD testResult = new PVAOnCDTestResultPlainEMD(testSpec.collectionName(),
        testSpec.identifierOrigLog(), testSpec.idExtraction(), testSpec.logType(), testSpec.previousRelatedCD(),
        testSpec.nextRelatedCD(), "plainEMD", 
        res.get().getRight(),
        testSpec.driftInducedDifference(), 
        res.get().getLeft());

    return writeResultFile(testResult.toCSVString() + "\n");
  }

  private synchronized boolean writeResultFile(String content) {
    // Write evaluation result line
    try {
      writer.append(content);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean complete() {
    try {
      writer.close();
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  @Override
  public String getNameUnique() {
    return "PLAIN_EMD";
  }

  public static Optional<Pair<Double, Long>> calculateEMDBetweenLogsWithTime(XLog xlogL, XLog xlogR, XEventClassifier classifier)
      throws ViewDataException, SLDSTransformationError, SLDSTransformerBuildingException, LogBuildingException {
    // Create Data Source
    CVariantLogFactory<CCCVariantImpl> variantLogFactory = new CCCLogImplFactory();
    variantLogFactory.setClassifier(classifier);
    List<StochasticLanguageDataSource<CCCVariantImpl>> lDataSources = null;
    lDataSources = SLDSPipelineBuildingUtil.buildPipelineFrom2XLogs(xlogL, xlogR, variantLogFactory);
    BiComparisonDataSource<CCCVariantImpl> biCompDS = new BiComparisonDataSource<CCCVariantImpl>(lDataSources.get(0),
        lDataSources.get(1));

    //////////////////////////////
    // View
    //////////////////////////////
    // Language transformer
    Window2OrderedStochLangTransformer langTransformer = null;
    langTransformer = new ContextAwareEmptyTraceBalancedTransformer(
        biCompDS.getDataSourceLeft().getVariantLog().sizeLog(), biCompDS.getDataSourceRight().getVariantLog().sizeLog(),
        ScalingContext.GLOBAL);

    // Trace descriptor + distance
    FeatureExtractorDistancePairVariant<BasicTraceCC, CVariant, LevenshteinCCStateful> desDistPair = new FeatureExtractorDistancePairVariant<BasicTraceCC, CVariant, LevenshteinCCStateful>(
        new TraceAsFeatureExtractor(), new LevenshteinCCStateful());

    ViewConfigVariant<CVariant, BasicTraceCC, LevenshteinCCStateful> viewConfig = new ViewConfigVariant<>(
        langTransformer, desDistPair,
        new ViewIdentifier(desDistPair.getShortDescription() + " - " + langTransformer.getShortDescription()));

    // Perspective is not really need
    long startTime = System.currentTimeMillis();
    Optional<EMDSolContainer<BasicTraceCC>> emdSol = BiDSDiffMeasure.measureEMD(biCompDS, viewConfig,
        new PerspectiveDescriptionLog(LogType.CONTEXT));
    long time = System.currentTimeMillis() - startTime;

    if (emdSol.isPresent()) {
      return Optional.of(Pair.of(emdSol.get().getEMD(), time));
    } else {
      return Optional.empty();
    }
  }
}
