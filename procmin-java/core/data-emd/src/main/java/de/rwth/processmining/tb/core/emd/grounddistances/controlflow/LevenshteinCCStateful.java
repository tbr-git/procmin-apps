package de.rwth.processmining.tb.core.emd.grounddistances.controlflow;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTraceCC;

public class LevenshteinCCStateful implements TraceDescDistCalculator<BasicTraceCC> {

  /**
   * Lookup table of known distances. 
   */
	private Table<BasicTraceCC, BasicTraceCC, Double> lvDistLookup = null; 
	  
	public LevenshteinCCStateful() {
		lvDistLookup = HashBasedTable.create();
	}

	@Override
	public double get_distance(BasicTraceCC t1, BasicTraceCC t2) {
	  // Lookup (t1, t2)
		Double d = lvDistLookup.get(t1, t2);
		if(d != null) {
			return d;
		}
		else {
      // Lookup (t2, t1)
      d = lvDistLookup.get(t2, t1);
      if(d != null) {
        return d;
      }
      else {
        d = Levenshtein.getNormalisedDistance(t1.getTraceCategories(), t2.getTraceCategories());
        lvDistLookup.put(t1, t2, d);
        return d;
      }
		}
  }
	
	@Override
	public String toString() {
		return "LevenshteinStringStateful []";
	}


	@Override
	public String getShortDescription() {
		return "LVS";
	}
}
