package hfdd.evaluation.cdrift.evaluators;

import hfdd.evaluation.cdrift.taskcreation.PVAOnCDTestSpec;

public interface PVAOnCDEvaluator {
  
  /**
   * Called to initialize the evaluator.
   * @return True, iff initialization successful. 
   */
  public boolean init();
  
  /**
   * Apply the method on the evaluation task. 
   * In doing so, the evaluator is responsible of result bookkeeping.
   * (Since the output of the various method might differ a lot). 
   * 
   * @param testSpec Test specification
   * @return True iff evaluation run was successful
   */
  public boolean evaluateOnTask(PVAOnCDTestSpec testSpec) throws Exception;
  
  /**
   * Called after completing all evaluation tasks.
   * @return True, iff evaluation finalization successful. 
   */
  public boolean complete();
  
  /**
   * Name of the evaluator (should be unique)
   * @return
   */
  public String getNameUnique();

}
