package de.rwth.processmining.tb.core.emd.solver.akp.helper;

import java.util.List;

/**
 * Container used to pass the initial tree to the solver.
 * @author brockhoff
 *
 */
public class InitTreeArrayStruct {

	//Tree representation
	/**
	 * Count sources
	 */
	public int cSrc;
	/**
	 * Count targets;
	 */
	public int cTar;
	/**
	 * Predecessor
	 */
	public int[] pre;
	/**
	 * Tree level
	 */
	public int[] level;
	/**
	 * Thread index (preorder)
	 */
	public int[] t;
	/**
	 * Final node in thread index (subtree)
	 */
	public int[] f;
	/**
	 * Number of nodes in subtree
	 */
	public int[] n;
	
	//Variables for the simplex-type algrithm
	/**
	 * Orientation of arc (p(i),i) -> true iff upward
	 */
	public boolean[] orient;
	
	/**
	 * Dual value of node
	 */
	public double[] dual;
	
	/**
	 * Flow value of arc (p(i),i)
	 */
	public double[] flow;
	
	/**
	 * List of deficit trees
	 */
	public List<Integer> defTrees;
	
	/**
	 * List of surplus trees
	 */
	public List<Integer> surTrees;
	
	/**
	 * Current dual objective function value
	 */
	public double curDual;
	
	/**
	 * Current primal objective function value (infeasible until termination)
	 */
	public double curPrimal;
	
	/**
	 * Cost matrix
	 */
	public double[][] costs;
	
	/**
	 * Flow to the artificial node
	 */
	public double surplusFlow;
		

	/**
	 * @param pre
	 * @param level
	 * @param t
	 * @param f
	 * @param n
	 * @param orient
	 * @param dual
	 * @param flow
	 * @param defTrees
	 * @param surTrees
	 */
	public InitTreeArrayStruct(int cSrc, int cTar, int[] pre, int[] level, int[] t, int[] f, int[] n, boolean[] orient, double[] dual,
			double[] flow, List<Integer> defTrees, List<Integer> surTrees, double curDual, double[][] costs) {
		super();
		this.cSrc = cSrc;
		this.cTar = cTar;
		this.pre = pre;
		this.level = level;
		this.t = t;
		this.f = f;
		this.n = n;
		this.orient = orient;
		this.dual = dual;
		this.flow = flow;
		this.defTrees = defTrees;
		this.surTrees = surTrees;
		this.curDual = curDual;
		this.costs = costs;
	}
	
	/**
	 * @param pre
	 * @param level
	 * @param t
	 * @param f
	 * @param n
	 * @param orient
	 * @param dual
	 * @param flow
	 * @param defTrees
	 * @param surTrees
	 * @param surplusFlow
	 */
	public InitTreeArrayStruct(int cSrc, int cTar, int[] pre, int[] level, int[] t, int[] f, int[] n, boolean[] orient, double[] dual,
			double[] flow, List<Integer> defTrees, List<Integer> surTrees, double curDual, double[][] costs, double surplusFlow) {
		super();
		this.cSrc = cSrc;
		this.cTar = cTar;
		this.pre = pre;
		this.level = level;
		this.t = t;
		this.f = f;
		this.n = n;
		this.orient = orient;
		this.dual = dual;
		this.flow = flow;
		this.defTrees = defTrees;
		this.surTrees = surTrees;
		this.curDual = curDual;
		this.costs = costs;
		this.surplusFlow = surplusFlow;
	}
	
	
	
	
}
