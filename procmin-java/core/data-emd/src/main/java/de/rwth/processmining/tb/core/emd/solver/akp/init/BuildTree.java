package de.rwth.processmining.tb.core.emd.solver.akp.init;

import java.util.LinkedList;
import java.util.List;

import de.rwth.processmining.tb.core.emd.solver.akp.helper.InitTreeArrayStruct;


/**
 * Utility class that builds the dual feasible AKP initial tree 
 * from a given instance.
 * @author brockhoff
 *
 */
public class BuildTree {

	/**
	 * Build an array instance of the initial tree. See {@see Tree}.
	 * @param cSrc Number of sources
	 * @param cTar Number of targets
	 * @param sizeHill Amount of earth at the sources
	 * @param sizeHole Size of the holes (NEGATIVE)
	 * @param cost Cost matrix (source,target)
	 * @return struct containing the tree and deficit/surplus subtrees(roots)
	 */
	public static InitTreeArrayStruct buildTreeArray(int cSrc, int cTar,
			float[] sizeHill,  float[] sizeHole, double[][] cost) {
		
		//Tree representation
		int cNode = cSrc + cTar + 1;
		int fTar = cSrc + 1;
		int[] pre = new int[cNode];
		int[] level = new int[cNode];
		int[] t = new int[cNode];
		int[] f = new int[cNode];
		int[] n = new int[cNode];
		//Variables for the simplex-type algrithm
		boolean[] orient = new boolean[cNode];
		double[] dual = new double[cNode];
		double[] flow = new double[cNode];
		
		//Root no predecessor
		pre[0] = -1;
		//First target
		t[0] = fTar;
			
		List<Integer> surTrees = new LinkedList<>();
		List<Integer> defTrees = new LinkedList<>();

		double dualValue = 0;
		//Min value and index
		double min = Double.POSITIVE_INFINITY;
		int target=-1;
		double[] earthAtTar = new double[cTar];
		
		//Determine arc for each source
		for(int i = 0; i < cSrc; i++) {
			//Closest target
			for(int j = 0; j < cTar; j++) {
				if(cost[i][j] < min) {
					min = cost[i][j];
					target = fTar + j;
				}
			}
			//Edge for earth send
			//Send all earth
			int curSrc = i+1;
			level[curSrc] = 2;
			pre[curSrc] = target;
			f[curSrc] = curSrc;
			n[curSrc] = 1;
			n[target]++;
			//Add source to targets subtree
			if(f[target] == 0) {
				t[target] = curSrc;
				//Last target is fixed later
				t[curSrc] = target + 1;
				f[target] = curSrc;
			}
			else {
				t[f[target]] = curSrc;
				//Last target is fixed later
				t[curSrc] = target + 1;
				f[target] = curSrc;
			}
			//Set Simplex variables
			double earth = sizeHill[i];
			dualValue += earth * min;
			flow[curSrc] = earth;
			orient[curSrc] = true;
			dual[curSrc] = min;
			
			//Store earth send to target
			earthAtTar[target - fTar] += earth;

			min = Double.POSITIVE_INFINITY;
		}
		
		double surplusFlow = 0;

		//Link targets to root
		//Now direction is clear!
		//Also determine deficits and overloads
		for(int j = 0; j < cTar; j++) {
			int tarInd = fTar + j;
			pre[tarInd] = 0;
			level[tarInd] = 1;
			//Also count root
			n[tarInd]++;
			if(f[tarInd] == 0) {
				f[tarInd] = tarInd;
				t[tarInd] = tarInd + 1;
			}
			//Set simplex variables
			double diffEarth = earthAtTar[j] + sizeHole[j];
			if(diffEarth > 0) {
				orient[tarInd] = true;
				flow[tarInd] = diffEarth;
				surTrees.add(tarInd);
				surplusFlow += diffEarth;
			}
			else {
				orient[tarInd] = false;
				flow[tarInd] = -1*diffEarth;
				defTrees.add(tarInd);
			}
		}
		//Last target
		int lastTarInd = fTar + cTar - 1;
		t[f[lastTarInd]] = 0;
		f[0] = f[lastTarInd];
		
		//Testing
		//TestHelper.treeToLatex(cSrc, cTar, pre, level, t, f, n, orient, dual, flow, sizeHole, sizeHill, 0);
		
		return new InitTreeArrayStruct(cSrc, cTar, pre, level, t, f, n, orient, dual, flow, defTrees, surTrees, dualValue, cost, surplusFlow);
	}
	
}

