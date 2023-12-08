package hfdd.evaluation.cdrift.evaluators.plainemd;

import java.util.Optional;

import hfdd.evaluation.cdrift.taskcreation.CDPVALogType;

public record PVAOnCDTestResultPlainEMD(String logCollection, String cdLogName, String pvaTaskId, 
    CDPVALogType logType, Optional<Integer> previousRelatedCD, Optional<Integer> nextRelatedCD,
    String method, 
    long time, boolean crossCDComparison,
    double emd) {

  public String toCSVString() {
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
    builder.append(",");
    builder.append(emd);
    
    return builder.toString();
  }
  
  public static String getCSVHeader() {
    StringBuilder builder = new StringBuilder();
    builder.append("CDCollection,CDLog,PVATaskId,LogType,PrevRelatedDrift,NextRelatedDrift,Method,"
        + "Time,IsCrossDriftComparison,EMD");
    
    return builder.toString();
  }
}
