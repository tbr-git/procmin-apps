package de.rwth.processmining.tb.core.emd.grounddistances.cfplusbins;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.TraceSingleBin;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

public class TimeBinnedWeightedLevenshteinStateful 
    implements TraceDescDistCalculator<TraceSingleBin> {
	final private static Logger logger = LogManager.getLogger(TimeBinnedWeightedLevenshteinStateful.class);
	
	private Table<Integer, Integer, Double> wlvDistLookup = null; 
	
	private int nbrBins;
	
	protected double cR;
	
	protected double cID;
	
	protected boolean bConsRIDTime;
	
	protected double maxOpCost = 0;
	  

	public TimeBinnedWeightedLevenshteinStateful(int nbrBins) {
		//TODO
		this(nbrBins, 1.0, 1.0, true);
	}
	
	public TimeBinnedWeightedLevenshteinStateful(int nbrBins, double costRename, double costInsDel, boolean considerTimeInsertDelete) {
		wlvDistLookup = HashBasedTable.create();
		this.nbrBins = nbrBins;
		bConsRIDTime = considerTimeInsertDelete;
		updateRenameDeleteCosts(costRename, costInsDel);
	}
	
	@Override
	public double get_distance(TraceSingleBin t1, TraceSingleBin t2) {
		
		int h1 = t1.hashCode();
		int h2 = t2.hashCode();
		
		Double d = wlvDistLookup.get(h1, h2);
		if(d != null) {
			return d;
		}
		else {
//			if(testCashMisses % 1000 == 0) {
//				logger.trace("Current Cash misses: " + testCashMisses);
//			}
			d = calcNormWeightedLevDist(t1, t2);
			wlvDistLookup.put(h1, h2, d);
			wlvDistLookup.put(h2, h1, d);
			return d;
		}
		
	}

	private double calcNormWeightedLevDist(TraceSingleBin t1, TraceSingleBin t2) {
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

		return (norm > 0) ? getDistance(labelMapped1, labelMapped2, t1.getBins(), t2.getBins()) / norm : 0;
	}

	private double getDistance(long[] left, long[] right, int[] tBinsLeft, int[] tBinsRight) {
		if (left == null || right == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}
		
		if (left.length != tBinsLeft.length || right.length != tBinsRight.length) {
			logger.error("Index problem");
		}
		

		/*
		 * This implementation use two variable to record the previous cost
		 * counts, So this implementation use less memory than previous impl.
		 */

		int n = left.length; // length of left
		int m = right.length; // length of right

		if (n == 0) {
			return m * cID + Arrays.stream(tBinsRight).sum();
		} else if (m == 0) {
			return n * cID + Arrays.stream(tBinsLeft).sum();
		}

		if (n > m) {
			// swap the input strings to consume less memory
			final long[] tmp = left;
			left = right;
			right = tmp;
			n = m;
			m = right.length;
			
			final int[] tmpB = tBinsLeft;
			tBinsLeft = tBinsRight;
			tBinsRight = tmpB;
			
		}

		double[] p = new double[n + 1];

		// indexes into strings left and right
		int i; // iterates through left
		int j; // iterates through right
		double upper_left;
		double upper;

		long rightJ; // jth character of right
		double cost; // cost

		p[0] = 0;
		for (i = 1; i <= n; i++) {
			// Important to be consistent with the method used for m = 0 || n = 0!
			p[i] = p[i-1] + cID + (bConsRIDTime ? tBinsLeft[i - 1] : 0);
		}

		for (j = 1; j <= m; j++) {
			upper_left = p[0];
			rightJ = right[j - 1];
			p[0] += cID + (bConsRIDTime ? tBinsRight[j - 1] : 0);

			for (i = 1; i <= n; i++) {
				upper = p[i];
				cost = Math.abs(tBinsLeft[i - 1] - tBinsRight[j - 1]);
				if(left[i - 1] != rightJ) {
					cost += cR;
				}
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost
				//TODO Changes tBinsRight[i-1]!!!!
				p[i] = Math.min(Math.min(p[i - 1] + cID + (bConsRIDTime ? tBinsLeft[i - 1] : 0), 
						p[i] + cID + (bConsRIDTime ? tBinsRight[j - 1] : 0)), 
						upper_left + cost);
				upper_left = upper;
			}
		}

		return p[n];
	}
	
	private void updateRenameDeleteCosts(double cFactorRename, double cFactorInsDel) {
		cR = cFactorRename;
		cID = cFactorInsDel;
		if(bConsRIDTime) {
			maxOpCost = Math.max(cR, cID) + (nbrBins - 1); 
		}
		else {
			maxOpCost = Math.max(cR, Math.max(cID, (nbrBins - 1))); 
		}
	}
	
	public int getNbrBins() {
		return nbrBins;
	}

	public double getCostRename() {
		return cR;
	}

	public double getCostInsertDelete() {
		return cID;
	}

	@Override
	public String toString() {
		return "TimeBinnedWeightedLevenshteinStateful [nbrBins=" + nbrBins
				+ ", costRename=" + cR + ", costInsertDelete=" + cID + "]";
	}

	@Override
	public String getShortDescription() {
		return "TLVS";
	}

}
