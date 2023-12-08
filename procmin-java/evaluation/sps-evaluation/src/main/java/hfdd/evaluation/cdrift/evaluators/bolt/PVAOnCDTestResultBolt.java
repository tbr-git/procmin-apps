package hfdd.evaluation.cdrift.evaluators.bolt;

import java.util.List;
import java.util.Optional;

import hfdd.evaluation.cdrift.taskcreation.CDPVALogType;

public record PVAOnCDTestResultBolt(String logCollection, String cdLogName, String pvaTaskId, 
    CDPVALogType logType, Optional<Integer> previousRelatedCD, Optional<Integer> nextRelatedCD,
    String method, 
    long time, boolean crossCDComparison,
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
    builder.append(time);
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
        + "Time,IsCrossDriftComparison");
    
    for (int i = 0; i < targetNbrScores; i++) {
      builder.append(",Score");
      builder.append(i);
    }
    return builder.toString();
  }
}
