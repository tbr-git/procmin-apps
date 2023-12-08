package org.processmining.spsevaluation.janusproxy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.cli.Options;

import minerful.JanusVariantAnalysisLauncher;
import minerful.JanusVariantAnalysisStarter;
import minerful.concept.TaskCharArchive;
import minerful.params.SystemCmdParameters;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.reactive.io.JanusVariantOutputManagementLauncher;
import minerful.reactive.params.JanusPrintParameters;
import minerful.reactive.params.JanusVariantCmdParameters;
import minerful.utils.MessagePrinter;

public class JanusProxy {

  public static void main(String[] args) {
    ////////////////////////////////////////
    // Copied from JanusVariantAnalysisStarter Entry Point
    // Minor adaptations (e.g., performance logging)
    ////////////////////////////////////////
    JanusVariantAnalysisStarter checkStarter = new JanusVariantAnalysisStarter();
    Options cmdLineOptions = checkStarter.setupOptions();

    SystemCmdParameters systemParams = new SystemCmdParameters(cmdLineOptions, args);
    PostProcessingCmdParameters preProcParams = new PostProcessingCmdParameters(cmdLineOptions, args);
    JanusPrintParameters janusViewParams = new JanusPrintParameters(cmdLineOptions, args);
    JanusVariantCmdParameters janusParams = new JanusVariantCmdParameters(cmdLineOptions, args);

    MessagePrinter.configureLogging(DebugLevel.debug);

    if (systemParams.help) {
      systemParams.printHelp(cmdLineOptions);
      System.exit(1);
    }

    // If one inspects the constructor, one sees that it load the logs
    long startLoadData = System.currentTimeMillis();
    JanusVariantAnalysisLauncher variantAnalysis = new JanusVariantAnalysisLauncher(janusParams, systemParams,
        preProcParams, janusViewParams);
    long timeLoadData = System.currentTimeMillis() - startLoadData;

    long startPVA = System.currentTimeMillis();
    Map<String, Float> result = variantAnalysis.checkVariants();
    long timePVA = System.currentTimeMillis() - startPVA;

    long startWriteResults = System.currentTimeMillis();
    TaskCharArchive alphabet = variantAnalysis.getAlphabetDecoder();
    new JanusVariantOutputManagementLauncher().manageVariantOutput(result, janusParams, janusViewParams, systemParams,
        alphabet, variantAnalysis.getMeasurementsSpecification1(), variantAnalysis.getMeasurementsSpecification2());
    long timeWriteResults = System.currentTimeMillis() - startWriteResults;

    ////////////////////////////////////////
    // Write Times
    ////////////////////////////////////////
    Path pathJanusRes = janusParams.outputCvsFile.toPath();
    String resultFileName = pathJanusRes.getFileName().toString().replace(".csv", "");
    Path pathJanusResTime = pathJanusRes.getParent().resolve(resultFileName + "-time.csv");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathJanusResTime.toFile(), false))) {
      System.out.println(janusParams.outputCvsFile);
      writer.write("TimeLoadData,TimePVA,TimeWriteResults\n");
      writer.write(Long.toString(timeLoadData));
      writer.write(",");
      writer.write(Long.toString(timePVA));
      writer.write(",");
      writer.write(Long.toString(timeWriteResults));
      writer.write("\n");
    } catch (IOException e) {
      System.out.println("Why does the file not exisit");
      // Ignore
    }
  }

}
