package de.rwth.processmining.tb.core.util.histogram;

import org.apache.commons.math3.ml.clustering.Clusterable;

//wrapper class
public class TimeWrapper implements Clusterable {
	private double t; 

	public TimeWrapper(double t) {
		this.t = t;
	}

	public double getTime() {
		return t;
	}

	public double[] getPoint() {
		return new double[]{t};
	}
}
