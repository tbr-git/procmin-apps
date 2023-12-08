package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.diffdecompgraph;

import java.util.Optional;

public record DDGVertexProbInfo(
		double probNonCondLeft, 			// Non-conditioned probability left 
		double probNonCondRight,			// Non-conditioned probability right 
		double probNonCondResLeft, 			// Residual Non-conditioned probability left 
		double probNonCondResRight, 		// Residual Non-conditioned probability right 
		Optional<Double> probCondLeft, 		// Conditioned probability left 
		Optional<Double> probCondRight,		// Conditioned probability right 
		Optional<Double> probCondResLeft, 	// Residual conditioned probability left 
		Optional<Double> probCondResRight 	// Residual conditioned probability right 
	) {

}
