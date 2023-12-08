package hfdd.evaluation.cdrift.taskcreation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.util.streaming.Either;
import hfdd.evaluation.cdrift.PVATaskExtractionFromCDLog;


/**
 * Created and iterate over PVA tasks derived from a provided path to a collection of 
 * concept drift logs.
 */
public class CDInducedPVATaskIterator implements Iterator<PVAOnCDTestSpec> {
	private final static Logger logger = LogManager.getLogger( CDInducedPVATaskIterator.class );

	/**
	 * Path to directory containing concept drift log collections 
	 */
  private final Path cdLogPath;
  
  /**
   * Count how many concept drift collection walks failed
   */
  private int failedCDDirectoryWalks = 0;
  
  /**
   * Count how often a PVA log extraction from a single concept drift log failed
   */
  private int failedLogExtractions = 0;
  
  /**
   * Internal iterator over tasks
   */
  private Iterator<PVAOnCDTestSpec> internalTaskIterator;
  
  /**
   * Initialization already attempted?
   */
  private boolean isInitialized;

  /**
   * Constructor
   * @param cdLogPath Path containing collections (i.e, directories) of concept drift logs
   */
  public CDInducedPVATaskIterator(Path cdLogPath) {
    this.cdLogPath = cdLogPath;
    this.isInitialized = false;
    this.internalTaskIterator = null;
  }
  
  /**
   * Setup the internal iterator
   */
  private void setup() {
    // Initialization attempted?
    this.isInitialized = true;
    List<Path> cdCollectionPaths = null;
    try (Stream<Path> cdCollections = Files.list(cdLogPath)) {
      cdCollectionPaths = cdCollections.
        filter(Files::isDirectory)
        .toList();
    } catch (IOException e) {
      e.printStackTrace();
      logger.error("Failed to list concept drift logs top directories!");
      return;
    }
    
    logger.info("Found {} concept drift log collections", cdCollectionPaths.size());

    //////////////////////////////
    // PVA Task Extraction
    //////////////////////////////
    internalTaskIterator = cdCollectionPaths.stream()
        // Create Walk collections recursively; only maintain files 
        .map(Either.lift(
            p -> new CDCollectionStream(p.getFileName().toString(), 
                Files.walk(p).filter(Files::isRegularFile))))
        // Count failed walk creations
        .peek(e -> {
          if (e.isLeft()) {
            failedCDDirectoryWalks++;
          }
        })
        // Filter and extract successful walks
        .filter(e -> e.isRight())
        .map(Either::getRight)
        .map(Optional::get)
        .flatMap(c -> c.logPathStream().map(f -> new CDFile(c.collectionName(), f)))
        // Filter out Ceravolo 500
        .filter(f -> !("Ceravolo".equalsIgnoreCase(f.collection()))
            || !f.logFile().getFileName().toString().contains("_500_")) 
        .peek(logger::debug)
        // Run extraction on log
        .map(PVATaskExtractionFromCDLog::createPVATasksFromCDCollection)
        // Count failed extractions
        .peek(t -> {
          if (t.isEmpty()) {
            failedLogExtractions++;
          }
        })
        .filter(Optional::isPresent)
        .map(Optional::get)
        // Flat map tasks
        .flatMap(List::stream)
        .iterator();
  }

  @Override
  public boolean hasNext() {
    // Have we tried to initialize it -> don't try again
    if (!isInitialized && internalTaskIterator == null) {
      this.setup();
    }
    
    // Use internal task iterator
    if (internalTaskIterator != null) {
      boolean hasNext = internalTaskIterator.hasNext();
      if (!hasNext) {
        this.postIterationLogging();
      }
      return hasNext;
    }
    else {
      return false;
    }
  }

  @Override
  public PVAOnCDTestSpec next() {
    return this.internalTaskIterator.next();
  }
  
  /**
   * Log some post iteration diagnostics. 
   */
  private void postIterationLogging() {
    if (failedCDDirectoryWalks > 0) {
      logger.warn("Failed to walk {} concept drift collection directories", failedCDDirectoryWalks);
    }
    if (failedLogExtractions > 0) {
      logger.warn("Log extraction failed on {} cncept drift logs", failedLogExtractions);
    }
  }

}
