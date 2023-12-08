package de.rwth.processmining.tb.core.emd.solver.akp.graph;

import de.rwth.processmining.tb.core.emd.solver.akp.helper.InitTreeArrayStruct;
import de.rwth.processmining.tb.core.emd.solver.akp.helper.TreeIterator;

/**
 * Tree representation of a basic solution according to
 * M.S. Bazaraa, J.J. Jarvis, and H.D. Sherali. Linear Programming and Network Flows. Wiley, 2010, 482-488.
 * @author brockhoff
 *
 */
public class Tree {

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
	private int[] pre;
	
	/**
	 * Tree level
	 */
	private int[] level;
	
	/**
	 * Thread index (preorder)
	 */
	private int[] t;
	
	/**
	 * Final node in thread index (subtree)
	 */
	private int[] f;
	
	/**
	 * Number of nodes in subtree
	 */
	private int[] n;
	
	/**
	 * Iterator to iterate over subtrees for a given root
	 */
	private TreeIterator itTree;
	
	//Variables for the simplex-type algorithm
	/**
	 * Orientation of arc (p(i),i) -> true iff upward
	 */
	private boolean[] orient;
	
	/**
	 * Dual value of node
	 */
	private double[] dual;
	
	/**
	 * Flow value of arc (p(i),i)
	 */
	private double[] flow;
	
	/**
	 * Current dual objective function value
	 */
	private double curDual;
	
	/**
	 * Cost matrix
	 */
	private double[][] costs;
	
	/**
	 * Forward edges
	 */
	private int[] forward;
	
	/**
	 * Backward edges
	 */
	private int[] backward;
	
	/**
	 * True if source is cut off, false else
	 */
	public boolean branchOfLeavingEdge;
	
    /**
     * Used when comparing weight to find leaving arc
     * -> Ensures that surplus tree is preferred in "draw" (hardly the same values) situation
     * "Real" improvement if improvement is greater than posEps
     */
    private static final double posEps = Math.pow(10, -6); //0.00000001;
    /**
     * Used when comparing weight to find leaving arc
     * -> Ensures that surplus tree is preferred in "draw" (hardly the same values) situation
     * Draw if improvement is in range of (negEps, posEps]
     */
    private static final double negEps = -1*posEps;
	
    /**
     * Temporary variables for sharing over function bounds
     */
	double minDelta;
	/**
	 * Parent node of the leaving arc
	 */
	int leavingChild;
	/**
	 * Number of forward edges
	 */
	int curForward = 0;
	/**
	 * Number of backward edges
	 */
	int curBackward = 0;
	
	/**
	 * Flow to the artificial node
	 */
	double surplusFlow;
	
	/**
	 * Build a tree from the given instance
	 * @param inst
	 */
	public Tree(InitTreeArrayStruct inst) {
		super();
		this.cSrc = inst.cSrc;
		this.cTar = inst.cTar;
		this.costs = inst.costs;
		this.pre = inst.pre;
		this.level = inst.level;
		this.t = inst.t;
		this.f = inst.f;
		this.n = inst.n;
		this.orient = inst.orient;
		this.dual = inst.dual;
		this.flow = inst.flow;
		this.curDual = inst.curDual;
		this.surplusFlow = inst.surplusFlow;
		
		int cNodes = this.cSrc + this.cTar;
		forward = new int[cNodes];
		backward = new int[cNodes];
		
		itTree = new TreeIterator(t, f);
	}
	
	
	/**
	 * Add the given edge with the given reducing costs to the tree
	 * and restore the the tree by choosing the appropriate edge
	 * (minimum flow in the cycle). 
	 * @param entering Entering edge (from, to)
	 * @param minRedC Reducing costs of the edge
	 * @return leaving edge (parent, child)
	 */
	public int[] enterEdgeNUpdate(int[] entering, double minRedC)
	{
		curForward = 0;
	    curBackward = 0;
	
	    //Minimum flow in the cycle
	    minDelta = Double.POSITIVE_INFINITY;
	    
	
	    //Identify the cycle and the minimum flow
	    identifyCycleAndFlow(entering[0], entering[1]);
	    
	    //Update surplus flow because it is reduced by minDelta in this iteration
	    surplusFlow -= minDelta;
	    
	    //Leaving arc
	    int[] leaving = {pre[leavingChild], leavingChild};
	    
	    //Update current dual value
	    curDual += minRedC * minDelta;
	
	    //Augment the minimum flow around the cycle
	    augment();
	    //Update the tree that is induced by removing the leaving
	    //edge without entering the entering one
	    updateTrPrime();
	
	    //Update the subtree that is cut off and re-root it
	    if (branchOfLeavingEdge)
	    	updateTv(entering[0]);
	    else
	       updateTv(entering[1]);
	    
	    
	    //Grafting the re-rooted tree in the given tree  
	    if(branchOfLeavingEdge) {
		    int x = f[entering[1]];
		    int y = t[x];
		    int z = f[entering[0]];
		    
		    t[x] = entering[0];
		    t[z] = y;
		    
		    int gam=pre[y];
		    int cur = entering[1];
		    while(cur != gam) {
		    	f[cur] = z;
		    	cur = pre[cur];
		    }
	    	orient[entering[0]] = true;
	    	flow[entering[0]] = minDelta;
	    	pre[entering[0]] = entering[1];
	    	//Update the dual variables and the depth
	    	updateDualNDepth(entering[0], minRedC);	    	
	    }
	    else {
		    int x = f[entering[0]];
		    int y = t[x];
		    int z = f[entering[1]];
		    
		    t[x] = entering[1];
		    t[z] = y;
		    
		    int gam=pre[y];
		    int cur = entering[0];
		    while(cur != gam) {
		    	f[cur] = z;
		    	cur = pre[cur];
		    }
		    
	    	orient[entering[1]] = false;
	    	pre[entering[1]] = entering[0];
	    	flow[entering[1]] = minDelta;
	    	updateDualNDepth(entering[1], minRedC);
	    }
	    
	    return leaving;
	}
	
	/**
	 * Identifies the cycle and the minimum flow around that cycle
	 * Edges of the cycle are stored.
	 * @param left left entering node
	 * @param right	right entering node
	 */
	public void identifyCycleAndFlow(int left, int right) {
		//Left edges are preferred to leave
		//Corresponds to the the subtree containing
		//the source node of the entering edge
		
		//Go up to same level
		while(level[left] < level[right]) {
			if(!orient[right]) {
				updateMinDelta(flow[right], right, false);
				backward[curBackward++] = right;
			}
			else
			{
				forward[curForward++] = right;
			}
			right = pre[right];
		}
		//Go up to same level
		while(level[right] < level[left]) {
			if(orient[left]) {
				updateMinDelta(flow[left], left, true);
				backward[curBackward++] = left;
			}
			else
			{
				forward[curForward++] = left;
			}
			left = pre[left];
		}
		//Go up on both side concurrently
		while(left != right) {
			if(orient[left]) {
				updateMinDelta(flow[left], left, true);
				backward[curBackward++] = left;
			}
			else
			{
				forward[curForward++] = left;
			}
			left = pre[left];
			
			if(!orient[right]) {
				updateMinDelta(flow[right], right, false);
				backward[curBackward++] = right;
			}
			else
			{
				forward[curForward++] = right;
			}
			right = pre[right];
			
		}
	}
		
	
	/**
	 * Updates the minimum value found by traversing the cycle
	 * @param delta Delta of the current edge
	 * @param root Node corresponding to the current edge (current edge is his tree edge)
	 * @param preferedLeaving true -> left, false -> right
	 */
	private void updateMinDelta(double delta, int root, boolean isPreferred)
	{
	    //Update if this is really less than the current OR
		//This is the preferred side of the tree
		if ((minDelta - delta > Tree.posEps) ||
	    		(minDelta - delta > Tree.negEps && isPreferred))
	    {
	        minDelta = delta;
	        leavingChild = root;
	        branchOfLeavingEdge = isPreferred;
	    }
	}
	
	/**
	 * Augments the current minDelta around the cycle
	 * that is stored in indentifyCylceAndFlow
	 */
	private void augment()
	{
	    for(int i = 0; i < curForward; i++) {
	    	flow[forward[i]] += minDelta;
	    }
	    for(int i = 0; i < curBackward; i++) {
	    	flow[backward[i]] -= minDelta;
	    }
	}
		

	/**
	 * Updates the tree when leaving subtree is simply cut off
	 * without entering the entering edge
	 */
	private void updateTrPrime() {
		int phi = pre[leavingChild];
		int u = pre[leavingChild];
		while(t[phi] != leavingChild) {
			phi=f[t[phi]];
		}
		
		t[phi] = t[f[leavingChild]];
		
		//Update n
		int cur = u;
	 	int nv = n[leavingChild];
		while(cur != -1) {
			n[cur] -= nv;
			cur = pre[cur];
		}
		int delta;
		if(f[u] == f[leavingChild]) {
			delta = phi;
		}
		else {
			delta = f[u];
		}
		int gam = pre[t[f[leavingChild]]];
		
		cur = u;
		while(cur != gam) {
			f[cur] = delta;
			cur = pre[cur];
		}
	}
	
	/**
	 * Re-roots the subtree that is cut off at the entering node
	 */
	private void updateTv(int q) {
		if(leavingChild == q) {
			return;
		}
			
		//t[f[q]] = pre[q];
		int lastStEl = q;
		int curStEl = pre[q];
		int curLast = f[q];
		int lastStSib = t[f[lastStEl]];
		int nextLast;
		while(curStEl != pre[leavingChild]) {
			int prevTree = curStEl;
			int curTree = t[curStEl];
			int nextTree = t[f[curTree]];
			while(curTree != lastStEl) {
				prevTree = curTree;
				curTree = nextTree;
				nextTree = t[f[curTree]];
			}
			nextTree = lastStSib;
			if(level[nextTree] > level[curStEl]) {
				lastStSib = t[f[curStEl]];
				if(prevTree == curStEl) {
					t[prevTree] = nextTree;
				}
				else {
					t[f[prevTree]] = nextTree;
				}
				nextLast = f[curStEl];
			}
			else
			{
				if(prevTree == curStEl) {
					nextLast = prevTree;
				}
				else {
					nextLast = f[prevTree];
				}
			}
			
			t[curLast] = curStEl;
			lastStEl = curStEl;
			curStEl = pre[curStEl];
			curLast = nextLast;
		}
		t[curLast] = q;    	    	
		f[q] = curLast;
		
		curStEl = pre[q];
		lastStEl = q;
		double lastFlow = flow[q];
		boolean lastOrient = orient[q];
		int stop = pre[leavingChild];
		while(curStEl != stop) {
			//n[curStEl] = nStack[i];
			f[curStEl] = curLast;
			double tmpFlow = flow[curStEl];
	    	boolean tmpOrient = orient[curStEl];
			int nextStEl = pre[curStEl];
			
			pre[curStEl] = lastStEl;
			flow[curStEl] = lastFlow;
			orient[curStEl] = !lastOrient;
			
			lastStEl = curStEl;
			curStEl = nextStEl;
			lastFlow = tmpFlow;
			lastOrient = tmpOrient;
		}  	
		f[lastStEl] = curLast;
	}
	
	/**
	 * Updates the dual variable using the given Costs
	 * in the subtree rooted at q
	 * @param q Root of the subtree
	 * @param minRedC Additive cost update
	 */
	private void updateDualNDepth(int q, double minRedC) {
		itTree.setRoot(q);
	
	    //Add if tree edge of q points upward
		//Subtract else
		double delta = orient[q] ? minRedC : -1 * minRedC;
		while(itTree.hasNext()) {
			int cur = itTree.next();
			dual[cur] += delta;
			level[cur] = level[pre[cur]] + 1;
			
		}
	}
    
	/**
	 * Calculates reducing costs
	 * @param from edge from
	 * @param to edge to
	 * @return Reducing costs of edge (from, to)
	 */
    public double redCost(int from, int to) {
    	double cij = costs[from - 1][to - cSrc - 1];
    	return cij - dual[from] + dual[to];
    }
     
    /**
     * 
     * @return A fresh tree iterator
     */
    public TreeIterator getFreshTreeIterator() {
		return new TreeIterator(t, f);
    }
    
    /**
     * Current dual objective function value
     * @return
     */
    public double getCurDual() {
    	return curDual;
    }
    
	/**
	 * @return the surplusFlow
	 */
	public double getSurplusFlow() {
		return surplusFlow;
	}
	
	public double getPrimalObj() {
		double primObj = 0;
		for(int i = 1; i <= cSrc + cTar; i++) {
			if(pre[i] > cSrc) {
				primObj += flow[i] * costs[i - 1][pre[i] - cSrc - 1];
			}
			else if(pre[i] > 0) {
				primObj += flow[i] * costs[pre[i] - 1][i - cSrc - 1];
			}
		}
		
		return primObj;
	}
	
	//could cylce for artificial node
	public int[] getChildren(int root) {
		int[] children = new int[cSrc + 1];
		int anz = 0;
		int child = t[root];
		
		while(pre[child] == root) {
			anz++;
			children[anz] = child;
			child = t[f[child]];
		}
		
		children[0] = anz;
		
		return children;
	}
	
	public double getFlow(int node) {
		return flow[node];
	}
	
	public int getParent(int node) {
		return pre[node];
	}


	/**
	 * @return the cSrc
	 */
	public int getcSrc() {
		return cSrc;
	}


	/**
	 * @return the cTar
	 */
	public int getcTar() {
		return cTar;
	}
	
	public int getCommonPredecessor(int left, int right) {
		//Left edges are preferred to leave
		//Corresponds to the the subtree containing
		//the source node of the entering edge
		
		//Go up to same level
		while(level[left] < level[right]) {
			right = pre[right];
		}
		//Go up to same level
		while(level[right] < level[left]) {
			left = pre[left];
		}
		//Go up on both side concurrently
		while(left != right) {
			left = pre[left];
			right = pre[right];
		}
		
		return left;
	}


	/**
	 * @return the leavingChild
	 */
	public int getLeavingChild() {
		return leavingChild;
	}


	/**
	 * @param curForward the curForward to set
	 */
	public void setCurForward(int curForward) {
		this.curForward = curForward;
	}


	/**
	 * @param curBackward the curBackward to set
	 */
	public void setCurBackward(int curBackward) {
		this.curBackward = curBackward;
	}


	/**
	 * Resets min Flow
	 */
	public void resetMinFlow() {
		this.minDelta = Double.POSITIVE_INFINITY;
	}
	
	
	/**
	 * 
	 * @param node
	 * @return true if upwards, false if downwards
	 */
	public boolean getOrientation(int node) {
		return orient[node];
	}
	
	/**
	 * Returns the orientation of the root "over" the given node
	 * @param node
	 * @return true if upwards, false if downwards
	 */
	public boolean getRootOrientation(int node) {
		while(pre[node] != 0) {
			node = pre[node];
		}
		return orient[node];
		
	}


	/**
	 * @param dual the dual to set
	 */
	public void setDual(double[] dual) {
		this.dual = dual;
	}


	/**
	 * @param curDual the curDual to set
	 */
	public void setCurDual(double curDual) {
		this.curDual = curDual;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		int cur = 0;
		for(int i = 0; i < cSrc + cTar; i++) {
			cur = t[cur];
			builder.append(new String(new char[level[cur]]).replace("\0", "-"));
			builder.append(" " + cur);
			builder.append("\n");
		}
		return "Tree: \n" + builder.toString();
	}

       
    /*
    public void toLatex(int iteration) {
    	TestHelper.treeToLatex(cSrc, cTar, pre, level, t, f, n, orient, dual, flow, Database.sizeTarget, Database.sizeHill, iteration);
    }
    */
}
