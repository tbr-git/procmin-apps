package de.rwth.processmining.tb.core.emd.solver.akp.core;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rwth.processmining.tb.core.emd.solutiondata.NonZeroFlows;
import de.rwth.processmining.tb.core.emd.solver.akp.graph.Tree;
import de.rwth.processmining.tb.core.emd.solver.akp.helper.TreeIterator;
import de.rwth.processmining.tb.core.emd.solver.akp.init.StructInitEPSA;



/**
 * A class which performs the AKP algorithm on an array representation of the tree.
 * Internally the tree (basic solution, not necessary feasible) is represented 
 * using several arrays. In each step a complete 
 * comparison between surplus sources and deficit
 * targets is made.
 * <p>
 * Papers:
 * <p>
 * <ul>
 * <li> Charalampos Papamanthou, Konstantinos Paparrizos, and Nikolaos Samaras. 2004. Computational experience with exterior point algorithms for the transportation problem. Appl. Math. Comput. 158, 2 (November 2004), 459-475. (AKP algorithm)
 * <li> M.S. Bazaraa, J.J. Jarvis, and H.D. Sherali. Linear Programming and Network Flows. Wiley, 2010, 482-488. (Tree)
 * </ul>
 * @author brockhoff
 */
public class AKPSolverArraySimple {
	
	private final static Logger logger = LogManager.getLogger( AKPSolverArraySimple.class );


	/**
	 * A handle to the internal tree representing the current 
	 * basic solution (in general neither primal nor dual feasible)
	 */
	private Tree tree;
	
	/**
	 * Count sources
	 */
	public int cSrc;
	/**
	 * Count targets;
	 */
	public int cTar;
	
	/**
	 * Iterator to iterate over subtrees for a given root
	 */
	private TreeIterator itTree;
	
	/**
	 * List of deficit trees
	 */
	private List<Integer> defTrees;
	
	/**
	 * List of surplus trees
	 */
	private List<Integer> surTrees;
	
	/**
	 * Current iterative step
	 */
	private int iteration;
	
	/**
	 * Reducing costs in the current step 
	 */
	double minRedC;
	
	/**
	 * If EMD has been calculated
	 */
	boolean isExact;
	
	/**
	 * Cost matrix (source,target)
	 */
	double[][] costs;
	
	/**
	 * Best source (in surplus tree) wrt reduced cost for targets (in deficit trees)
	 */
	int[] indMinRedCost4Target;
	
	boolean[] inSurTree;
	
	/**
	 * Min reduced cost Value for best source {@link AKPSolverArraySimple#indMinRedCost4Target}
	 */
	double[] costMinRedCost4Target;
	
	
	public AKPSolverArraySimple(StructInitEPSA inst) {
		super();
		this.tree = inst.tree;
		this.cSrc = inst.cSrc;
		this.cTar = inst.cTar;
		this.defTrees = inst.defTrees;
		this.surTrees = inst.surTrees;
		this.indMinRedCost4Target = new int[this.cSrc + this.cTar + 1];
		this.costMinRedCost4Target = new double[this.cSrc + this.cTar + 1];
		this.inSurTree = new boolean[this.cSrc + this.cTar + 1];
		
		// Initialize iterators
		itTree = tree.getFreshTreeIterator();
		
		iteration = 1;
		
		isExact = false;
		
		this.costs = inst.costs;
	}
	
    /**
     * Runs the simplex type algorithm.
     */
    public double solve()
    {
    	initInSurTreeArray();
    	initMinRedCostMemory();
    	int iterations = 1;
        //While further iterations are necessary
//    	logger.trace("Iteration " + iterations);
    	while (nextIteration()) {
    		iterations++;
//			logger.trace("Iteration " + iterations);
    	}
    	
        logger.trace("EPSA steps: " + iterations);
    	return tree.getCurDual();
    }
    

	public boolean nextIteration() {
    	int[] entering;
    	entering = getNextEntering();
    	
		//There is not edge between a surplus and a deficit tree -> Optimal solution found
    	if(entering == null) {
    		isExact = true;
			return false;
		}
		else {
			//Calculate new costs (adpat dual values)
        	//Update tree structure
        	int[] leaving = tree.enterEdgeNUpdate(entering, minRedC);

        	//A (direct) child of the artificial root node is cut off
        	//-> A surplus resp. deficit tree is cut off
        	if(leaving[0] == 0) {
        		if(tree.branchOfLeavingEdge)
        			surTrees.remove(Integer.valueOf(leaving[1]));
        		else
        			defTrees.remove(Integer.valueOf(leaving[1]));			
            }

			if(tree.branchOfLeavingEdge)
				updateMove2Deficit(entering[0]);
			else
				updateMove2Surplus(entering[1]);
			
        	//curStart = endTime;
            iteration++;
            return true;
        }

	}


	/**
     * Gets the next entering arc.
     *
     * @return
     */
    public int[] getNextEntering() {
    	int minSrc = -1, minTar = -1;
    	minRedC = Double.POSITIVE_INFINITY;
    	//Compare reducing costs between deficit targets and surplus sources
    	//All targets in a deficit tree
    	for(int tar = cSrc + 1; tar < cSrc + 1 + cTar; tar++) {
			//Skip surplus targets
			if(inSurTree[tar]) {
				continue;
			}
    			
			if(indMinRedCost4Target[tar] <= 0 || !inSurTree[indMinRedCost4Target[tar]]) {
				updateMinRedSrc(tar);
			}
			if(minRedC > costMinRedCost4Target[tar]) {
				minRedC = costMinRedCost4Target[tar];
				minSrc = indMinRedCost4Target[tar];
				minTar = tar;
			}
    	}
    	//If no edge is found
    	//-> No further iteration are mandatory
    	if(minSrc == -1 && minTar == -1)
    		return null;
    	else {
    		int[] res = {minSrc, minTar};
    		return res;
    	}
    }
    
    
	public void updateMove2Surplus(int root) {
		int n;
		itTree.setRoot(root);
		while(itTree.hasNext()) {
			n = itTree.next();
			if(n > cSrc) {
				indMinRedCost4Target[n] = -1;
				costMinRedCost4Target[n] = Double.POSITIVE_INFINITY;
				inSurTree[n] = true;
			}
		}
		itTree.setRoot(root);
		double redC;
		while(itTree.hasNext()) {
			n = itTree.next();
			if(n <= cSrc) {
				for(int tar = cSrc + 1; tar < cSrc + 1 + cTar; tar++) {
					if(inSurTree[tar])
						continue;
    				redC = tree.redCost(n, tar);
    				if(redC < costMinRedCost4Target[tar]) {
    					costMinRedCost4Target[tar] = redC;
    					indMinRedCost4Target[tar] = n;
    				}
				}
//				indMinRedCost4Target[n] = -1;
//				costMinRedCost4Target[n] = Double.POSITIVE_INFINITY;
				inSurTree[n] = true;
			}
		}
	}
	
	public void updateMove2Deficit(int root) {
		int n;
		itTree.setRoot(root);
		while(itTree.hasNext()) {
			n = itTree.next();
			if(n <= cSrc) {
				inSurTree[n] = false;
			}
		}
		itTree.setRoot(root);
		while(itTree.hasNext()) {
			n = itTree.next();
			if(n > cSrc) {
				updateMinRedSrc(n);
				inSurTree[n] = false;
			}
		}
	}
	
	public void initInSurTreeArray() {
    	for(Integer defRoot : defTrees) {
    		itTree.setRoot(defRoot);
    		while(itTree.hasNext()) {
    			inSurTree[itTree.next()] = false;
    		}
    	}
    	for(Integer surRoot : surTrees) {
    		itTree.setRoot(surRoot);
    		while(itTree.hasNext()) {
    			inSurTree[itTree.next()] = true;
    		}
    	}

		
	}
	
	/**
     * Gets the next entering arc.
     *
     * @return
     */
    public void initMinRedCostMemory() {
    	for(int tar = cSrc + 1; tar < cSrc + 1 + cTar; tar++) {
    		if(inSurTree[tar]) {
    			continue;
    		}
    		else {
    			updateMinRedSrc(tar);
    		}
    	}
    }

    
    public void updateMinRedSrc(int tar) {
    	double redC;
    	double localMinRedC = Double.POSITIVE_INFINITY;
		int localFrom = -1;
		for(int src = 1; src <= cSrc; src++) {
			if(!inSurTree[src]) {
				continue;
			}
			redC = tree.redCost(src, tar);
			if(redC < localMinRedC) {
				localMinRedC = redC;
				localFrom = src;
			}
		}
		indMinRedCost4Target[tar] = localFrom;
		costMinRedCost4Target[tar] = localMinRedC;
    }


	public boolean isExact() {
		return isExact;
	}
	
	public NonZeroFlows getNonZeroFlows() {
		NonZeroFlows.Builder builder = new NonZeroFlows.Builder();
		// Each tree edge corresponds to a certain flow (possibly 0) but some flow > 0 implies
		// there will be an edge in the tree
		// We cover all edges by investigating each node and its parent
		for (int n = 1; n < cSrc + cTar + 1; n++) {
			int parent = tree.getParent(n);
			// Ignore the artificial node (index 0) 
			if (parent > 0) {
				double flow = tree.getFlow(n);
				if(Double.compare(flow,  0) > 0) {
					if(n < parent) {
						builder.addNonZeroFlow(n - 1, parent - 1 - this.cSrc, flow);
					}
					else {
						builder.addNonZeroFlow(parent - 1, n - 1 - this.cSrc, flow);
					}
				}
			}
		}
		
		return builder.build();
	}

}

