package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot2Image.Engine;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.Streams;

import de.rwth.processmining.tb.core.data.statistics.ActivityOccurencePosition;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagement;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuilderTimeout;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuildingException;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.AutoIterationDiagnosticsExtractor;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.CandidatesMetaDiagnostic;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.MetaDifferenceVisContainer;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.PTDFGVisualizationMetaParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSAutoPDFGExtractionMetaParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSComplementaryDiffParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSGraphMiningMetaParam;
import de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery.cmdmetaparam.SPSIVFMetaParam;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraphBuilderTimeLimited;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.PDFGFilterEdgeFrequency;
import de.rwth.processmining.tb.core.sps.data.itemsetdiffgraph.ProbDiffDFG;
import de.rwth.processmining.tb.core.util.LogLoader;

public class DataToPDFG {

  private final static Logger logger = LogManager.getLogger(DataToPDFG.class);

  /**
   * Path to the left log
   */
  private final Path pathLogL;

  /**
   * Path to the right log
   */
  private final Path pathLogR;

  /**
   * Handle to the iteration management
   */
  private HFDDIterationManagement hfddItMan;

  private ObjectMapper metaDiagnosticMapper;

  /**
   * Constructor
   * 
   * @param pathLogL Path to left log
   * @param pathLogR Path to right log
   * @throws IOException
   * @throws HFDDIterationManagementBuildingException
   * @throws FileNotFoundException
   */
  public DataToPDFG(Path pathLogL, Path pathLogR, SPSGraphMiningMetaParam initSpsGraphMiningParam)
      throws FileNotFoundException, HFDDIterationManagementBuildingException, IOException {
    super();
    this.pathLogL = pathLogL;
    this.pathLogR = pathLogR;
    this.hfddItMan = setupIterationManagement(pathLogL, pathLogR, initSpsGraphMiningParam);

    metaDiagnosticMapper = new ObjectMapper();
    SimpleModule module = new SimpleModule("ProbDiffDFGMetaDiagnosticSerializeModule",
        new Version(1, 0, 0, null, null, null));
    module.addSerializer(PDFGMetaDiagnostic.class, new PDFGMetaDiagnosticSerializer());
    metaDiagnosticMapper.registerModule(module);
    metaDiagnosticMapper.registerModule(new Jdk8Module());
  }

  /**
   * Setup a new SPS Graph for the comparison. After this rather expensive
   * operation, multiple difference mining runs can be executed on this graph.
   * 
   * @throws FileNotFoundException
   * @throws HFDDIterationManagementBuildingException
   * @throws IOException
   */
  public void resetSPSGraph(SPSGraphMiningMetaParam spsGraphMiningParam)
      throws FileNotFoundException, HFDDIterationManagementBuildingException, IOException {
    this.hfddItMan = setupIterationManagement(pathLogL, pathLogR, spsGraphMiningParam);
  }

  /**
   * 
   * @param ivfParam         Interesting vertex finder parameters
   * @param spsCompDiffParam Complementary difference discovery parameters
   * @param autoExtractParam Auto extraction parameters
   * @param ptDFGVisParam    PT-DFG Visualization parameters
   * @param baseDir          Directory where a directory for the discovered graphs
   *                         will be created (if discovery successful)
   * @param graphDirName     Name of the directory holding the resulting graphs
   *                         (in case extraction was successful)
   * @param overrideGraphs   Override graph directory if it already exists
   * @throws IOException Problem writing the graph results
   */
  public void runDifferenceExtraction(SPSIVFMetaParam ivfParam, SPSComplementaryDiffParam spsCompDiffParam,
      SPSAutoPDFGExtractionMetaParam autoExtractParam, PTDFGVisualizationMetaParam ptDFGVisParam, Path baseDir,
      String graphDirName, boolean overrideGraphs) throws IOException {
    // Existing iteration management
    if (this.hfddItMan == null) {
      throw new IllegalStateException("No SPS graph.");
    }

    // Check base result directory
    if (!Files.exists(baseDir)) {
      throw new FileNotFoundException("Base directory for graphs does not exist!");
    }

    // Don't even start if overriding is disabled and graph directory exists
    if (!overrideGraphs && Files.exists(baseDir.resolve(graphDirName))) {
      throw new FileAlreadyExistsException(
          String.format("The graph directors %s already exists and overriding is disabled", graphDirName));
    }

    //////////////////////////////
    // Apply Automatic Discovery
    //////////////////////////////
    List<PDFGMetaDiagnostic> lProbDFGs = this.applyAutoExtractor(ivfParam, spsCompDiffParam, autoExtractParam);

    //////////////////////////////
    // Filter Graphs
    //////////////////////////////
    // PDFGFilterEdgeIM filter = new PDFGFilterEdgeIM(0.1f);
    PDFGFilterEdgeFrequency filter = new PDFGFilterEdgeFrequency(ptDFGVisParam.getShowDifferencesAbove(),
        ptDFGVisParam.getFilterEdgeFrequency());
    lProbDFGs = lProbDFGs.stream()
        .map(d -> new PDFGMetaDiagnostic(d.diffCandidates(),
            new MetaDifferenceVisContainer(filter.apply(d.pvaVisualizations().probDiffDFGTotal()),
                filter.apply(d.pvaVisualizations().probDiffDFGNormalized()))))
        .toList();

    ////////////////////////////////////////
    // Write Results
    ////////////////////////////////////////
    // Create folder for run id
    Path dirGraphs = baseDir.resolve(graphDirName);
    if (!Files.exists(dirGraphs)) {
      Files.createDirectory(dirGraphs);
    }

    ////////////////////
    // Write Discovery Info
    ////////////////////
    BufferedWriter writerRunInfo = new BufferedWriter(new FileWriter(dirGraphs.resolve("info.csv").toFile()));
    // Write Run Info Header
    writerRunInfo.write("tMetric,tPhi,tDom,jaccardUpper,nbrMetaDiagnostics,compDiffSetSize");
    writerRunInfo.write("\n");
    // Write Parameter
    writerRunInfo.write(Double.toString(ivfParam.getIvfTMetric()));
    writerRunInfo.write(",");
    writerRunInfo.write(Double.toString(ivfParam.getIvfTPhi()));
    writerRunInfo.write(",");
    writerRunInfo.write(Double.toString(ivfParam.getIvfTDom()));
    writerRunInfo.write(",");
    writerRunInfo.write(Float.toString(spsCompDiffParam.getUpperBoundCoOccJaccard()));
    writerRunInfo.write(",");
    writerRunInfo.write(Integer.toString(autoExtractParam.getNbrMetaDiagnostics()));
    writerRunInfo.write(",");
    writerRunInfo.write(Integer.toString(autoExtractParam.getCompDiffSetSize()));
    writerRunInfo.close();

    //////////////////////////////
    // Write Meta Diagnostics
    //////////////////////////////
    int i = 0;
    for (PDFGMetaDiagnostic diagnostic : lProbDFGs) {

      saveProbDiffDFG(dirGraphs, diagnostic.diffCandidates(), diagnostic.pvaVisualizations().probDiffDFGTotal(),
          "pdfg-" + i + "-total", ptDFGVisParam);
      saveProbDiffDFG(dirGraphs, diagnostic.diffCandidates(), diagnostic.pvaVisualizations().probDiffDFGNormalized(),
          "pdfg-" + i + "-normalized", ptDFGVisParam);
      // Provide information document containing the vertices
      ObjectWriter writer = metaDiagnosticMapper.writer(new DefaultPrettyPrinter());
      try {
        writer.writeValue(dirGraphs.resolve("vertices-" + i + ".json").toFile(), diagnostic);
      } catch (Exception e) {
        e.printStackTrace();
      }
      i++;
    }

    logger.info("Completed the Data to PDFG pipeline with graph directory {}", graphDirName);
  }

  private void saveProbDiffDFG(Path dirRunResults, CandidatesMetaDiagnostic context, ProbDiffDFG pdfg, String pdfgName,
      PTDFGVisualizationMetaParam ptDFGVisParam) {
    // Dot to SVG/PNG
    PDFG2Dot converter = new PDFG2Dot(ptDFGVisParam.getMinLineWidth(), ptDFGVisParam.getMaxLineWidth(),
        ptDFGVisParam.getUpperRangeColorScale());
    ActivityOccurencePosition actOccPos = this.hfddItMan.getDataQueryEngine().getAvgFirstActivityOccurrences();
    Dot dot = converter.pdfg2Dot(context, pdfg, actOccPos != null ? Optional.of(actOccPos) : Optional.empty());

    InputStream plottedPDFGsvg = MyDot2Image.dot2imageInputStream(dot.toString(), Type.svg, Engine.dot,
        Optional.empty());
    InputStream plottedPDFGpng = MyDot2Image.dot2imageInputStream(dot.toString(), Type.png, Engine.dot,
        Optional.empty());

    // Write PDFGs
    Path pathResSvg = dirRunResults.resolve(pdfgName + ".svg");
    Path pathResPng = dirRunResults.resolve(pdfgName + ".png");
    try {
      Files.copy(plottedPDFGsvg, pathResSvg, StandardCopyOption.REPLACE_EXISTING);
      Files.copy(plottedPDFGpng, pathResPng, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param ivfParam         Interesting vertex finder parameters
   * @param spsCompDiffParam Complementary difference discovery parameters
   * @param autoExtractParam Auto extraction parameters
   * @param ptDFGVisParam    PT-DFG Visualization parameters
   * @return List of Probability-extended DFGs
   */
  private List<PDFGMetaDiagnostic> applyAutoExtractor(SPSIVFMetaParam ivfParam,
      SPSComplementaryDiffParam spsCompDiffParam, SPSAutoPDFGExtractionMetaParam autoExtractParam) {

    // 1. Setup - Auto-Extractor
    PerspectiveDescriptor pDesc = hfddItMan.getPerspective4Iteration(0);
    AutoIterationDiagnosticsExtractor autoExtractor = new AutoIterationDiagnosticsExtractor(hfddItMan, 0,
        autoExtractParam.getNbrMetaDiagnostics(), // Auto extract
        ivfParam.getIvfTMetric(), ivfParam.getIvfTDom(), ivfParam.getIvfTPhi(), // IVF
        autoExtractParam.getCompDiffSetSize(), // Auto extract
        spsCompDiffParam.getUpperBoundCoOccJaccard()); // Complementary

    // 2. Discovery - Meta Vertex Sets
    List<CandidatesMetaDiagnostic> metaDiagnostics = autoExtractor.getDiagnostics();

    // 3. PDFGs
    List<MetaDifferenceVisContainer> metaDiffVisualizations = autoExtractor.getDiagnosticDFGs(metaDiagnostics);

    return Streams
        .zip(metaDiagnostics.stream(), metaDiffVisualizations.stream(), (c, dfg) -> new PDFGMetaDiagnostic(c, dfg))
        .toList();
  }

  private HFDDIterationManagement setupIterationManagement(Path logLeft, Path logRight,
      SPSGraphMiningMetaParam spsGraphMiningParam)
      throws HFDDIterationManagementBuildingException, FileNotFoundException, IOException {
    logger.info("Loading logs for the test...");
    XLog logL = null, logR = null;
    logger.debug("Loading the left log...");
    logL = LogLoader.loadLog(logLeft.toFile());
    logger.debug("Loading the right log...");
    logR = LogLoader.loadLog(logRight.toFile());
    logger.info("Done loading logs for the test.");

    // Graph Builder
    HFDDGraphBuilderTimeLimited spsGraphBuilder = new HFDDGraphBuilderTimeLimited();
    spsGraphBuilder.setFreqActMiningTimeMs(spsGraphMiningParam.getMaxMiningTimeMs())
        .setTargetActISMargin(spsGraphMiningParam.getTargetNbrMargin())
        .setTargetActISNbr(spsGraphMiningParam.getTargetNbrVertices())
        .setPreferBFSSupport(spsGraphMiningParam.getThresholdPreferBFSOverComplete())
        .setBFSInitMinSupport(spsGraphMiningParam.getMinSupportBFSActivitySets());

    // Create iteration management
    XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
    HFDDIterationManagementBuilderTimeout builder = new HFDDIterationManagementBuilderTimeout();
    builder.setSPSGraphBuilder(spsGraphBuilder);
    builder.setClassifier(eventClassifier).setXlogL(logL).setXlogR(logR);
    HFDDIterationManagement hfddItMan = builder.build();

    return hfddItMan;
  }

}
