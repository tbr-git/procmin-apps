package de.rwth.processmining.tb.core.emd.solver.akp.core;

import de.rwth.processmining.tb.core.emd.grounddistances.TraceDescDistCalculator;
import de.rwth.processmining.tb.core.emd.language.StochasticLanguage;
import de.rwth.processmining.tb.core.emd.language.StochasticLanguageIterator;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.emd.solutiondata.EMDSolContainer;
import de.rwth.processmining.tb.core.emd.solver.SolverFactory;
import de.rwth.processmining.tb.core.emd.solver.akp.helper.InitTreeArrayStruct;
import de.rwth.processmining.tb.core.emd.solver.akp.init.BuildTree;
import de.rwth.processmining.tb.core.emd.solver.akp.init.StructInitEPSA;


/**
 * A factory for an AKP solver that represents the tree with an array
 * and does a complete comparison between surplus sources and deficit
 * targets in each step.
 * @author brockhoff
 */
public class AKPFactoryArraySimple<F extends TraceDescriptor, D extends TraceDescDistCalculator<F>> 
    extends SolverFactory<AKPSolverArraySimple, F, D> {

	/**
	 * Reference to the solver that will be build
	 */
	private AKPSolverArraySimple solver;
	
	/**
	 * 
	 * @param groundDist
	 */
	public AKPFactoryArraySimple(D groundDist) {
		super(groundDist);
	}

	// IMPORTANT: weights of holes need to be positive
	@Override
	public void setupNewSolver(StochasticLanguage<F> Ll, 
	    StochasticLanguage<F> Lr, 
	    EMDSolContainer.Builder<F> emdSolBuilder) {
		
		int cSrc = Ll.getNumberOfTraceVariants();
		int cTar = Lr.getNumberOfTraceVariants();
		double[][] groundDistMatrix = computeDistanceMatrix(Ll, Lr, super.getGroundDist());
		if(emdSolBuilder != null) {
			emdSolBuilder.addDistances(groundDistMatrix);
		}

		// Weights sources
		float[] sizeHill = new float[Ll.getNumberOfTraceVariants()];
		{
			StochasticLanguageIterator<F> itL = Ll.iterator();
			for(int i = 0; i < Ll.getNumberOfTraceVariants(); i++) {
				itL.next();
				sizeHill[i] = (float) itL.getProbability();
			}
		}

		// Weights targets
		float[] sizeHole = new float[Lr.getNumberOfTraceVariants()];
		{
			StochasticLanguageIterator<F> itR = Lr.iterator();
			for(int i = 0; i < Lr.getNumberOfTraceVariants(); i++) {
				itR.next();
				sizeHole[i] = (float) itR.getProbability();
			}
		}
		float[] negSizeHole = new float[Lr.getNumberOfTraceVariants()];
		
		for(int i = 0; i < Lr.getNumberOfTraceVariants(); i++) {
			negSizeHole[i] = -1 * sizeHole[i];
		}
		
	
		StructInitEPSA initSolverStruct;
		InitTreeArrayStruct initTreeStruct;
		//Build the initial tree and calculate the dual values
		initTreeStruct = BuildTree.buildTreeArray(cSrc, cTar, sizeHill, negSizeHole, groundDistMatrix);
		initSolverStruct = new StructInitEPSA(initTreeStruct);
		//TestHelper.arrayTreeToLatex(initSolverStruct.tree, 0);

		solver = new AKPSolverArraySimple(initSolverStruct);

	}

	@Override
	public AKPSolverArraySimple getSolver() {
		return solver;
	}
}
