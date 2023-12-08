package de.rwth.processmining.tb.core.emd.grounddistances.cfplusbins;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDistEditDiagnose;
import de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics.EditSequence;
import de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics.LVSOpNames;
import de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics.TLVSEditOperation;
import de.rwth.processmining.tb.core.emd.grounddistances.histogramutil.BinDistance;
import de.rwth.processmining.tb.core.emd.grounddistances.histogramutil.BinDistanceIndex;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.TraceSingleBin;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

public class TimeBinnedWLVSWithEdit 
    extends TimeBinnedWeightedLevenshteinStateful 
    implements TraceDistEditDiagnose<TraceSingleBin>{
  
  //TODO
  private BinDistance binDistance;
	
	public TimeBinnedWLVSWithEdit(int nbrBins) {
		super(nbrBins);
		this.binDistance = new BinDistanceIndex(nbrBins);
	}
	
	public TimeBinnedWLVSWithEdit(int nbrBins, double costRename, double costInsDel, boolean considerTimeInsertDelete) {
		super(nbrBins, costRename, costInsDel, considerTimeInsertDelete);
	}

	public EditSequence get_distance_op(TraceSingleBin t1, TraceSingleBin t2) {
		return calcNormWeightedLevDistWithOp(t1, t2);
		
	}
	
	public EditSequence calcNormWeightedLevDistWithOp(TraceSingleBin t1, TraceSingleBin t2) {
		double norm = maxOpCost * Math.max(t1.getTraceLength(), t2.getTraceLength());

		TObjectLongMap<String> map = new TObjectLongHashMap<>(10, 0.5f, -1);
		int lastIndex = -1;

		long[] labelMapped1 = new long[t1.getTraceLength()];
		String[] st1 = t1.getTraceLabels();
		for (int i = 0; i < t1.getTraceLength(); i++) {
			labelMapped1[i] = map.adjustOrPutValue(st1[i], 0, lastIndex + 1);
			if (labelMapped1[i] == lastIndex + 1) {
				lastIndex++;
			}
		}

		long[] labelMapped2 = new long[t2.getTraceLength()];
		String[] st2 = t2.getTraceLabels();
		for (int i = 0; i < t2.getTraceLength(); i++) {
			labelMapped2[i] = map.adjustOrPutValue(st2[i], 0, lastIndex + 1);
			if (labelMapped2[i] == lastIndex + 1) {
				lastIndex++;
			}
		}

		EditSequence.Builder editSeqBuilder = new EditSequence.Builder();
		editSeqBuilder = getDistanceNOperations(labelMapped1, labelMapped2, t1.getBins(), t2.getBins(), editSeqBuilder);
		editSeqBuilder.setCost((norm > 0) ? editSeqBuilder.getCost() / norm : 0);

		return editSeqBuilder.build();
	}	
	
	
	public EditSequence.Builder getDistanceNOperations(long[] left, long[] right, int[] tBinsLeft, int[] tBinsRight, EditSequence.Builder editSeqBuilder) {
		double[][] distance = new double[left.length + 1][right.length + 1]; 
		TLVSEditOperation[][] optPath = new TLVSEditOperation[left.length + 1][right.length + 1]; 
																				 
	    double sum_cost = 0;
	    distance[0][0] = 0;
	    optPath[0][0] = null;
	    for(int i = 0; i < left.length; i++) {
	    	sum_cost += cID + (bConsRIDTime ? tBinsLeft[i] : 0); 
	    	distance[i+1][0] = sum_cost;
	    	optPath[i+1][0] = new TLVSEditOperation(LVSOpNames.DELETE, tBinsLeft[i], i, -1);;
	    }
	    sum_cost = 0;
	    for(int j = 0; j < right.length; j++) {
	    	sum_cost += cID + (bConsRIDTime ? tBinsRight[j] : 0);
	    	distance[0][j+1] = sum_cost;
	    	optPath[0][j+1] = new TLVSEditOperation(LVSOpNames.INSERT, tBinsRight[j], -1, j);
	    }
																				 
		for (int i = 1; i <= left.length; i++) {
			for (int j = 1; j <= right.length; j++) {
				double cost = Math.abs(tBinsLeft[i - 1] - tBinsRight[j - 1]);
				boolean needRenaming = false;
				if(left[i - 1] != right[j - 1]) {
					cost += cR;
					needRenaming = true;
				}
				cost += distance[i - 1][j - 1];
				double costInsert = distance[i][j - 1] + cID + (bConsRIDTime ? tBinsRight[j - 1] : 0);
				double costDelete = distance[i-1][j] + cID + (bConsRIDTime ? tBinsLeft[i - 1] : 0);

				if(cost <= costInsert && cost <= costDelete) {
					if(needRenaming) {
						optPath[i][j] = new TLVSEditOperation(LVSOpNames.RENAME, tBinsLeft[i - 1] - tBinsRight[j - 1], i-1, j-1);
					}
					else {
						int binDiff = tBinsLeft[i - 1] - tBinsRight[j - 1];
						//TODO
						optPath[i][j] = new TLVSEditOperation(binDiff == 0 ? LVSOpNames.MATCH : LVSOpNames.TMATCH, binDiff, i-1, j-1);
//						optPath[i][j] = new TLVSEditOperation(LVSOpNames.MATCH, binDiff, i-1, j-1);
					}
					distance[i][j] = cost;
				}
				else if(costInsert <= costDelete) {
					optPath[i][j] = new TLVSEditOperation(LVSOpNames.INSERT, tBinsRight[j - 1], -1, j-1);
					distance[i][j] = costInsert;
				}
				else {
					optPath[i][j] = new TLVSEditOperation(LVSOpNames.DELETE, tBinsLeft[i - 1],  i-1, -1);;
					distance[i][j] = costDelete;
				}
			}
		}
		
		int i = left.length;
		int j = right.length;
		while(i > 0 || j > 0) {
			editSeqBuilder.addEditOperationReverse(optPath[i][j]);
			switch(optPath[i][j].getOperation()) {
				case MATCH:
				case TMATCH:
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
