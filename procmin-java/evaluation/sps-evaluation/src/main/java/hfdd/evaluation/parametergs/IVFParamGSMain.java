package hfdd.evaluation.parametergs;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuildingException;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class IVFParamGSMain {

  public static void main(String[] args) throws FileNotFoundException, HFDDIterationManagementBuildingException, IOException {
    ArgumentParser parser = ArgumentParsers.newFor("Interesting Vertex Finder Grid Serach")
        .build();
    parser.addArgument("--logl").type(String.class).help("Path to the left log file (.xes)");
    parser.addArgument("--logr").type(String.class).help("Path to the right log file (.xes)");
    parser.addArgument("--result").type(String.class).help("Path to the result file");
    
    Namespace res;
    try {
      res = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      return;
    }
    
    IVFParameterGridSearch gs = new IVFParameterGridSearch();
    gs.setupData(res.getString("logl"), res.getString("logr"));
    gs.setupDefault();
    gs.runEvaluation(res.getString("result"));

  }

}
