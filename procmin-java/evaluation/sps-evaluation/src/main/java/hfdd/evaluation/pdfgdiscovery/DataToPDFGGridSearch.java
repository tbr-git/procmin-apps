package hfdd.evaluation.pdfgdiscovery;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuildingException;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.DataToPDFG;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.PTDFGVisualizationMetaParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSAutoPDFGExtractionMetaParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSComplementaryDiffParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSGraphMiningMetaParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSIVFMetaParam;

public class DataToPDFGGridSearch {
  
  private final SPSGraphMiningMetaParam spsGraphMiningParam;
  
  private Path dirResults;
  
   /**
   * Path to the left log
   */
  private final Path pathLogL;

  /**
   * Path to the right log
   */
  private final Path pathLogR; 
  
  /**
   * Override results already contained in the provided result directory
   */
  private boolean overrideResults = true;
  
  /**
   * Constructor
   * 
   * @param pathLogL Path to left log
   * @param pathLogR Path to right log
   * @param dirResults Parent directory that will contain another directory for each run.
   */
  public DataToPDFGGridSearch(Path pathLogL, Path pathLogR, Path dirResults) {
    this.spsGraphMiningParam = new SPSGraphMiningMetaParam();
    this.pathLogL = pathLogL;
    this.pathLogR = pathLogR;
    this.dirResults = dirResults;
  }
  
  
  public void run() throws FileNotFoundException {
    if (!Files.exists(this.dirResults)) {
      throw new FileNotFoundException("Result directory does not exist!");
    }
    try {
      // Those I liked for RTFM the most 
      //this.run(0.001, 1.2, 0.9, 0.05, 5, 3, 0.1f, 0.01f, 0.02f, "greedyExtension-strictComplement-filterFreqLow");
      SPSIVFMetaParam ivfParamSensitive = new SPSIVFMetaParam(0.001, 0.05, 0.9);
      SPSComplementaryDiffParam compDiffParamRelaxed = new SPSComplementaryDiffParam(0.2f);
      SPSAutoPDFGExtractionMetaParam autoDiagParam = new SPSAutoPDFGExtractionMetaParam(5, 3);
      PTDFGVisualizationMetaParam ptDfgVisParam = new PTDFGVisualizationMetaParam(0.02f, 0.01f, 0.15f, 0.5f, 5.5f);
      DataToPDFG dataToPdfg = new DataToPDFG(this.pathLogL, this.pathLogR, this.spsGraphMiningParam);
      dataToPdfg.runDifferenceExtraction(ivfParamSensitive, compDiffParamRelaxed, autoDiagParam, ptDfgVisParam, 
          dirResults, "greedyExtension-looseComplement-filterFreqLow", overrideResults);
      // Corresponds to call below
      // this.run(0.001, 1.2, 0.9, 0.05, 5, 3, 0.2f, 0.01f, 0.02f, "greedyExtension-looseComplement-filterFreqLow"); 
      

      //////////////////////////////
      // Final Grid
      //////////////////////////////
      // SOME More
      //this.run(0.001, 1.2, 0.9, 0.05, 5, 3, 0.1f, 0.015f, 0.05f, "greedyExtension-strictComplement-filterFreqMed");
      //this.run(0.001, 1.2, 0.9, 0.05, 5, 3, 0.2f, 0.015f, 0.05f, "greedyExtension-looseComplement-filterFreqMed"); 
      //this.run(0.001, 1.2, 0.9, 0.05, 5, 3, 0.1f, 0.02f, 0.1f, "greedyExtension-strictComplement-filterFreqHigh");
      //this.run(0.001, 1.2, 0.9, 0.05, 5, 3, 0.2f, 0.02f, 0.1f, "greedyExtension-looseComplement-filterFreqHigh"); 
      //this.run(0.001, 1.2, 0.9, 0.05, 5, 3, 0.1f, 0.05f, 0.1f, "greedyExtension-strictComplement-filterFreqHigh2");
      //this.run(0.001, 1.2, 0.9, 0.05, 5, 3, 0.2f, 0.05f, 0.1f, "greedyExtension-looseComplement-filterFreqHigh2"); 
      
      //this.run(0.001, 1.2, 0.9, 0.15, 5, 3, 0.1f, 0.015f, 0.05f, "breakExtension-strictComplement-filterFreqMed");
      //this.run(0.001, 1.2, 0.9, 0.15, 5, 3, 0.2f, 0.015f, 0.05f, "breakExtension-looseComplement-filterFreqMed"); 
      //this.run(0.001, 1.2, 0.9, 0.15, 5, 3, 0.1f, 0.02f, 0.1f, "breakExtension-strictComplement-filterFreqHigh");
      //this.run(0.001, 1.2, 0.9, 0.15, 5, 3, 0.2f, 0.02f, 0.1f, "breakExtension-looseComplement-filterFreqHigh"); 
      //this.run(0.001, 1.2, 0.9, 0.15, 5, 3, 0.1f, 0.04f, 0.1f, "breakExtension-strictComplement-filterFreqHigh2");
      //this.run(0.001, 1.2, 0.9, 0.15, 5, 3, 0.2f, 0.04f, 0.1f, "breakExtension-looseComplement-filterFreqHigh2"); 
      
      //////////////////////////////
      // Old Runs
      //////////////////////////////
      //this.run(0.005, 1.2, 0.9, 0.05, 3, 3, 0.1f, "1");
      // Test a a few IVF parameters
      //this.run(0.001, 1.2, 0.9, 0.05, 3, 3, 0.1f, "2");
      //this.run(0.001, 1.1, 0.9, 0.05, 3, 3, 0.1f, "3");
      //this.run(0.001, 1.2, 0.8, 0.05, 3, 3, 0.1f, "4");
      //this.run(0.001, 1.1, 0.9, 0.01, 3, 3, 0.1f, "5");
      //this.run(0.001, 1.1, 0.9, 0.1, 3, 3, 0.1f, "6");
      //this.run(0.001, 1.1, 0.9, 0.2, 3, 3, 0.1f, "7");
      // Complementary - Overlap
      //this.run(0.001, 1.2, 0.9, 0.05, 3, 3, 0.025f, "8");
      //this.run(0.001, 1.2, 0.9, 0.05, 3, 3, 0.2f, "9-filter-freq"); // Those I liked the most very similar to 0
      //this.run(0.001, 1.2, 0.9, 0.05, 3, 3, 0.4f, "10");
      // Complementary - Number
      //this.run(0.001, 1.2, 0.9, 0.05, 3, 5, 0.4f, "11");
      //this.run(0.001, 1.2, 0.9, 0.05, 5, 3, 0.4f, "12");
    } catch (FileAlreadyExistsException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (HFDDIterationManagementBuildingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


}
