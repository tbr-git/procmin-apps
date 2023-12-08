package hfdd.evaluation.cdrift.evaluators;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagement;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuilderTimeout;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuildingException;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.DiffCandidate;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.IVFilterIndepentDiff;
import de.rwth.processmining.tb.core.util.stopwatch.PerformanceLogger;
import de.rwth.processmining.tb.core.util.stopwatch.SpsBuildMilestones;
import hfdd.evaluation.cdrift.PVAOnCDSPSTestResult;
import hfdd.evaluation.cdrift.taskcreation.PVAOnCDTestSpec;
import hfdd.evaluation.util.PropertiesLoader;

public class SPSEvaluator implements PVAOnCDEvaluator {
	private final static Logger logger = LogManager.getLogger( PVAOnCDEvaluator.class );
  
  //////////////////////////////
  // Logging
  //////////////////////////////
  public static final int NBR_TOP_SCORES = 5; 

  /**
   * Writer to incrementally write runs
   */
  private BufferedWriter writer;

  /**
   * Path (csv-file) to write the results to 
   */
  private final Path pathResult;

  ////////////////////////////////////////////////////////////
  // SPS Graph-related Parameters
  ////////////////////////////////////////////////////////////
  /**
   * Event classifier;
   */
  private final XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
  
  /**
   * Target number of vertices
   */
  private int targetNumberItemsets = 10_000;

  //////////////////////////////
  // Interesting Vertex Finder
  //////////////////////////////
  private double tMetric = 0.005;
  private double tDom = 0.95;
  private double tPhi = 0.05;

  public static SPSEvaluator initFromProperties(Properties spsProp, Path pathResult) {
    final SPSEvaluator spsEvaluator = new SPSEvaluator(pathResult);

    //////////////////////////////
    // Process Properties 
    //////////////////////////////
    // Nbr Vertices
    Optional<Integer> propNbrVertices = PropertiesLoader.parsePropertyInt(spsProp, "targetNumberItemsets");
    propNbrVertices.ifPresentOrElse(v -> spsEvaluator.targetNumberItemsets = v, 
        () -> logger.warn("Property {} not set properly, will use default value {}", 
            "targetNumberItemsets", spsEvaluator.targetNumberItemsets));

    // tDom
    Optional<Double> propTDom = PropertiesLoader.parsePropertyDouble(spsProp, "tDom");
    propTDom.ifPresentOrElse(v -> spsEvaluator.tDom = v, 
        () -> logger.warn("SPS-property {} not set properly, will use default value {}", 
            "tDom", spsEvaluator.tDom));
    // tMetric
    Optional<Double> propTMetric = PropertiesLoader.parsePropertyDouble(spsProp, "tMetric");
    propTMetric.ifPresentOrElse(v -> spsEvaluator.tMetric = v, 
        () -> logger.warn("SPS-property {} not set properly, will use default value {}", 
            "tMetric", spsEvaluator.tMetric));
    // tPhi
    Optional<Double> propTPhi = PropertiesLoader.parsePropertyDouble(spsProp, "tPhi");
    propTPhi.ifPresentOrElse(v -> spsEvaluator.tPhi = v, 
        () -> logger.warn("SPS-property {} not set properly, will use default value {}", 
            "tPhi", spsEvaluator.tPhi));

    return spsEvaluator;
  }
  
  public SPSEvaluator(Path pathResult) {
    this.pathResult = pathResult;
  }

  @Override
  public boolean init() {
    try {
      writer = new BufferedWriter(new FileWriter(pathResult.toFile()));
      // Write Result Header
      writer.write(PVAOnCDSPSTestResult.getCSVHeader(NBR_TOP_SCORES));
      writer.write("\n");
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public boolean evaluateOnTask(PVAOnCDTestSpec testSpec) {
    // Run evaluation
    PVAOnCDSPSTestResult testResult;
    try {
      testResult = this.evalHFDD(testSpec);
    } catch (HFDDIterationManagementBuildingException e) {
      return false;
    }
    
    // Write evaluation result line
    try {
      writer.write(testResult.toCSVString(NBR_TOP_SCORES));
      writer.write("\n");
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

  public PVAOnCDSPSTestResult evalHFDD(PVAOnCDTestSpec testSpec) throws HFDDIterationManagementBuildingException {
    ////////////////////////////////////////
    // All Parameters
    ////////////////////////////////////////

    ////////////////////////////////////////
    // Build HFDD Iteration Management
    ////////////////////////////////////////
		HFDDIterationManagementBuilderTimeout builder = new HFDDIterationManagementBuilderTimeout();
		builder.setTargetItemsetNumber(targetNumberItemsets);
		builder.setClassifier(eventClassifier).setXlogL(testSpec.logL()).setXlogR(testSpec.logR());
		HFDDIterationManagement hfddItMan = builder.build();

    ////////////////////////////////////////
    // Difference Discovery
    ////////////////////////////////////////
		long startVertexFinder = System.currentTimeMillis();
    IVFilterIndepentDiff finderDefault = new IVFilterIndepentDiff(hfddItMan, 0, 
        tMetric, tDom, tPhi);
		long timeVertexFinder = System.currentTimeMillis() - startVertexFinder;
    
    final Collection<DiffCandidate> candidatesDefault = finderDefault.findInterestingVertices();
    
    ////////////////////////////////////////
    // Result Evaluation
    ////////////////////////////////////////
    PerspectiveDescriptor pDesc = hfddItMan.getPerspective4Iteration(0);
    ToDoubleFunction<DiffCandidate> emdExtract = 
        c -> c.v().getVertexInfo().getMeasurements().get(pDesc).getMetric().get(); 
    Comparator<Double> sortByEMD = Comparator.<Double>naturalOrder().reversed();
    
    final List<Double> resultEMDSorted = candidatesDefault
        .stream()
        .mapToDouble(emdExtract)
        .boxed()
        .sorted(sortByEMD)
        .limit(NBR_TOP_SCORES)
        .toList();
    PerformanceLogger<SpsBuildMilestones> perfLogger = builder.getPerformanceLogger();
    return new PVAOnCDSPSTestResult(testSpec.collectionName(), testSpec.identifierOrigLog(), testSpec.idExtraction(),
        testSpec.logType(), testSpec.previousRelatedCD(), testSpec.nextRelatedCD(),
        "hfdd", 
        perfLogger.getMeasurements().get(SpsBuildMilestones.LOAD_DATA).getTime(),
        perfLogger.getMeasurements().get(SpsBuildMilestones.CREATE_SPS_GRAPH).getTime(),
        perfLogger.getMeasurements().get(SpsBuildMilestones.BASE_MEASUREMENT).getTime(),
        timeVertexFinder,
        testSpec.driftInducedDifference(),
        resultEMDSorted);
  }

  @Override
  public String getNameUnique() {
    return "SPS";
  }
}
