package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery;

import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.CandidatesMetaDiagnostic;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.MetaDifferenceVisContainer;

public record PDFGMetaDiagnostic(CandidatesMetaDiagnostic diffCandidates, MetaDifferenceVisContainer pvaVisualizations) {

}
