package hfdd.evaluation.cdrift;

import java.util.List;
import java.util.Optional;

import hfdd.evaluation.cdrift.taskcreation.CDPVALogType;

public record PVAOnCDSPSTestResult(String logCollection, String cdLogName, String pvaTaskId, 
    CDPVALogType logType, Optional<Integer> previousRelatedCD, Optional<Integer> nextRelatedCD,
    String method, 
    long timeLoadData, long timeInitGraph, long timeMeasure, long timeVertexFinder,
    boolean crossCDComparison,
    List<Double> selectedTopScores) {
  
  public String toCSVString(int targetNbrScores) {
    StringBuilder builder = new StringBuilder();
    builder.append(logCollection);
    builder.append(",");
    builder.append(cdLogName);
    builder.append(",");
    builder.append(pvaTaskId);
    builder.append(",");
    builder.append(logType);
    builder.append(",");
    builder.append(previousRelatedCD.orElse(-1));
    builder.append(",");
    builder.append(nextRelatedCD.orElse(-1));
    builder.append(",");
    builder.append(method);
    builder.append(",");
    builder.append(timeLoadData);
    builder.append(",");
    builder.append(timeInitGraph);
    builder.append(",");
    builder.append(timeMeasure);
    builder.append(",");
    builder.append(timeVertexFinder);
    builder.append(",");
    builder.append(crossCDComparison);
    
    int nbrSelectedScores = selectedTopScores.size();

    selectedTopScores.stream()
      .limit(targetNbrScores)
      .forEach(c -> {
        builder.append(",");
        builder.append(c);
      });
    
    if (nbrSelectedScores < targetNbrScores) {
      builder.append(",".repeat(targetNbrScores - nbrSelectedScores));
    }
    
    return builder.toString();
  }
  
  public static String getCSVHeader(int targetNbrScores) {
    StringBuilder builder = new StringBuilder();
    builder.append("CDCollection,CDLog,PVATaskId,LogType,PrevRelatedDrift,NextRelatedDrift,Method,"
        + "TimeLoadData,TimeInitGraph,TimeMeasure,timeVertexFinder,"
        + "IsCrossDriftComparison");
    
    for (int i = 0; i < targetNbrScores; i++) {
      builder.append(",Score");
      builder.append(i);
    }
    return builder.toString();
  }

}
