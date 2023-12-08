package hfdd.evaluation.pdfgdiscovery;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuildingException;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class PDFGMain {

  private final static Logger logger = LogManager.getLogger(PDFGMain.class);

  // Sepsis
  private final static String pathLogLSepsis = "C:/temp/dataset/Sepsis/SepsisYoungerThanEqual35.xes";
  private final static String pathLogRSepsis = "C:/temp/dataset/Sepsis/SepsisAtLeast70.xes";
  
  // RTFM
  private final static String pathLogLRTFM = "C:/temp/dataset/RTFM/RTFM_l_50-shortAct.xes";
  private final static String pathLogRRTFM = "C:/temp/dataset/RTFM/RTFM_ge_50-shortAct.xes";

  // RTFM - split on initial Fine
  private final static String pathLogLRTFMSplitCF = "C:/temp/dataset/RTFM/RTFM_l_50-shortAct.xes";
  private final static String pathLogRRTFMSplitCF = "C:/temp/dataset/RTFM/rtfm-CF_Amount_GE_50-shortAct.xes";
  

  public static void main(String[] args) throws FileNotFoundException, HFDDIterationManagementBuildingException, IOException {
    ArgumentParser parser = ArgumentParsers.newFor("Run auto dignstostics")
        .build();
    parser.addArgument("--sepsis").action(Arguments.storeTrue()).help("Run on Sepsis");
    parser.addArgument("--rtfm").action(Arguments.storeTrue()).help("Run on RTFM");
    parser.addArgument("--rtfmSplitCF").action(Arguments.storeTrue())
      .help("Run on RTFM; split on fine amount saved in Create Fine");
    
    Namespace resArgs;
    try {
      resArgs = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      return;
    }

    if (resArgs.getBoolean("sepsis")) {
      logger.info("Run on Sepsis");
      //Path dirResults = Path.of("C:/temp/HFDD/evalSepsis-final/");
      Path dirResults = Path.of("C:/temp/HFDD/test/");
      DataToPDFGGridSearch d2PDFG = new DataToPDFGGridSearch(Path.of(pathLogLSepsis), Path.of(pathLogRSepsis), dirResults);
      d2PDFG.run();
    }
    if (resArgs.getBoolean("rtfm")) {
      logger.info("Run on RTFM");
      //Path dirResults = Path.of("C:/temp/HFDD/evalRTFM-final/color-scaled/");
      Path dirResults = Path.of("C:/temp/HFDD/test/");
      DataToPDFGGridSearch d2PDFG = new DataToPDFGGridSearch(Path.of(pathLogLRTFM), Path.of(pathLogRRTFM), dirResults);
      d2PDFG.run();
    }
    if (resArgs.getBoolean("rtfmSplitCF")) {
      logger.info("Run on RTFM");
      //Path dirResults = Path.of("C:/temp/HFDD/evalRTFMSplitCF-final/color-scaled-adpat-IVF/");
      //Path dirResults = Path.of("C:/temp/HFDD/evalRTFMSplitCF-final/color-scaled-adapt-IVF-2/");
      Path dirResults = Path.of("C:/temp/HFDD/test/");
      DataToPDFGGridSearch d2PDFG = new DataToPDFGGridSearch(Path.of(pathLogLRTFMSplitCF), Path.of(pathLogRRTFMSplitCF), dirResults);
      d2PDFG.run();
    }
    
    logger.info("Done {}", PDFGMain.class.getName());
    
  }


}
