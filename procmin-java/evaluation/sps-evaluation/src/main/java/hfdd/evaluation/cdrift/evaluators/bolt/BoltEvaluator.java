package hfdd.evaluation.cdrift.evaluators.bolt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.transitionsystem.miner.TSMinerInput;
import org.processmining.plugins.transitionsystem.miner.modir.TSMinerModirInput;
import org.processmining.plugins.transitionsystem.miner.util.TSAbstractions;
import org.processmining.plugins.transitionsystem.miner.util.TSDirections;
import org.processmining.processcomparator.algorithms.DrawUtils;
import org.processmining.processcomparator.algorithms.TransitionSystemUtils;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.model.ResultsObject;
import org.processmining.processcomparator.model.SettingsObject;
import org.processmining.processcomparator.tbutils.MetricExtractor;
import org.processmining.processcomparator.tbutils.dummycontext.DummyConsolePluginContext;
import org.processmining.processcomparator.view.ComparatorPanel;

import hfdd.evaluation.cdrift.evaluators.PVAOnCDEvaluator;
import hfdd.evaluation.cdrift.taskcreation.PVAOnCDTestSpec;

public class BoltEvaluator implements PVAOnCDEvaluator {
  private final static Logger logger = LogManager.getLogger(BoltEvaluator.class);

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
  
  /**
   * Configuration (key) of the method.
   */
  private final BoltPCConfig config;
  
  /**
   * Factory method
   * @param pathRestults Path (File) to write the results to
   * @return
   */
  public static BoltEvaluator init(Path pathRestults, BoltPCConfig config) {
    return new BoltEvaluator(pathRestults, config);
  }

  public BoltEvaluator(Path pathResult, BoltPCConfig config) {
    this.pathResult = pathResult;
    this.config = config;
  }

  @Override
  public boolean init() {
    try {
      writer = new BufferedWriter(new FileWriter(pathResult.toFile()));
      // Write Result Header
      writer.write(PVAOnCDTestResultBolt.getCSVHeader(NBR_TOP_SCORES));
      writer.write("\n");
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public boolean evaluateOnTask(PVAOnCDTestSpec testSpec) {
    logger.debug("Inside BOLT on {}", testSpec.idExtraction());

    // Dummy Plugin context
    PluginContext dummyContext = new DummyConsolePluginContext();

    // Setup of Bolt's Comparison method
    InputObject boltInput = new InputObject(testSpec.logL(), testSpec.logR());
    boltInput.setSelected_A(List.of(testSpec.logL()));
    boltInput.setSelected_B(List.of(testSpec.logR()));

    SettingsObject boltSettings = new SettingsObject(boltInput, dummyContext); // default to control flow
    
    if (config == BoltPCConfig.TWO_SEQ_HIST) {
      TSMinerInput tsSettings = boltSettings.getGraph_tsSettings().getObject();
      for (XEventClassifier classifier : tsSettings.getClassifiers()) {
        TSMinerModirInput modirInput = tsSettings.getModirSettings(TSDirections.BACKWARD, classifier);
        modirInput.setAbstraction(TSAbstractions.SEQUENCE);
        modirInput.setUse(true);
        modirInput.setFilteredHorizon(2);
      }
    }
    
    // Needed to run the HT
    // Unfortunately, HTs are tightly integrated with the UI
    ComparatorPanel boltPanelRoot = new ComparatorPanel();

    //////////////////////////////
    // Comparison
    //////////////////////////////
    // Run comparison
    long startTime = System.currentTimeMillis();
    ResultsObject boltResults = TransitionSystemUtils.createResultsObject(dummyContext, boltSettings, boltInput, null);

    // The HT are eventually triggered by this method
    DrawUtils.createGraph(boltResults, boltSettings, boltPanelRoot, boltInput);

    long time = System.currentTimeMillis() - startTime;

    //////////////////////////////
    // Process PVA Results
    //////////////////////////////
    //Supplier<PriorityQueue<Double>> supplyInvertedQueue = () -> new PriorityQueue<Double>(Comparator.reverseOrder());
    //PriorityQueue<Double> highScores = Stream
    //    .concat(MetricExtractor.instance().getStateEffectSize().values().stream(),
    //        MetricExtractor.instance().getTransitionEffectSize().values().stream())
    //    .map(Math::abs)
    //    .sequential()
    //    .collect(Collectors.toCollection(supplyInvertedQueue));
    //List<Double> selectedHighScores = highScores.stream().limit(NBR_TOP_SCORES).toList();
    List<Double> highScores = new ArrayList<>(MetricExtractor.instance().getStateEffectSize().values());
    highScores.addAll(MetricExtractor.instance().getTransitionEffectSize().values());
    highScores.sort(Comparator.reverseOrder());
    List<Double> selectedHighScores = highScores.subList(0, 
        Math.min(NBR_TOP_SCORES, highScores.size()));

    ////////////////////
    // Build Result Data Structure
    ////////////////////
    String methodName = "boltDefault";
    if (config == BoltPCConfig.TWO_SEQ_HIST) {
      methodName = "boltTwoSeqHist";
    }
    PVAOnCDTestResultBolt testResult = new PVAOnCDTestResultBolt(testSpec.collectionName(),
        testSpec.identifierOrigLog(), testSpec.idExtraction(), testSpec.logType(), testSpec.previousRelatedCD(),
        testSpec.nextRelatedCD(), methodName, time, testSpec.driftInducedDifference(), selectedHighScores);

    // Leave in clean state (for next run)
    MetricExtractor.instance().clearMetrics();
    // Write evaluation result line (thread safe)
    logger.debug("Finalizing BOLT on {}", testSpec.idExtraction());
    return writeResultFile(testResult.toCSVString(NBR_TOP_SCORES) + "\n");
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
    return "BOLT";
  }

}
