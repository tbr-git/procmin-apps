package hfdd.evaluation.parametergs;

public record IVFResultGS(IVFGSTaskSpec taskSpec, int nbrDifferences, double itemsetSizeQ25, double itemsetSizeQ50, double itemsetSizeQ75,
    double top10EMDQ50, double top10ItemsetSizeQ50, double jaccardDefault, double overlapCoefficient) {
}
