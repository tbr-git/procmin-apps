package de.rwth.processmining.tb.core.sps.algorithm.iteration;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.SLDSPipelineBuildingUtil;
import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.abstraction.CCCVariantAbstImpl;
import de.rwth.processmining.tb.core.data.variantlog.abstraction.CCCVariantAbstLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.util.LogBuildingException;
import de.rwth.processmining.tb.core.data.xlogutil.LoopUnrollingLimited;
import de.rwth.processmining.tb.core.data.xlogutil.TraceTransformerInplace;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraph;
import de.rwth.processmining.tb.core.util.stopwatch.PerformanceLogger;
import de.rwth.processmining.tb.core.util.stopwatch.SpsBuildMilestones;

public abstract class HFDDIterationManagementBuilder {
	final static Logger logger = LogManager.getLogger( HFDDIterationManagementBuilder.class );
	
	/**
	 * Event classifier.
	 */
	private XEventClassifier classifier;
	
	/** 
	 * Left log for comparison.
	 */
	private XLog xlogL;
	
	/** 
	 * Right log for comparison.
	 */
	private XLog xlogR;
	
	/**
	 * Maximum number of loop iterations (activity repetitions) that 
	 * will be extended by their number of occurrences.
	 * Everything larger or equal will receive the same name.
	 * 
	 * If absent, no unrolling will take place.
	 */
	private Optional<Integer> maxUnroll;
	
	/**
	 * Some high-level time logging.
	 */
	private final PerformanceLogger<SpsBuildMilestones> perfLogger;
	
	
	public HFDDIterationManagementBuilder() {
		super();
		xlogL = null;
		xlogR = null;
		classifier = null;
		
		this.perfLogger = new PerformanceLogger<>(SpsBuildMilestones.class);

		this.maxUnroll = Optional.empty();

	}

	public HFDDIterationManagement build() throws HFDDIterationManagementBuildingException {
		if (xlogL == null || xlogR == null || classifier == null) {
			logger.error("Cannot create HFDD iteration managment class. Missing data.");
			throw new HFDDIterationManagementBuildingException("Error creating hfdd iteration management. Missing data");
		}

		////////////////////////////////////////
		// Data source
		////////////////////////////////////////
		logger.info("Creating data source...");
		perfLogger.startMeasurement(SpsBuildMilestones.LOAD_DATA);
		BiComparisonDataSource<CCCVariantAbstImpl> biCompDS;
		try {
			biCompDS = createDataSource();
		} catch (SLDSTransformerBuildingException | LogBuildingException e) {
			e.printStackTrace();
			logger.error("Could not instantiate the data source to compute the HFDDGraph structure on");
			throw new HFDDIterationManagementBuildingException("Error creating the data source: " + e.getMessage(), e);
		}
		perfLogger.stopMeasurement(SpsBuildMilestones.LOAD_DATA);
		logger.info("Created data source.");

		////////////////////////////////////////
		// HFDD Graph
		////////////////////////////////////////
		logger.info("Mining the HFDD graph...");
		perfLogger.startMeasurement(SpsBuildMilestones.CREATE_SPS_GRAPH);
		HFDDGraph hfddGraph = mineHFDDGraph(biCompDS);
		perfLogger.stopMeasurement(SpsBuildMilestones.CREATE_SPS_GRAPH);
		logger.info("Mined the HFDD graph.");
		
		HFDDIterationManagement hfddItManage = new HFDDIterationManagement(biCompDS, hfddGraph);
		
		////////////////////////////////////////
		// Measurement initialization
		////////////////////////////////////////
		logger.info("Initializing the base measurement...");
		perfLogger.startMeasurement(SpsBuildMilestones.BASE_MEASUREMENT);
		hfddItManage.initBaseIteration();
		perfLogger.stopMeasurement(SpsBuildMilestones.BASE_MEASUREMENT);
		logger.info("Base measurement done.");
		return hfddItManage;
	}
	
	/**
	 * Create the data source for the iterations from the provided XLogs.
	 * @return Data source of abstraction trace variants.
	 * @throws LogBuildingException 
	 * @throws SLDSTransformerBuildingException 
	 */
	private BiComparisonDataSource<CCCVariantAbstImpl> createDataSource() 
			throws SLDSTransformerBuildingException, LogBuildingException {
		// Apply loop unrolling
		List<TraceTransformerInplace> transformers = new LinkedList<>();
		
		if (this.maxUnroll.isPresent()) {
			TraceTransformerInplace loopUnrollingTransformer = new LoopUnrollingLimited(maxUnroll.get());
			transformers.add(loopUnrollingTransformer);
		}

		// Create Data Source
		// Factory
		CVariantLogFactory<CCCVariantAbstImpl> variantLogFactory = new CCCVariantAbstLogFactory();
		variantLogFactory.setClassifier(classifier);
		List<StochasticLanguageDataSource<CCCVariantAbstImpl>> lDataSources = null;

		// Create data source
		lDataSources = SLDSPipelineBuildingUtil.buildPipelineFrom2XLogs(xlogL, xlogR, variantLogFactory, transformers);
		BiComparisonDataSource<CCCVariantAbstImpl> biCompDS = 
				new BiComparisonDataSource<CCCVariantAbstImpl>(lDataSources.get(0), lDataSources.get(1));
		return biCompDS;
	}
	
	/**
	 * Create the initial HFDD Graph.
	 * 
	 * @param biCompDS Data source to mine the HFDD graph structure from (frequent IS)
	 * @return HFDDGraph
	 * @throws HFDDIterationManagementBuildingException Raised if the creation fails
	 */
	protected abstract HFDDGraph mineHFDDGraph(BiComparisonDataSource<? extends CVariant> biCompDS) 
			throws HFDDIterationManagementBuildingException;

	/*================================================================================
	 * Getters and Setters
	 *================================================================================
	 */
	
	public XEventClassifier getClassifier() {
		return classifier;
	}

	public HFDDIterationManagementBuilder setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
		return this;
	}

	public HFDDIterationManagementBuilder setMaxUnroll(int maxUnroll) {
		this.maxUnroll = Optional.of(maxUnroll);
		return this;
	}

	public HFDDIterationManagementBuilder disableLoopUnrolling() {
		this.maxUnroll = Optional.empty();
		return this;
	}


	public XLog getXlogL() {
		return xlogL;
	}

	public HFDDIterationManagementBuilder setXlogL(XLog xlogL) {
		this.xlogL = xlogL;
		return this;
	}

	public XLog getXlogR() {
		return xlogR;
	}

	public HFDDIterationManagementBuilder setXlogR(XLog xlogR) {
		this.xlogR = xlogR;
		return this;
	}
	
	public PerformanceLogger<SpsBuildMilestones> getPerformanceLogger() {
	  return this.perfLogger;
	}



}
