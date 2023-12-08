package org.processmining.processcomparator.model;

import java.util.Arrays;
import java.util.List;

import org.processmining.plugins.tsanalyzer2.TSAnalyzer;

public class ConstantDefinitions {

	public static final String NONE = "none";

	public static final List<String> comparisonTypes = Arrays.asList(TSAnalyzer.trace_frequency, TSAnalyzer.duration,
			TSAnalyzer.elapsed_time, TSAnalyzer.remaining_time, TSAnalyzer.sojourn_time, TSAnalyzer.ocurrence_frequency,
			NONE);

	public static final String[] stateThicknessTypes = { TSAnalyzer.trace_frequency, TSAnalyzer.elapsed_time,
			TSAnalyzer.sojourn_time, TSAnalyzer.remaining_time };
	public static final String[] transitionThicknessTypes = { TSAnalyzer.trace_frequency, TSAnalyzer.duration };

	public static final int MAX_EDGE_WIDTH = 10;
}
