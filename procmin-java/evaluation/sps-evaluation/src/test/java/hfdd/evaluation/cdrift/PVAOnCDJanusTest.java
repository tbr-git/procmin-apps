package hfdd.evaluation.cdrift;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.deckfour.xes.model.XLog;
import org.junit.jupiter.api.Test;

import de.rwth.processmining.tb.core.util.LogLoader;
import hfdd.evaluation.cdrift.evaluators.PVAOnCDEvaluator;
import hfdd.evaluation.cdrift.evaluators.janus.JanusEvaluator;
import hfdd.evaluation.cdrift.taskcreation.CDPVALogType;
import hfdd.evaluation.cdrift.taskcreation.PVAOnCDTestSpec;
import hfdd.evaluation.util.PropertiesLoader;


class PVAOnCDJanusTest {

  public static final String pathCDSingleStr = "C:/temp/dataset/test/cdrift-evaluation-single";
  
  public static final String pathSepsisLeftStr = "C:/temp/dataset/Sepsis/SepsisYoungerThanEqual35.xes";

  public static final String pathSepsisRightStr = "C:/temp/dataset/Sepsis/SepsisAtLeast70.xes";

  @Test
  void testSPSSingleRuns() throws IOException {
    Path pathCollections = Path.of(pathCDSingleStr);

    Path tmpResultJanus = Files.createTempDirectory("janussingle"); // Directory

    // Janus
    JanusEvaluator janusEval = JanusEvaluator.initFromProperties(
        PropertiesLoader.readPropertiesFileFromResources(
            getClass(), "config-pva-on-cd-janus.properties"), // Load from properties file
        tmpResultJanus); // Evaluator

    List<PVAOnCDEvaluator> evaluators = List.of(janusEval);
    PVAOnCDEvaluation mainEval = new PVAOnCDEvaluation(pathCollections, evaluators);
    
    mainEval.runEvaluation(true);
    
    ////////////////////////////////////////
    // Print all files
    ////////////////////////////////////////
    Files.walk(tmpResultJanus)
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
  }
  
  @Test
  void testJanusSepsis() throws IOException {
    Path tmpResultJanus = Files.createTempDirectory("janussinglesepsis"); // Directory

    // Janus
    JanusEvaluator janusEval = JanusEvaluator.initFromProperties(
        PropertiesLoader.readPropertiesFileFromResources(
            getClass(), "config-pva-on-cd-janus.properties"), // Load from properties file
        tmpResultJanus); // Evaluator
    
    // Load Sepsis
    XLog logLeft = LogLoader.loadLog(pathSepsisLeftStr);
    XLog logRight = LogLoader.loadLog(pathSepsisRightStr);
    
    PVAOnCDTestSpec testSpec = new PVAOnCDTestSpec("Sepsis", "SepsisYoungOld", "sepsis-1", CDPVALogType.DRIFT, 
        Optional.empty(), Optional.empty(), logLeft, logRight, true);
    
    janusEval.init();
    janusEval.evaluateOnTask(testSpec);
    janusEval.complete();
    
    ////////////////////////////////////////
    // Print all files
    ////////////////////////////////////////
    Files.walk(tmpResultJanus)
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
  }

}
