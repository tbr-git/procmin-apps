package org.processmining.processcomparator.model;

import org.processmining.plugins.transitionsystem.miner.TSMinerTransitionSystem;
import org.processmining.plugins.tsanalyzer2.AnnotatedTransitionSystem;

public class ResultsObject {

	private TSMinerTransitionSystem ts;

	private AnnotatedTransitionSystem ats_A, ats_B, ats_Union;

	public ResultsObject(TSMinerTransitionSystem ts, AnnotatedTransitionSystem ats_A, AnnotatedTransitionSystem ats_B,
			AnnotatedTransitionSystem ats_Union) {
		this.ats_A = ats_A;
		this.ats_B = ats_B;
		this.ats_Union = ats_Union;
		this.ts = ts;
	}

	public AnnotatedTransitionSystem getAts_A() {
		return ats_A;
	}

	public AnnotatedTransitionSystem getAts_B() {
		return ats_B;
	}

	public AnnotatedTransitionSystem getAts_Union() {
		return ats_Union;
	}

	public TSMinerTransitionSystem getTs() {
		return ts;
	}

}
