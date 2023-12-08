package hfdd.evaluation.cdrift;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hfdd.evaluation.cdrift.evaluators.PVAOnCDEvaluator;
import hfdd.evaluation.cdrift.evaluators.SPSEvaluator;
import hfdd.evaluation.cdrift.evaluators.bolt.BoltEvaluator;
import hfdd.evaluation.cdrift.evaluators.bolt.BoltPCConfig;
import hfdd.evaluation.cdrift.evaluators.janus.JanusEvaluator;
import hfdd.evaluation.cdrift.evaluators.plainemd.PlainEMDEvaluator;
import hfdd.evaluation.util.PropertiesLoader;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class PVAOnCDMain {
  private final static Logger logger = LogManager.getLogger(PVAOnCDMain.class);

  public static void main(String[] args) {
    ArgumentParser parser = ArgumentParsers.newFor("PVA Sensitivity Test on CD Logs")
        .build();
    parser.addArgument("--cdcollections").type(String.class).help("Path to the collections (i.e., directories) of CD logs");
    parser.addArgument("--resultdirectory").type(String.class).help("Path to the result directory");
    parser.addArgument("--sps").action(Arguments.storeTrue()).help("Include SPS Evaluation");
    parser.addArgument("--janus").action(Arguments.storeTrue()).help("Include Janus Evaluation");
    parser.addArgument("--boltDefault").action(Arguments.storeTrue()).help("Include Bolt Evaluation (default parameters)");
    parser.addArgument("--boltTwoSeq").action(Arguments.storeTrue()).help("Include Bolt Evaluation (two sequence abstraction)");
    parser.addArgument("--plainemd").action(Arguments.storeTrue()).help("Include Plain EMD");
    parser.addArgument("--evalparallel").action(Arguments.storeTrue()).help("Run evaluation loop concurrently");
    
    Namespace resArgs;
    try {
      resArgs = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      return;
    }

    Path pathCDCollections = Path.of(resArgs.getString("cdcollections"));
    Path pathResultDir = Path.of(resArgs.getString("resultdirectory"));
    
    ////////////////////////////////////////
    // Evaluators 
    ////////////////////////////////////////
    List<PVAOnCDEvaluator> evaluators = new LinkedList<>();
    if (resArgs.getBoolean("janus")) {
      // Setup Janus
      try {
        Path resultJanus = Files.createDirectory(pathResultDir.resolve("janus")); // Directory
        JanusEvaluator janusEval = JanusEvaluator.initFromProperties(
            PropertiesLoader.readPropertiesFileFromResources(
                PVAOnCDMain.class, "config-pva-on-cd-janus.properties"), // Load from properties file
            resultJanus);
            evaluators.add(janusEval);
      } catch (IOException e) {
        e.printStackTrace();
        logger.warn("Failed to create JANUS evaluator. Will continue without.");
      } 
    }
    if (resArgs.getBoolean("sps")) {
      Path resultSPS = pathResultDir.resolve("sps-results.csv");
      try {
        SPSEvaluator spsEval = SPSEvaluator.initFromProperties(
              PropertiesLoader.readPropertiesFileFromResources(
                  PVAOnCDMain.class, "config-pva-on-cd-sps.properties"), // Load from properties file
            resultSPS);
        evaluators.add(spsEval);
      } catch (IOException e) {
        e.printStackTrace();
        logger.warn("Failed to create SPS evaluator. Will continue without.");
      }
    }
    if (resArgs.getBoolean("boltDefault")) {
      Path resultBolt = pathResultDir.resolve("bolt-results-default.csv");
      BoltEvaluator boltEval = BoltEvaluator.init(resultBolt, BoltPCConfig.DEFAULT);
        evaluators.add(boltEval);
    }
    if (resArgs.getBoolean("boltTwoSeq")) {
      Path resultBolt = pathResultDir.resolve("bolt-results-twoSeq.csv");
      BoltEvaluator boltEval = BoltEvaluator.init(resultBolt, BoltPCConfig.TWO_SEQ_HIST);
        evaluators.add(boltEval);
    }
    if (resArgs.getBoolean("plainemd")) {
      Path resultPlainEMD = pathResultDir.resolve("plainemd-results.csv");
      PlainEMDEvaluator plainEMDEval = PlainEMDEvaluator.init(resultPlainEMD);
        evaluators.add(plainEMDEval);
    }
    
    // Evaluation
    PVAOnCDEvaluation mainEval = new PVAOnCDEvaluation(pathCDCollections, evaluators);
    mainEval.runEvaluation(resArgs.getBoolean("evalparallel"));

  }

}
