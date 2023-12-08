package de.rwth.processmining.tb.core.emd.solver.akp.helper;

/**
 * Class used to iterate over a given subtree.
 * Does not work for artificial node as root because cur == end at the start.
 * @author brockhoff
 *
 */
public class TreeIterator {

	private int[] t;
	
	private int[] f;
	
	private int end;
	
	private int cur;
	
	public TreeIterator(int[] t, int[] f) {
		this.t = t;
		this.f = f;
		cur = 0;
	}
	
	public void setRoot(int root) {
		cur = root;
		end = t[f[root]];
	}
	
	public boolean hasNext() {
		return cur != end;
	}
	
	public int next() {
		int res = cur;
		cur = t[cur];
		return res;
	}

}
