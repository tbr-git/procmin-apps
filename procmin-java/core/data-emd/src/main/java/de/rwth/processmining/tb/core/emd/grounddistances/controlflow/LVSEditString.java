package de.rwth.processmining.tb.core.emd.grounddistances.controlflow;

import org.apache.commons.lang3.tuple.Pair;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDistEditDiagnose;
import de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics.EditSequence;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTrace;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class LVSEditString extends LevenshteinStringStateful implements TraceDistEditDiagnose<BasicTrace> {

  @Override
  public EditSequence get_distance_op(BasicTrace t1, BasicTrace t2) {
		int[] leftCat;
		int[] rightCat;
    Pair<int[], int[]> tracesCategorical = categorizeTraces(t1.getsTrace(), 
        t2.getsTrace());
    leftCat = tracesCategorical.getLeft();
    rightCat = tracesCategorical.getRight();
    
    return LevenshteinEdit.calcNormWeightedLevDistWithOp(leftCat, rightCat);
  }
  
  /**
   * Translate both into categorical traces
   * @param left Left trace
   * @param right Right trace
   * @return
   */
	public Pair<int[], int[]> categorizeTraces(String[] left, String[] right) {
		TObjectIntMap<String> map = new TObjectIntHashMap<>(10, 0.5f, -1);
		int lastIndex = -1;

		int[] leftCat = new int[left.length];
		for (int i = 0; i < left.length; i++) {
			leftCat[i] = map.adjustOrPutValue(left[i], 0, lastIndex + 1);
			if (leftCat[i] == lastIndex + 1) {
				lastIndex++;
			}
		}

		int[] rightCat = new int[right.length];
		for (int i = 0; i < right.length; i++) {
			rightCat[i] = map.adjustOrPutValue(right[i], 0, lastIndex + 1);
			if (rightCat[i] == lastIndex + 1) {
				lastIndex++;
			}
		}
		
		return Pair.of(leftCat, rightCat);
	}

}
