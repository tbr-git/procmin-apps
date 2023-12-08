package hfdd.evaluation.cdrift.taskcreation;

public enum CDPVALogType {
  PRE_DRIFT,    // Before a drift
  DRIFT,    // Drift between left and right
  POST_PRE_DRIFT, // Between two drifts (POST the first, PRE the other)
  POST_DRIFT,   // After all drifts, no next drift

}
