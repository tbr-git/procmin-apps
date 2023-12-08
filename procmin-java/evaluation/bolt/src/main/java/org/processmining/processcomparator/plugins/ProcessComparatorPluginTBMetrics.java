package org.processmining.processcomparator.plugins;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.processcomparator.help.YourHelp;

@Plugin(name = "Process Comparator Metric Evaluation", level = PluginLevel.Local, parameterLabels = {
    "Directory containing concept drift logs" }, returnLabels = {
        "Success" }, returnTypes = { Boolean.class }, help = YourHelp.TEXT)
public class ProcessComparatorPluginTBMetrics {

  // public static String strPathCDCollection =
  // "C:/temp/dataset/test/cdrift-evaluation";

  @PluginVariant(variantLabel = "Process Comparator Evaluation without UI, default parameters", requiredParameterLabels = {
      0 })
  public boolean runMetricEvaluation(PluginContext context, String strPathCDCollection) {
    ////////////////////////////////////////
    // NOTE
    // Just Keep this class to illustrate how to use gradle and the 
    // CLI context 
    ////////////////////////////////////////
    return true;

  }

}
