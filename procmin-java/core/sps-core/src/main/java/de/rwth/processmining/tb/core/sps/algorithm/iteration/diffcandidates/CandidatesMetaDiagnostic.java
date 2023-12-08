package de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates;

import java.util.Collection;

public record CandidatesMetaDiagnostic(DiffCandidate diffCandidate, Collection<DiffCandidate> complDifferences) {

}
