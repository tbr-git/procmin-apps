package hfdd.evaluation.cdrift;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuildingException;

import hfdd.evaluation.cdrift.evaluators.PVAOnCDEvaluator;
import hfdd.evaluation.cdrift.evaluators.SPSEvaluator;
import hfdd.evaluation.cdrift.evaluators.bolt.BoltEvaluator;
import hfdd.evaluation.cdrift.evaluators.bolt.BoltPCConfig;
import hfdd.evaluation.cdrift.evaluators.janus.JanusEvaluator;
import hfdd.evaluation.cdrift.evaluators.plainemd.PlainEMDEvaluator;
import hfdd.evaluation.cdrift.taskcreation.CDInducedPVATaskIterator;
import hfdd.evaluation.util.PropertiesLoader;

class PVAOnCDEvaluationTest {
  
  public static final String pathCDFullStr = "C:/temp/dataset/test/cdrift-evaluation";
  
  public static final String pathCDSingleStr = "C:/temp/dataset/test/cdrift-evaluation-single";
  
  @Test
  void testFull() throws HFDDIterationManagementBuildingException, IOException {
    Path testFullResults = Files.createTempDirectory("pvaoncdresultsfull"); // Directory
    //////////////////////////////
    // Run Evaluation
    //////////////////////////////
    PVAOnCDEvaluation mainEval = this.initEvaluationInstance(Paths.get(pathCDFullStr), testFullResults);
    PVAOnCDEvaluation.TEST_SHORTEN_LOGS = true;
    mainEval.runEvaluation(true);
    
    //List<Path> dirsEvaluatorResults = Files.list(testFullResults).toList();
    //assertEquals(2, dirsEvaluatorResults.size());
    //for (Path dirEvaluator : dirsEvaluatorResults) {
    //  assertTrue(Files.list(dirEvaluator).count() > 0);
    //}

    ////////////////////////////////////////
    // Print all files
    ////////////////////////////////////////
    Files.walk(testFullResults)
      .filter(Files::isRegularFile)
      .map(Path::toFile)
      .forEach(f -> {
        System.out.println(f.getAbsolutePath());
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
          String line;
          while ((line = br.readLine()) != null) {
            System.out.println(line);
          }
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    
    // Delete everything
    //Files.walk(testFullResults)
    //  .sorted(Comparator.reverseOrder())
    //  .map(Path::toFile)
    //  .forEach(File::delete);
    
  }

  @Test
  void testSPSSingleRuns() throws HFDDIterationManagementBuildingException, IOException {
    Path pathCollections = Path.of(pathCDSingleStr);

    // SPS
    Path tmpResultSPS = Files.createTempDirectory("spsmocksingle"); // Directory
    SPSEvaluator spsEval = SPSEvaluator.initFromProperties(
        PropertiesLoader.readPropertiesFileFromResources(
            getClass(), "config-pva-on-cd-sps.properties"), // Load from properties file
        tmpResultSPS); // Evaluator
    
    // Task Iterator
    CDInducedPVATaskIterator itTask = new CDInducedPVATaskIterator(pathCollections);

    // Run SPS on a few Tasks
    List<PVAOnCDSPSTestResult> allResults = new LinkedList<>();
    
    int i = 0;
    while (itTask.hasNext() && i < 3) {
      PVAOnCDSPSTestResult res = spsEval.evalHFDD(itTask.next());
      allResults.add(res);
      System.out.println(res.toCSVString(5));
      i++;
    }
    assertTrue(allResults.get(0).selectedTopScores().get(0) < allResults.get(1).selectedTopScores().get(0));
    assertTrue(allResults.get(2).selectedTopScores().get(0) < allResults.get(1).selectedTopScores().get(0));
    
    Files.delete(tmpResultSPS);
  }
  
  /**
   * Instantiate the evaluation class.
   * 
   * Creates the evaluation class with evaluators:
   * <p>
   * <ul>
   * <li> SPS </li>
   * <li> Janus </li>
   * </ul>
   * <p>
   * Provide it a test directory path (method will create it, if it does not exist) that
   * does not cause collisions between tests.
   * 
   * @param pathCollections 
   * @param testDirectory Path to test directory
   * @return
   * @throws IOException 
   */
  private PVAOnCDEvaluation initEvaluationInstance(Path pathCollections, Path tmpResult) throws IOException {
    // Not a directory, or does not exist
    if (!Files.isDirectory(tmpResult)) {
      // If a file, we have problem
      if (Files.exists(tmpResult)) {
        return null;
      }
      else { // Try to create it
        Files.createTempDirectory(tmpResult.toString()); // Directory
      }
    }
    //////////////////////////////
    // Evaluators
    //////////////////////////////
    // SPS
    //Path tmpResultSPSDir = Files.createTempDirectory(tmpResult, "sps");
    //Path tmpResultSPS = Files.createTempFile(tmpResultSPSDir, "sps-results", ".csv"); // File
    //SPSEvaluator spsEval = SPSEvaluator.initFromProperties(
    //    PropertiesLoader.readPropertiesFileFromResources(
    //        getClass(), "config-pva-on-cd-sps.properties"), // Load from properties file
    //    tmpResultSPS); // Evaluator

    // Janus
    //Path tmpResultJanus = Files.createTempDirectory(tmpResult, "janus"); // Directory
    //JanusEvaluator janusEval = JanusEvaluator.initFromProperties(
    //    PropertiesLoader.readPropertiesFileFromResources(
    //        getClass(), "config-pva-on-cd-janus.properties"), // Load from properties file
    //    tmpResultJanus); // Evaluator
    
    // Bolt Default
    Path tmpResultBolt = Files.createTempFile(tmpResult, "bolt-results-default", ".csv"); // File
    BoltEvaluator boltEvaluator = BoltEvaluator.init(tmpResultBolt, BoltPCConfig.DEFAULT);

    // Bolt Two Sequence History
    Path tmpResultBoltTwoSeq = Files.createTempFile(tmpResult, "bolt-results-twoSeqHist", ".csv"); // File
    BoltEvaluator boltEvaluatorTwoSeq = BoltEvaluator.init(tmpResultBoltTwoSeq, BoltPCConfig.TWO_SEQ_HIST);
    
    // Pure EMD
    Path tmpResultPlainEMD = Files.createTempFile(tmpResult, "plainEMD-results", ".csv"); // File
    PlainEMDEvaluator plainEMDEvaluator = PlainEMDEvaluator.init(tmpResultPlainEMD);

    //////////////////////////////
    // Setup Evaluation
    //////////////////////////////
    //List<PVAOnCDEvaluator> evaluators = List.of(spsEval, janusEval);
    //List<PVAOnCDEvaluator> evaluators = List.of(janusEval);
    List<PVAOnCDEvaluator> evaluators = List.of(boltEvaluator, boltEvaluatorTwoSeq, plainEMDEvaluator);
    PVAOnCDEvaluation mainEval = new PVAOnCDEvaluation(pathCollections, evaluators);
    
    return mainEval;
  }

}
