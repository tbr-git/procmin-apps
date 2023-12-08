package de.rwth.processmining.tb.core.emd.language.tracedescriptors.noncffeatures;

public abstract class BinnedContinuousFeatureExtractor {
	
	/**
	 * Bin Edges.
	 * Assumption: binEdges[0] = -infty; binEdges[-1] = infty
	 * Left closed.
	 */
	private final double[] binEdges;
	
	/**
	 * Number of bins.
	 */
	private final int nbrBins;
	
	/**
	 * Formatting the bin edge when printing the bin.
	 */
	private BinEdgeFormatter edgeFormatter;
	
	public BinnedContinuousFeatureExtractor(double[] binEdges, int nbrBins, 
			BinEdgeFormatter edgeFormatter) {
		
		this.binEdges = binEdges;
		this.nbrBins = nbrBins;
		this.edgeFormatter = edgeFormatter;
	}
	
	public String getBinDescription(int bin) {
		// Not in bin range
		if (bin >= (binEdges.length - 1)) {
			throw new IndexOutOfBoundsException("Invalid bin index " + bin);
		}
		else {
			String lower = this.edgeFormatter.formatBinEdge(binEdges[bin]);
			String upper = this.edgeFormatter.formatBinEdge(binEdges[bin + 1]);
			
			return "[" + lower + ", " + upper + ")";
		}
	}

}
