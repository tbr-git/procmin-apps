package hfdd.evaluation.parametergs;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import hfdd.evaluation.parametergs.IVFParameterGridSearch.SingleEvaluationTask;

class IVFGSTest {

  @Test
  void testEvaluationRun() throws Exception {
    String pathLogL = "C:/temp/dataset/RTFM/RTFM_l_50-shortAct.xes";
    String pathLogR = "C:/temp/dataset/RTFM/RTFM_ge_50-shortAct.xes";

    IVFParameterGridSearch gs = new IVFParameterGridSearch();
    gs.setupData(pathLogL, pathLogR);
    gs.setupDefault();
    
    Set<Integer> candidateIdsTop10Default = gs.getCandidateIdsTop10Default();

    ////////////////////
    // Compare with DEFAULT Config
    // -> Perfect similarity
    ////////////////////
    IVFGSTaskSpec taskSpecDefault = new IVFGSTaskSpec(0, 0.005, 0.95, 0.05);
    SingleEvaluationTask evalTaskDefault = gs.new SingleEvaluationTask(candidateIdsTop10Default, taskSpecDefault); 
    IVFResultGS resTaskDefault = evalTaskDefault.call();
    
    assertNotNull(resTaskDefault);
    assertEquals(1, resTaskDefault.jaccardDefault());
    assertEquals(1, resTaskDefault.overlapCoefficient());
    
    IVFGSTaskSpec taskSpec2 = new IVFGSTaskSpec(0, 0.01, 0.8, 0.1);
    SingleEvaluationTask evalTask = gs.new SingleEvaluationTask(candidateIdsTop10Default, taskSpec2); 
    
    IVFResultGS res = evalTask.call();
    assertNotNull(res);
    System.out.println(res);
  }

}
