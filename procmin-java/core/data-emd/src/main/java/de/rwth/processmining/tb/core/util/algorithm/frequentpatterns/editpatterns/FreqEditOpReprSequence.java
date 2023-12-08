package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.editpatterns;

import java.util.List;

public class FreqEditOpReprSequence {
	
	private final List<LVSEditOpTriple> ops;
	
	private final double weight;

	public FreqEditOpReprSequence(List<LVSEditOpTriple> ops, double weight) {
		super();
		this.ops = ops;
		this.weight = weight;
	}

	public List<LVSEditOpTriple> getOps() {
		return ops;
	}

	public double getWeight() {
		return weight;
	}

}
