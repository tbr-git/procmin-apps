package hfdd.evaluation.cdrift.taskcreation;

import java.util.Optional;

import org.deckfour.xes.model.XLog;

public record PVAOnCDTestSpec(String collectionName, String identifierOrigLog, String idExtraction, 
    CDPVALogType logType, Optional<Integer> previousRelatedCD, Optional<Integer> nextRelatedCD,
    XLog logL, XLog logR, 
    boolean driftInducedDifference) {
  
  public static String getCSVHeader() {
    return "CDCollection,CDLog,idExtractionTask,LogType,PrevRelatedDrift,NextRelatedDrift,IsCrossDriftComparison";
  }
  
  public String toCSVString() {
    StringBuilder builder = new StringBuilder();
    builder.append(collectionName);
    builder.append(",");
    builder.append(identifierOrigLog);
    builder.append(",");
    builder.append(idExtraction);
    builder.append(",");
    builder.append(logType);
    builder.append(",");
    builder.append(previousRelatedCD.orElse(-1));
    builder.append(",");
    builder.append(nextRelatedCD.orElse(-1));
    builder.append(",");
    builder.append(driftInducedDifference);

    return builder.toString();
    
  }

}
