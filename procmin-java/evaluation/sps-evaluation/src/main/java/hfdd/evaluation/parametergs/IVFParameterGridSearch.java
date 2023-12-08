package hfdd.evaluation.parametergs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;

import com.google.common.math.Quantiles;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagement;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuilderTimeout;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuildingException;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.DiffCandidate;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.IVFilterIndepentDiff;
import de.rwth.processmining.tb.core.util.LogLoader;

public class IVFParameterGridSearch {
  
	private final static Logger logger = LogManager.getLogger( IVFParameterGridSearch.class );
	
	private final static int ITERATION = 0;

  /**
   * Iteration Management
   */
  private HFDDIterationManagement hfddItMan;
  
  private Set<Integer> candidateIdsTop10Default;
  
  private Comparator<DiffCandidate> sortByEMD;
  
  private PerspectiveDescriptor pDesc;

  /**
   * Iteration Management
   */
  private static final int NBR_TARGET_ITEMSETS = 10_000;
  
  public IVFParameterGridSearch() {
    hfddItMan = null;

  }
  
  public void setupData(String pathLogL, String pathLogR) throws HFDDIterationManagementBuildingException, 
      FileNotFoundException, IOException {
    logger.info("Running data setup");
    logger.info("Loading logs");
    XLog logL = LogLoader.loadLog(pathLogL);
    XLog logR = LogLoader.loadLog(pathLogR);
    
		XEventClassifier classifier = XLogInfoImpl.STANDARD_CLASSIFIER;
		
    logger.info("Instantiating HFDD iteraiton management");
		// Create the iteration management instance
		HFDDIterationManagementBuilderTimeout builder = new HFDDIterationManagementBuilderTimeout();
		builder.setTargetItemsetNumber(NBR_TARGET_ITEMSETS);
		builder.setClassifier(classifier).setXlogL(logL).setXlogR(logR);
		hfddItMan = builder.build();
		
    pDesc = hfddItMan.getPerspective4Iteration(ITERATION);
    Function<DiffCandidate, Double> sortKeyExtractor = 
        c -> c.v().getVertexInfo().getMeasurements().get(pDesc).getMetric().get(); 
    sortByEMD = Comparator.comparing(sortKeyExtractor).reversed();
		
    logger.info("Setup data done");
  }
  
  public void setupDefault() {
    logger.info("Setup default");
    double tMetricDefault = 0.005;
    double tDomDefault = 0.95;
    double tPhiDefault = 0.05;
    IVFilterIndepentDiff finderDefault = new IVFilterIndepentDiff(hfddItMan, ITERATION, 
        tMetricDefault, tDomDefault, tPhiDefault);
    
    final Collection<DiffCandidate> candidatesDefault = finderDefault.findInterestingVertices();

    candidateIdsTop10Default = candidatesDefault.stream()
        .sorted(sortByEMD)
        .limit(10)
        .mapToInt(c -> c.v().getId())
        .boxed()
        .collect(Collectors.toSet());
  }
  
  public void runEvaluation(String pathResultFile) {
    
    logger.info("Running evaluation");
    double[] paramsMetric = new double[] {0.005, 0.01, 0.015, 0.025, 0.04, 0.065, 0.105, 0.17, 0.275};
    double[] paramsDom = new double[] {0.6, 0.7, 0.8, 0.85, 0.9, 0.95, 0.99};
    double[] paramsPhi = new double[] {0.001, 0.005, 0.01, 0.05, 0.1, 0.15, 0.2, 0.3, 0.4, 0.5};

    //double[] paramsMetric = new double[] {0.005, 0.01};
    //double[] paramsSurprise = new double[] {1.05};
    //double[] paramsDom = new double[] {0.6};
    //double[] paramsPhi = new double[] {0.001};
    
    ExecutorService service = Executors.newVirtualThreadPerTaskExecutor(); 
    List<Future<IVFResultGS>> futuresResults = new LinkedList<>();
    int i = 0;
    // Metric loop
    for (double tMetric : paramsMetric) {
      // Domination loop
      for (double tDom : paramsDom) {
        // Phi (independence) loop
        for (double tPhi : paramsPhi) {
          IVFGSTaskSpec taskSpec = new IVFGSTaskSpec(i, tMetric, tDom, tPhi);
          Future<IVFResultGS> f = service.submit(new SingleEvaluationTask(candidateIdsTop10Default, taskSpec));
          futuresResults.add(f);
          i++;
        }
      }
    }
    
    ////////////////////
    // Wait for task Termination
    ////////////////////
    try {
        for (var f: futuresResults) {
          f.get();
        }
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    ////////////////////
    // Write Results
    ////////////////////
    logger.info("Writing results");
    File file = new File(pathResultFile);
    FileWriter fr = null;
    try {
      fr = new FileWriter(file);
      fr.write("taskId,tMetric,tDom,tPhi,nbrDifferences,itemsetSizeQ25,itemsetSizeQ50,itemsetSizeQ75,"
          + "top10EMDQ50,top10ItemsetSizeQ50,jaccardDefault,overlapCoefficient\n");
      for (var f : futuresResults) {
        try {
          IVFResultGS res = f.get();
          StringBuilder builder = new StringBuilder();
          builder.append(res.taskSpec().taskId());
          builder.append(",");
          builder.append(res.taskSpec().tMetric());
          builder.append(",");
          builder.append(res.taskSpec().tDom());
          builder.append(",");
          builder.append(res.taskSpec().tPhi());
          builder.append(",");
          builder.append(res.nbrDifferences());
          builder.append(",");
          builder.append(res.itemsetSizeQ25());
          builder.append(",");
          builder.append(res.itemsetSizeQ50());
          builder.append(",");
          builder.append(res.itemsetSizeQ75());
          builder.append(",");
          builder.append(res.top10EMDQ50());
          builder.append(",");
          builder.append(res.top10ItemsetSizeQ50());
          builder.append(",");
          builder.append(res.jaccardDefault());
          builder.append(",");
          builder.append(res.overlapCoefficient());
          builder.append("\n");
          
          fr.write(builder.toString());

        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }finally{
      //close resources
      try {
        fr.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    logger.info("Done writing results");
  }
  
  public class SingleEvaluationTask implements Callable<IVFResultGS> {

    private final IVFGSTaskSpec taskSpec;

    private final Set<Integer> candidateIdsDefaultTop10;
    
    public SingleEvaluationTask(Set<Integer> candidateIdsDefaultTop10, 
        IVFGSTaskSpec taskSpec) {
      super();
      this.taskSpec = taskSpec;
      this.candidateIdsDefaultTop10 = candidateIdsDefaultTop10;
    }

    @Override
    public IVFResultGS call() throws Exception {
      
      // Find Differences
      IVFilterIndepentDiff finder = new IVFilterIndepentDiff(hfddItMan, ITERATION, 
          taskSpec.tMetric(), taskSpec.tDom(), taskSpec.tPhi());
      final Collection<DiffCandidate> candidates = finder.findInterestingVertices();

      ////////////////////
      // Statistics on complete set
      ////////////////////
      Collection<Integer> itemsetSizes = candidates.stream()
          .mapToInt(c -> c.v().getVertexInfo().getActivities().cardinality())
          .boxed().toList();
      Map<Integer, Double> fullQuantiles = 
          Quantiles.percentiles().indexes(25, 50, 75).compute(itemsetSizes);


      ////////////////////
      // Statistics on top 10 (EMD)
      ////////////////////
      List<DiffCandidate> candidatesTop10EMD  = candidates.stream()
          .sorted(sortByEMD)
          .limit(10)
          .toList();
      
      double top10MedianSize = Quantiles.median().compute(
          candidatesTop10EMD.stream()
            .mapToInt(c -> c.v().getVertexInfo().getActivities().cardinality())
            .boxed().toList()
          );

      double top10MedianEMD = Quantiles.median().compute(
          candidatesTop10EMD.stream()
            .mapToDouble(c -> c.v().getVertexInfo().getMeasurements().get(pDesc).getMetric().get())
            .boxed().toList()
          );
      
      // Jaccard
      Set<Integer> candidateIdsTop10 = candidatesTop10EMD.stream()
          .mapToInt(c -> c.v().getId())
          .boxed()
          .collect(Collectors.toSet());
      
      int nbrCandidatesTop10 = candidateIdsTop10.size();
      int nbrCandidatesDefault = candidateIdsDefaultTop10.size();

      candidateIdsTop10.retainAll(candidateIdsDefaultTop10);
      int intersection = candidateIdsTop10.size();
      
      double jaccard = ((double) intersection) / (nbrCandidatesTop10 + nbrCandidatesDefault - intersection);
      
      double overlapCoefficient = ((double) intersection) / Math.min(nbrCandidatesTop10, nbrCandidatesDefault);
      
      return new IVFResultGS(taskSpec, candidates.size(), 
          fullQuantiles.get(25), fullQuantiles.get(50), fullQuantiles.get(75),
          top10MedianEMD, top10MedianSize, jaccard, overlapCoefficient);
    }
  }

  protected Set<Integer> getCandidateIdsTop10Default() {
    return candidateIdsTop10Default;
  }

  protected HFDDIterationManagement getHfddItMan() {
    return hfddItMan;
  }
    
}
