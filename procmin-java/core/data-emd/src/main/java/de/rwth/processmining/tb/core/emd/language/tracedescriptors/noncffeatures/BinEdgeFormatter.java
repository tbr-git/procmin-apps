package de.rwth.processmining.tb.core.emd.language.tracedescriptors.noncffeatures;

/**
 * Format bin and bin edges into human-readable form.
 */
public interface BinEdgeFormatter {
	
	/**
	 * Format a single edge value (usually, lower or upper)
	 * @param value
	 * @return
	 */
	public String formatBinEdge(double value);
	
	/**
	 * Format an entire bin. 
	 * 
	 * Defaults to formatting "[lower, upper)".
	 * @param bin Bin index.
	 * @param lower Lower edge.
	 * @param upper Upper edge.
	 * @return
	 */
	public default String formatBin(int bin, double lower, double upper) {
		StringBuilder builder = new StringBuilder("[");
		builder.append(this.formatBinEdge(lower));
		builder.append(", ");
		builder.append(this.formatBinEdge(upper));
		builder.append(")");
		return builder.toString();
	}

}
