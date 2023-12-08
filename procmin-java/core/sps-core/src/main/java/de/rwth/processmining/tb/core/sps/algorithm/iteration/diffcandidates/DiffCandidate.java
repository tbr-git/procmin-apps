package de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates;

import java.util.Collection;
import java.util.Optional;

import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;

public record DiffCandidate(HFDDVertex v, Optional<Collection<HFDDVertex>> condContext) {

}
