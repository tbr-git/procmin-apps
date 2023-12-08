package hfdd.evaluation.cdrift;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;

import com.google.common.base.Joiner;

import hfdd.evaluation.cdrift.evaluators.PVAOnCDEvaluator;
import hfdd.evaluation.cdrift.taskcreation.CDInducedPVATaskIterator;
import hfdd.evaluation.cdrift.taskcreation.PVAOnCDTestSpec;

public class PVAOnCDEvaluation {

  private final static Logger logger = LogManager.getLogger(PVAOnCDEvaluation.class);

  private final Path pathCollections;

  private final List<PVAOnCDEvaluator> evaluators;

  public static boolean TEST_SHORTEN_LOGS = false;

  public static int NBR_THREADS = 7;

  public PVAOnCDEvaluation(Path pathCollections, List<PVAOnCDEvaluator> evaluators) {
    this.pathCollections = pathCollections;
    // If initialization fails, I want to remove the evaluator.
    // => Needs to modifiable
    // Besides, avoid external modfication of list.
    this.evaluators = new LinkedList<>(evaluators);
  }

  public void runEvaluation(boolean parallel) {
    initEvaluators();
    if (!parallel) {
      logger.info("Entering main evaluation loop with {} approaches to evaluate", evaluators.size());
      runEvaluationSequential();
    } else {
      logger.info("Entering main evaluation loop with {} approaches to evaluate concurrently", evaluators.size());
      runEvaluationConcurrent();
    }
  }

  private void initEvaluators() {
    //////////////////////////////
    // Initialization
    //////////////////////////////
    ListIterator<PVAOnCDEvaluator> itEvaluators = this.evaluators.listIterator();
    while (itEvaluators.hasNext()) {
      boolean initSuccess = itEvaluators.next().init();
      // If initialization not successful,
      // DROP evaluator
      if (!initSuccess) {
        itEvaluators.remove();
      }
    }
  }

  public void runEvaluationSequential() {
    //////////////////////////////
    // Evaluation Loop
    //////////////////////////////
    CDInducedPVATaskIterator itTask = new CDInducedPVATaskIterator(pathCollections);

    Map<String, Integer> countSuccRuns = new ConcurrentHashMap<>();
    Map<String, Integer> countFailedRuns = new ConcurrentHashMap<>();

    while (itTask.hasNext()) {
      PVAOnCDTestSpec testSpec = itTask.next();
      // Potential simplification for testing purposes
      testSpec = adaptSpec(testSpec);
      for (PVAOnCDEvaluator evaluator : this.evaluators) {
        boolean success = false;
        try {
          success = evaluator.evaluateOnTask(testSpec);
        } catch (Exception e) {
          e.printStackTrace();
        }

        // Bookkeeping of Success
        if (success) {
          int oldVal = countSuccRuns.getOrDefault(evaluator.getNameUnique(), 0);
          countSuccRuns.put(evaluator.getNameUnique(), oldVal + 1);
        } else {
          int oldVal = countFailedRuns.getOrDefault(evaluator.getNameUnique(), 0);
          countFailedRuns.put(evaluator.getNameUnique(), oldVal + 1);
        }
      }
    }

    // Cleanup
    for (PVAOnCDEvaluator evaluator : this.evaluators) {
      evaluator.complete();
    }

    logPostEvaluationInfo(countSuccRuns, countFailedRuns);
  }

  public void runEvaluationConcurrent() {
    //////////////////////////////
    // Evaluation Loop
    //////////////////////////////
    CDInducedPVATaskIterator itTask = new CDInducedPVATaskIterator(pathCollections);

    /////////////////////////////
    // Schedule Evaluation Tasks
    // Concurrently
    //////////////////////////////
    // Default implementation of the close method required by the try with resource pattern
    // will wait 1 day for still running tasks to terminate (should be enough)
    List<Pair<String, Future<Boolean>>> tasks = new LinkedList<>();
    try (ExecutorService executorService = new ThreadPoolExecutor(NBR_THREADS, NBR_THREADS, 1000L,
        TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(NBR_THREADS), new ThreadPoolExecutor.CallerRunsPolicy())) {

      while (itTask.hasNext()) {
        PVAOnCDTestSpec testSpec = itTask.next();
        testSpec = adaptSpec(testSpec);
        for (PVAOnCDEvaluator evaluator : this.evaluators) {
          Future<Boolean> f = executorService.submit(new SingleEvaluationTask(testSpec, evaluator));
          tasks.add(Pair.of(evaluator.getNameUnique(), f));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    ////////////////////////////////////////// 
    // Bookkeeping sucessful runs
    ////////////////////////////////////////// 
    ////////////////////
    // Setup Counter
    ////////////////////
    final Map<String, Integer> countSuccRuns = new HashMap<>();
    final Map<String, Integer> countFailedRuns = new HashMap<>();
    for (PVAOnCDEvaluator evaluator : this.evaluators) {
      countSuccRuns.put(evaluator.getNameUnique(), 0);
      countFailedRuns.put(evaluator.getNameUnique(), 0);
    }
    
    ////////////////////
    // Check Runs 
    ////////////////////
    for (Pair<String, Future<Boolean>> task : tasks) {
      boolean success = false;
      try {
        success = task.getRight().get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
      
      if (!success) {
        int curCount = countFailedRuns.get(task.getLeft());
        countFailedRuns.put(task.getLeft(), curCount + 1);
      }
      else {
        int curCount = countSuccRuns.get(task.getLeft());
        countSuccRuns.put(task.getLeft(), curCount + 1);
      }
    }
    logPostEvaluationInfo(countSuccRuns, countFailedRuns);
    
    // Cleanup
    for (PVAOnCDEvaluator evaluator : this.evaluators) {
      evaluator.complete();
    }
  }

  private void logPostEvaluationInfo(Map<String, Integer> countSuccRuns, Map<String, Integer> countFailedRuns) {
    // Some final information
    logger.info("Finished {} sucessful and {} failed evaluation runs",
        countSuccRuns.values().stream().mapToInt(i -> i).sum(),
        countFailedRuns.values().stream().mapToInt(i -> i).sum());
    logger.info("Details successful runs: {}", Joiner.on(",").withKeyValueSeparator("=").join(countSuccRuns));
    logger.info("Details failed runs: {}", Joiner.on(",").withKeyValueSeparator("=").join(countFailedRuns));
  }

  private PVAOnCDTestSpec adaptSpec(PVAOnCDTestSpec testSpec) {
    if (TEST_SHORTEN_LOGS) {
      XLog logL = testSpec.logL();
      logL = PVATaskExtractionFromCDLog.extractSubLog(logL, 0, Math.min(logL.size(), 20), "_short");
      XLog logR = testSpec.logR();
      logR = PVATaskExtractionFromCDLog.extractSubLog(logR, 0, Math.min(logR.size(), 20), "_short");

      return new PVAOnCDTestSpec(testSpec.collectionName(), testSpec.identifierOrigLog(), testSpec.idExtraction(),
          testSpec.logType(), testSpec.previousRelatedCD(), testSpec.nextRelatedCD(), logL, logR,
          testSpec.driftInducedDifference());
    } else {
      return testSpec;
    }
  }

  public class SingleEvaluationTask implements Callable<Boolean> {

    private final PVAOnCDTestSpec testSpec;

    private final PVAOnCDEvaluator evaluator;

    public SingleEvaluationTask(PVAOnCDTestSpec testSpec, PVAOnCDEvaluator evaluator) {
      super();
      this.testSpec = testSpec;
      this.evaluator = evaluator;
    }

    @Override
    public Boolean call() throws Exception {
      boolean success = false;
      success = evaluator.evaluateOnTask(testSpec);

      return success;
    }
  }
}
