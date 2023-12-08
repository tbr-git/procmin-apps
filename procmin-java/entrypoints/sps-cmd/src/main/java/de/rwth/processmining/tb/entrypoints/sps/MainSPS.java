package de.rwth.processmining.tb.entrypoints.sps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuildingException;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.DataToPDFG;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.PTDFGVisualizationMetaParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSAutoPDFGExtractionMetaParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSComplementaryDiffParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSGraphMiningMetaParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSIVFMetaParam;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class MainSPS {

  public static void main(String[] args) throws FileNotFoundException, HFDDIterationManagementBuildingException, IOException {
    SPSGraphMiningMetaParam spsGraphMiningDefaultLookup = new SPSGraphMiningMetaParam();
    SPSIVFMetaParam ivfDefaultLookup = new SPSIVFMetaParam();
    SPSComplementaryDiffParam compDiffDefaultLookup = new SPSComplementaryDiffParam();
    SPSAutoPDFGExtractionMetaParam autoDiagDefaultLookup = new SPSAutoPDFGExtractionMetaParam();
    PTDFGVisualizationMetaParam ptdfgVisDefaultLookup = new PTDFGVisualizationMetaParam();
    
    ArgumentParser parser = ArgumentParsers.newFor("Run auto dignstostics").build();
    // Positional arguments: logs and result target
    parser.addArgument("logL").type(String.class).help("Path to the left log");
    parser.addArgument("logR").type(String.class).help("Path to the right log");
    parser.addArgument("baseDir").type(String.class).help(
        "Path base result directory. Directory must exist. "
        + "In this directory a subdirectory containing the results will be created ");
    parser.addArgument("resultDirName").type(String.class).help("Name of the result directory that will be created/used. "
        + "If exisits, content will be overridden");

    //////////////////////////////
    // SPS Graph Mining
    //////////////////////////////
    parser.addArgument("--spsVertices").type(Integer.class)
      .setDefault(spsGraphMiningDefaultLookup.getTargetNbrVertices())
      .help("SPS Graph: Target number of vertices");
    parser.addArgument("--spsMargin").type(Double.class)
      .setDefault(spsGraphMiningDefaultLookup.getTargetNbrMargin())
      .help("SPS Graph: Allowed margin around target number");
    parser.addArgument("--spsMaxTime").type(Integer.class)
      .setDefault(spsGraphMiningDefaultLookup.getMaxMiningTimeMs())
      .help("SPS Graph: Maximum discovery time");
    parser.addArgument("--spsMinBFSSupport").type(Double.class)
      .setDefault(spsGraphMiningDefaultLookup.getMinSupportBFSActivitySets())
      .help("SPS Graph: Minimum support used for BFS initialization");
    parser.addArgument("--spsThresholdBFSInit").type(Double.class)
      .setDefault(spsGraphMiningDefaultLookup.getThresholdPreferBFSOverComplete())
      .help("SPS Graph: Use BFS initialization if having complete itemsets requires higher support");

    //////////////////////////////
    // Interesting Vertex Finder
    //////////////////////////////
    parser.addArgument("--ivfTMetric").type(Double.class).setDefault(ivfDefaultLookup.getIvfTMetric())
      .help("Interesting vertex finder: Metric threshold - EMD must be at least this value");
    parser.addArgument("--ivfTPhi").type(Double.class).setDefault(ivfDefaultLookup.getIvfTPhi())
      .help("Interesting vertex finder: Phi threshold - Don't aggregate vertices that occur independently. "
          + "(Phi coefficient below this threshold)");
    parser.addArgument("--ivfTDom").type(Double.class).setDefault(ivfDefaultLookup.getIvfTDom())
      .help("Interesting vertex finder: Interesting, more specific vertex dominates less specific predecessor if "
          + "EMD similar up to this factor.");
    
    //////////////////////////////
    // Complementary Difference Finder
    //////////////////////////////
    parser.addArgument("--domJaccard").type(Float.class).setDefault(compDiffDefaultLookup.getUpperBoundCoOccJaccard())
      .help("Complementary Difference Retrieval: Two vertices are considered complementary if "
          + "co-occurence Jaccard index below this value");

    //////////////////////////////
    // Auto Extraction
    //////////////////////////////
    parser.addArgument("--metaDiagNbr").type(Integer.class).setDefault(autoDiagDefaultLookup.getNbrMetaDiagnostics())
      .help("Auto Diagnostics: Target number of PT-DFGs");
    parser.addArgument("--metaDiagSize").type(Integer.class).setDefault(autoDiagDefaultLookup.getCompDiffSetSize())
      .help("Auto Diagnostics: Target size of each complementary set");

    //////////////////////////////
    // PT-DFG Visualization
    //////////////////////////////
    parser.addArgument("--ptdfgEdgeFreq").type(Float.class).setDefault(ptdfgVisDefaultLookup.getFilterEdgeFrequency())
      .help("PT-DFG Visualization: Minium edge frequency (overruled by difference threshold");
    parser.addArgument("--ptdfgDiff").type(Float.class).setDefault(ptdfgVisDefaultLookup.getShowDifferencesAbove())
      .help("PT-DFG Visualization: Show edges if difference larger than this value");
    parser.addArgument("--ptdfgMinLW").type(Float.class).setDefault(ptdfgVisDefaultLookup.getMinLineWidth())
      .help("PT-DFG Visualization: Minimum line width.");
    parser.addArgument("--ptdfgMaxLW").type(Float.class).setDefault(ptdfgVisDefaultLookup.getMaxLineWidth())
      .help("PT-DFG Visualization: Maximum line width.");
    parser.addArgument("--ptdfgCScale").type(Float.class).setDefault(ptdfgVisDefaultLookup.getUpperRangeColorScale())
      .help("PT-DFG Visualization: Color scale normalizer r: [-1 * r, r].");

    Namespace resArgs;
    try {
      resArgs = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      return;
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Parse Arguments
    ////////////////////////////////////////////////////////////////////////////////
    Path pathLogL = Path.of(resArgs.getString("logL"));
    Path pathLogR = Path.of(resArgs.getString("logR"));
    Path pathResBaseDir = Path.of(resArgs.getString("baseDir"));
    String resultDirName = resArgs.getString("resultDirName");

    // SPS Graph Mining
    Integer spsVertices = resArgs.getInt("spsVertices");
    Double spsMargin = resArgs.getDouble("spsMargin");
    Integer spsMaxTime = resArgs.getInt("spsMaxTime");
    Double spsMinBFSSupport = resArgs.getDouble("spsMinBFSSupport");
    Double spsThresholdBFSInit = resArgs.getDouble("spsThresholdBFSInit");
    SPSGraphMiningMetaParam spsGraphMiningParam = new SPSGraphMiningMetaParam(spsVertices, spsMargin, 
        spsMaxTime, spsMinBFSSupport, spsThresholdBFSInit);


    // Interesting Vertex Finder
    Double ivfTMetric = resArgs.getDouble("ivfTMetric");
    Double ivfTPhi = resArgs.getDouble("ivfTPhi");
    Double ivfTDom = resArgs.getDouble("ivfTDom");
    SPSIVFMetaParam ivfParam = new SPSIVFMetaParam(ivfTMetric, ivfTPhi, ivfTDom);

    // Complementary Differences
    Float domJaccard = resArgs.getFloat("domJaccard");
    SPSComplementaryDiffParam compDiffParam = new SPSComplementaryDiffParam(domJaccard);

    // Auto Extraction
    Integer metaDiagNbr = resArgs.getInt("metaDiagNbr");
    Integer metaDiagSize = resArgs.getInt("metaDiagSize");
    SPSAutoPDFGExtractionMetaParam autoDiagParam = new SPSAutoPDFGExtractionMetaParam(metaDiagNbr, metaDiagSize);

    // PT-DFG Visualization
    Float ptdfgEdgeFreq = resArgs.getFloat("ptdfgEdgeFreq");
    Float ptdfgDiff = resArgs.getFloat("ptdfgDiff");
    Float ptdfgMinLW = resArgs.getFloat("ptdfgMinLW");
    Float ptdfgMaxLW = resArgs.getFloat("ptdfgMaxLW");
    Float ptdfgCScale = resArgs.getFloat("ptdfgCScale");
    PTDFGVisualizationMetaParam ptdfgVisParam = new PTDFGVisualizationMetaParam(ptdfgEdgeFreq, ptdfgDiff, 
        ptdfgCScale, ptdfgMinLW, ptdfgMaxLW);

    DataToPDFG dataToPDFG = new DataToPDFG(pathLogL, pathLogR, spsGraphMiningParam);
    
    dataToPDFG.runDifferenceExtraction(ivfParam, compDiffParam, autoDiagParam, 
        ptdfgVisParam, pathResBaseDir, resultDirName, true);
  }

}
