package de.rwth.processmining.tb.core.emd.grounddistances.controlflow;

import de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics.EditSequence;
import de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics.LVSEditOperation;
import de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics.LVSOpNames;

public class LevenshteinEdit {

	public static EditSequence calcNormWeightedLevDistWithOp(int[] left, int[] right) {
		double norm = (double) Math.max(left.length, right.length);


		EditSequence.Builder editSeqBuilder = new EditSequence.Builder();
		editSeqBuilder = addDistNOp(left, right, editSeqBuilder);
		editSeqBuilder.setCost((norm > 0) ? editSeqBuilder.getCost() / norm : 0 );

		return editSeqBuilder.build();
	}	
	
	
	public static EditSequence.Builder addDistNOp(int[] left, int[] right, EditSequence.Builder editSeqBuilder) {
		double[][] distance = new double[left.length + 1][right.length + 1]; 
		LVSEditOperation[][] optPath = new LVSEditOperation[left.length + 1][right.length + 1]; 
																				 
	    distance[0][0] = 0;
	    optPath[0][0] = null;
	    for(int i = 0; i < left.length; i++) {
	    	distance[i+1][0] = distance[i][0] + 1;
	    	optPath[i+1][0] = new LVSEditOperation(LVSOpNames.DELETE, i, -1);
	    }
	    for(int j = 0; j < right.length; j++) {
	    	distance[0][j + 1] = distance[0][j] + 1;
	    	optPath[0][j+1] = new LVSEditOperation(LVSOpNames.INSERT, -1, j);
	    }
																				 
	    
	    double cost = 0;
		for (int i = 1; i <= left.length; i++) {
			for (int j = 1; j <= right.length; j++) {
				cost = 0;
				boolean needRenaming = false;
				if(left[i - 1] != right[j - 1]) {
					cost += 1;
					needRenaming = true;
				}
				cost += distance[i - 1][j - 1];
				double costInsert = distance[i][j - 1] + 1;
				double costDelete = distance[i-1][j] + 1;

				if(cost <= costInsert && cost <= costDelete) {
					if(needRenaming) {
						optPath[i][j] = new LVSEditOperation(LVSOpNames.RENAME, i-1, j-1);
					}
					else {
						optPath[i][j] = new LVSEditOperation(LVSOpNames.MATCH, i-1, j-1);
					}
					distance[i][j] = cost;
				}
				else if(costInsert <= costDelete) {
					optPath[i][j] = new LVSEditOperation(LVSOpNames.INSERT, -1, j-1);
					distance[i][j] = costInsert;
				}
				else {
					optPath[i][j] = new LVSEditOperation(LVSOpNames.DELETE, i-1, -1);
					distance[i][j] = costDelete;
				}
			}
		}
		
//		List<Pair<Integer, Pair<Integer, Integer>>> lOptPath = new LinkedList<>();
//		StringBuilder builder = new StringBuilder();
		int i = left.length;
		int j = right.length;
		while(i > 0 || j > 0) {
			editSeqBuilder.addEditOperationReverse(optPath[i][j]);	
			switch(optPath[i][j].getOperation()) {
				case MATCH:
					i--;
					j--;
					break;
				case RENAME:
					i--;
					j--;
					break;
				case DELETE:
					i--;
					break;
				case INSERT:
					j--;
					break;
			}
			
		}
		editSeqBuilder.setCost(distance[left.length][right.length]);
		return editSeqBuilder;
	}              	

}
