package hfdd.evaluation.cdrift.evaluators.janus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;

import hfdd.evaluation.cdrift.evaluators.PVAOnCDEvaluator;
import hfdd.evaluation.cdrift.taskcreation.PVAOnCDTestSpec;

public class JanusEvaluator implements PVAOnCDEvaluator {
	private final static Logger logger = LogManager.getLogger( JanusEvaluator.class );
	
	private final AtomicInteger evalRunCounter;
	
	/**
	 * Path of the Janus jar file
	 */
	private final Path janusJar;
	
	/**
	 * Directory to which the Janus tool can write its results.
	 */
	private final Path dirJanusOut;
	
	/**
	 * Meta parameters of the method (e.g., number of permutations).
	 * 
	 */
	private final List<String> processMethodMetaParam;

	/**
	 * Writer to write the test case descriptions to a file.
	 */
  private BufferedWriter writerRunInfo;
  
  public static JanusEvaluator initFromProperties(Properties propConfigJanus, Path dirJanusOut) {
    final List<String> methodMetaParam = new LinkedList<>();
    String propJanusJar = propConfigJanus.getProperty("janusJar");
    Path janusJar = Paths.get(propJanusJar);
    // !!!!! The jar must be there !!!!!
    if (!Files.exists(janusJar)) {
      return null;
    }

    Function<String, Pair<String, String>> extractProp = p -> Pair.of(p, propConfigJanus.getProperty(p));
    propConfigJanus.stringPropertyNames().stream()
      .filter(p -> !p.equalsIgnoreCase("janusJar"))
      .map(extractProp)
      .filter(p -> p.getValue() != null)
      .forEach(p -> {
        if (p.getValue().equalsIgnoreCase("true")) {
          methodMetaParam.add("-" + p.getKey());
        }
        else if (p.getValue().equalsIgnoreCase("false")) {
          // do nothing -> ignore
        }
        else { // add key with "-" and value
          methodMetaParam.add("-" + p.getKey());
          methodMetaParam.add(p.getValue());
        }
      });
    return new JanusEvaluator(janusJar, dirJanusOut, methodMetaParam);
  }
	
	public JanusEvaluator(Path janusJar, Path dirJanusOut, List<String> processMethodMetaParam) {
	  this.janusJar = janusJar;
	  this.dirJanusOut = dirJanusOut;
	  this.processMethodMetaParam = processMethodMetaParam;
	  
	  this.evalRunCounter = new AtomicInteger(0);
	}
	
  @Override
  public String getNameUnique() {
    return "Janus";
  }
	
  @Override
  public boolean init() {
    // Create directory if not exists
    if (!Files.exists(dirJanusOut)) {
      logger.info("Janus output directory does not exist, creating it!");
      try {
        Files.createDirectory(dirJanusOut);
      } catch (IOException e) {
        logger.error("Failed to create Janus' output directory!");
        e.printStackTrace();
        return false;
      }
    }
    
    // Create a writer to a file that contains evaluation task specification information
    Path pathEvalDesc = dirJanusOut.resolve("run_descriptions.csv");
    try {
      writerRunInfo = new BufferedWriter(new FileWriter(pathEvalDesc.toFile()));
      // Write Result Header
      writerRunInfo.write(PVAOnCDTestSpec.getCSVHeader());
      writerRunInfo.write(",");
      // Id that connects id to Janus output file
      writerRunInfo.write("JanusId");
      writerRunInfo.write("\n");
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public boolean evaluateOnTask(PVAOnCDTestSpec testSpec) {
    // Id for this evaluation run
    final int janusTaskId = this.evalRunCounter.incrementAndGet();
    boolean success = runJanus(testSpec, janusTaskId);
    
    // If successful, also write spec 
    if (success) {
      try {
        // Spec
        writerRunInfo.write(testSpec.toCSVString());
        // and id
        writerRunInfo.write(",");
        writerRunInfo.write(Integer.toString(janusTaskId));
        writerRunInfo.write("\n");
      } catch (IOException e) {
        return false;
      }
    }
    return success;
  }

  @Override
  public boolean complete() {
    // Close result file writer
    try {
      writerRunInfo.close();
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  public boolean runJanus(PVAOnCDTestSpec testSpec, int janusTaskId) {
    //////////////////////////////
    // Create Temporary Log Files
    //////////////////////////////
    // Left log
    File logL = null;
    try {
      logL = serialize2TempLogFile(testSpec.logL(), "log_left");
    } catch (IOException e) {
      e.printStackTrace();
      logger.error("Failed to create temporary left log file");
      return false;
    }
    // Right log
    File logR = null;
    try {
      logR = serialize2TempLogFile(testSpec.logR(), "log_right");
    } catch (IOException e) {
      logger.error("Failed to create temporary right log file");
      if (logL != null) {
        logger.error("Cleaning the left log after failing to create right log file.");
        try {
          Files.delete(logL.toPath());
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
      return false;
    }
    
    ////////////////////////////////////////////////////////////
    // Command-line Arguments Janus Process
    ////////////////////////////////////////////////////////////
    Path pathJanusOutput = this.dirJanusOut.resolve("janus_result_" + janusTaskId + ".csv");
    List<String> args = new LinkedList<>();
    args.add("--in-log-1-file");
    args.add(logL.toString());
    args.add("--in-log-2-file");
    args.add(logR.toString());
    args.add("-oCSV");
    args.add(pathJanusOutput.toString());
    // Finally, the meta parameters
    args.addAll(processMethodMetaParam);
    
    // External run
    boolean success = runJanusExternal(args, janusJar);
    
    // Clean temp files
    if (logL != null) {
      logL.delete();
    }
    if (logR != null) {
      logR.delete();
    }
    
    return success;
  }
  
  public static boolean runJanusExternal(List<String> args, Path janusJar) {
    List<String> cmds = new LinkedList<String>();
    cmds.add("java");
    cmds.add("-cp");
    cmds.add(janusJar.toString());
    //cmds.add("minerful.JanusVariantAnalysisStarter");
    cmds.add("org.processmining.spsevaluation.janusproxy.JanusProxy");
    cmds.addAll(args);
    
    logger.debug("Arguments Janus call:" + cmds.toString());
    
    // Start the process
    ProcessBuilder processBuilder = new ProcessBuilder(cmds).inheritIO();
    try {
      Process process = processBuilder.start();
      process.waitFor();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  /**
   * Create a temporary .xes-file with the provided name (excluding suffix)
   * @param log Log
   * @param filename Filename (excluding suffix)
   * @return Handle to the file.
   * @throws IOException 
   */
  private static File serialize2TempLogFile(XLog log, String filename) throws IOException {
    // Create temporary file
    Path tmpFilePath = null;
    tmpFilePath = Files.createTempFile(filename, ".xes");
    
    // Serialize to file
    File f = tmpFilePath.toFile();
    serializeLogToXes(log, f);
    return f;
  }
  
  /**
   * Serialize the log into the provided file.
   * @param log Log to serialize
   * @param file File to serialize the log into
   * @return True, iff success
   * @throws IOException 
   */
  private static void serializeLogToXes(XLog log, File file) throws IOException {
    OutputStream out = new FileOutputStream(file);
    // Serialize the log 
    XesXmlSerializer serializer = new XesXmlSerializer();
    serializer.serialize(log, out);
  }
  
}
