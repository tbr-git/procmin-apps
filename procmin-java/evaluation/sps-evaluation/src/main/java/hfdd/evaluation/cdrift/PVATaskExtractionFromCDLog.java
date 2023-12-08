package hfdd.evaluation.cdrift;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import de.rwth.processmining.tb.core.util.LogLoader;
import hfdd.evaluation.cdrift.taskcreation.CDFile;
import hfdd.evaluation.cdrift.taskcreation.CDPVALogType;
import hfdd.evaluation.cdrift.taskcreation.PVAOnCDTestSpec;

public class PVATaskExtractionFromCDLog {
  
  public static int MIN_LOGSIZE = 100;

	private final static Logger logger = LogManager.getLogger( PVATaskExtractionFromCDLog.class );
	
	public static Optional<List<PVAOnCDTestSpec>> createPVATasksFromCDCollection(CDFile cdFile) {
	  
	  String collectionName = cdFile.collection().toLowerCase();
	  List<Integer> driftPoints;
	  
	  ////////////////////////////////////////
	  // Concept Drift Points
	  // Based on mechanism of the log collection
	  ////////////////////////////////////////
	  if (collectionName.equals("bose")) {
	    driftPoints = List.of(1200, 2400, 3600, 4800);
	  }
	  else if (collectionName.equals("ostovar")) {
	    driftPoints = List.of(1_000, 2_000);
	  }
	  else if (collectionName.equals("ceravolo")) {
	    String filename = cdFile.logFile().getFileName().toString();
      String[] split = filename.split("_");
      int cdIndex = Integer.parseInt(split[3]);
      driftPoints = List.of((cdIndex + 1) / 2);
	  }
	  else {
      return Optional.empty();
	  }
	   

	  ////////////////////////////////////////
	  // Load Log
	  ////////////////////////////////////////
    XLog cdLog;
    String logName = cdFile.logFile().getFileName().toString();
    try {
      cdLog = LogLoader.loadLog(cdFile.logFile().toFile());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return Optional.empty();
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
    
    if (cdLog == null) {
      return Optional.empty();
    }

	  ////////////////////////////////////////
    // Extract PVA Tasks
	  ////////////////////////////////////////
    List<PVAOnCDTestSpec> tasks = createPVATasksFromCDTask(
        collectionName, logName, cdLog, driftPoints);
    
    return Optional.of(tasks);
	  
	}
	
  protected static List<PVAOnCDTestSpec> createPVATasksFromCDTask(String collectionName, String cdLogName, 
      XLog cdLog, List<Integer> driftPoints) {
    logger.debug("Processing CD Log {}", cdLogName);
    // Sort drift points
    driftPoints = new LinkedList<>(driftPoints);
    driftPoints.sort(Integer::compare);

    List<PVAOnCDTestSpec> extractedTestSpecs = new LinkedList<>();
    // Number of unsuitable windows (logs would be too small)
    int nbrUnsuitableWindows = 0;
    // Number of logs extracted
    int extractedLogs = 0;
    // Drift point bookkeeping
    Optional<Integer> nextDriftNumber = Optional.of(0);
    Optional<Integer> prevDriftNumber = Optional.empty();
    CDPVALogType logType = CDPVALogType.PRE_DRIFT;
    String idNextExtraction = getLogId(collectionName, cdLogName, extractedLogs);
    
    ////////////////////////////////////////
    // Iterate over Drift Points
    ////////////////////////////////////////
    int startWindow = 0;
    int midPoint = 0;
    PeekingIterator<Integer> itDriftPoints = Iterators.peekingIterator(driftPoints.iterator());
    Optional<PVAOnCDTestSpec> testSpec;
    
    while (itDriftPoints.hasNext()) {
      int nextDriftPoint = itDriftPoints.next();
      ////////////////////
      // Pre-Drift window
      // -> No differences
      // [last Drift or 0, drift)
      ////////////////////
      midPoint = startWindow / 2 + nextDriftPoint / 2;
      testSpec = extractSpec(collectionName, cdLogName, idNextExtraction, 
          logType, prevDriftNumber, nextDriftNumber,
          cdLog, startWindow, midPoint, nextDriftPoint, false);
      if (testSpec.isPresent()) {
        extractedTestSpecs.add(testSpec.get());
        extractedLogs++;

        idNextExtraction = getLogId(collectionName, cdLogName, extractedLogs);
      }
      else {
        nbrUnsuitableWindows++;
      }
      
      ////////////////////
      // Drift window
      // -> Differences
      // [midPoint previous no drift window, midpoint drift - next drift or end of log)
      ////////////////////
      // Drift itself is its previous and next related drift 
      prevDriftNumber = nextDriftNumber; 
      logType = CDPVALogType.DRIFT;
      // Start
      startWindow = (nextDriftPoint + startWindow) / 2;
      midPoint = nextDriftPoint;
      int endWindow = (midPoint + (itDriftPoints.hasNext() ? itDriftPoints.peek() : cdLog.size())) / 2;
      testSpec = extractSpec(collectionName, cdLogName, idNextExtraction, 
          logType, prevDriftNumber, nextDriftNumber,
          cdLog, startWindow, midPoint, endWindow, true);
      if (testSpec.isPresent()) {
        extractedTestSpecs.add(testSpec.get());
        extractedLogs++;
        idNextExtraction = getLogId(collectionName, cdLogName, extractedLogs);
      }
      else {
        nbrUnsuitableWindows++;
      }
      
      // Setup next Iteration
      startWindow = nextDriftPoint;
      // Next window will be an "inbetween" drifts window
      logType = CDPVALogType.POST_PRE_DRIFT;
      nextDriftNumber = Optional.of(nextDriftNumber.get() + 1);
    }
    
    ////////////////////
    // Process remaining data (no drift)
    ////////////////////
    // No next drift
    nextDriftNumber = Optional.empty();
    logType = CDPVALogType.POST_DRIFT;
    int endWindow = cdLog.size();
    // Both logs would have minimum size
    midPoint = startWindow / 2 + endWindow / 2;
      testSpec = extractSpec(collectionName, cdLogName, idNextExtraction, 
          logType, prevDriftNumber, nextDriftNumber,
          cdLog, startWindow, midPoint, endWindow, false);
    
    if (testSpec.isPresent()) {
      extractedTestSpecs.add(testSpec.get());
    }
    else {
      nbrUnsuitableWindows++;
    }
    
    // Problem Logging
    if (nbrUnsuitableWindows > 0) {
      logger.warn("{} potential log pairs extracted from {} for PVA were found to be unsuitable", 
          nbrUnsuitableWindows, cdLogName);
    }
    
    return extractedTestSpecs;
    
  }
  
  /**
   * Extract a PVA test specification from a log. 
   * Extract [start, midPoint), and [midPoint, endWindow) logs.
   * 
   * @param collectionName Name of the CD log collection this log belongs to
   * @param identifierOrigLog  Identifier for the CD log
   * @param idExtraction Identifier for the extracted PVA log
   * @param cdLog Log to extract a sublog from
   * @param startWindow Start of the window
   * @param midPoint Midpoint of the window
   * @param endWindow End of the window
   * @param containsCenterDriftPoint
   * @return Empty, if windows too small
   */
  private static Optional<PVAOnCDTestSpec> extractSpec(String collectionName, String identifierOrigLog, 
      String idExtraction, 
      CDPVALogType logType, Optional<Integer> previousRelatedCD, Optional<Integer> nextRelatedCD, 
      XLog cdLog, int startWindow, int midPoint, int endWindow, boolean containsCenterDriftPoint) {
    // Both logs would have minimum size
    if ((midPoint - startWindow >= MIN_LOGSIZE) && (endWindow - midPoint >= MIN_LOGSIZE)) {
      // Extract logs
      XLog logL = extractSubLog(cdLog, startWindow, midPoint, "_left");
      XLog logR = extractSubLog(cdLog, midPoint, endWindow, "_right");

      // Create Spec
      PVAOnCDTestSpec testSpec = new PVAOnCDTestSpec(collectionName, identifierOrigLog, idExtraction,
          logType, previousRelatedCD, nextRelatedCD,
          logL, logR, containsCenterDriftPoint);
      return Optional.of(testSpec);
    }
    else {
      return Optional.empty();
    }
  }

  
  /**
   * Extract a sublog containing cloned traces between start (inclusive) and end (exclustive).
   * @param log Log to extract the traces from
   * @param start Start extraction at trace (inclusive)
   * @param end End extraction at trace (exclusive)
   * 
   * @return Extracted sublog with cloned traces
   */
  protected static XLog extractSubLog(XLog log, int start, int end, String logNameExtension) {
    assert start >= 0;
    assert end <= log.size();

    XLog logSub = new XLogImpl((XAttributeMap) log.getAttributes().clone());
    ////////////////////
    // Log Name
    ////////////////////
    String logName = XConceptExtension.instance().extractName(logSub);
    if (logName == null) {
      logName = "Log";
    }
    logName = logName + logNameExtension;
    XConceptExtension.instance().assignName(logSub, logName);
    
    ////////////////////
    // Traces
    ////////////////////
    int nbrTracesAdd = end - start;
    Iterator<XTrace> itTraces = log.listIterator(start);
    while (nbrTracesAdd > 0) {
      assert itTraces.hasNext();
      XTrace t = itTraces.next();
      logSub.add((XTrace)t.clone());
      nbrTracesAdd--;
    }
    return logSub;
  }
  
  private static String getLogId(String collectionName, String logName, int extractedLogs) {
    return collectionName + "-" + logName + "-" + extractedLogs;
  }

}
