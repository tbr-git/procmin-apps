package de.rwth.processmining.tb.core.emd.grounddistances.controlflow;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.BasicTrace;

public class LevenshteinStringStateful implements TraceDescDistCalculator<BasicTrace> {
	
	private Table<BasicTrace, BasicTrace, Double> lvDistLookup = null; 
	  
	
	public LevenshteinStringStateful() {
		lvDistLookup = HashBasedTable.create();
	}

	@Override
	public double get_distance(BasicTrace t1, BasicTrace t2) {
		Double d = lvDistLookup.get(t1, t2);
		if(d != null) {
			return d;
		}
		else {
      d = lvDistLookup.get(t2, t1);
      if(d != null) {
        return d;
      }
      else {
        d = Levenshtein.getNormalisedDistance(t1.getsTrace(), t2.getsTrace());
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
