package de.rwth.processmining.tb.core.util.histogram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

public class KMeansEdgeCalculator implements BinEdgeCalculator {
	
	private int nClusters;

	private KMeansPlusPlusClusterer<TimeWrapper> clusterer;

	public KMeansEdgeCalculator(int nClusters) {
		this.nClusters = nClusters;
	}

	@Override
	public double[] calculateBinEdges(List<Double> lTimes) {
		List<TimeWrapper> clusterInput = new ArrayList<TimeWrapper>(lTimes.size());
		
		lTimes.forEach(t -> clusterInput.add(new TimeWrapper(t)));
		clusterer = new KMeansPlusPlusClusterer<TimeWrapper>(nClusters, 100);
		
		List<CentroidCluster<TimeWrapper>> clusterResults = 
				clusterer.cluster(clusterInput);

		double[] clusterCenters = new double[this.nClusters];
		// Get cluster centers 
		ListIterator<CentroidCluster<TimeWrapper>> it = clusterResults.listIterator();
		
		while(it.hasNext()) {
			clusterCenters[it.nextIndex()] = it.next().getCenter().getPoint()[0];
		}
		Arrays.sort(clusterCenters);

		double[] binEdges = new double[nClusters];
		binEdges[nClusters - 1] = Double.POSITIVE_INFINITY;
		for(int i = 0; i < nClusters - 1; i++) {
			binEdges[i] = (clusterCenters[i] + clusterCenters[i+1]) / 2;
		}
		// Ensure that bin boarders differ at least by 10 (seconds)
		for(int i = 1; i < binEdges.length; i++) {
			binEdges[i] = Math.max(binEdges[i-1] + 10, binEdges[i]);
		}
		return binEdges;
	}
	
	public void setNbrClusters(int nClusters) {
		this.nClusters = nClusters;
	}

}
