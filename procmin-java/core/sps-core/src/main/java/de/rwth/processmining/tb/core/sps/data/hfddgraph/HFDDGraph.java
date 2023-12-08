package de.rwth.processmining.tb.core.sps.data.hfddgraph;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.Graph;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.emd.language.tracedescriptors.TraceDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.measure.HFDDVertexMeasurer;
import de.rwth.processmining.tb.core.sps.data.measurement.HFDDMeasurement;
import de.rwth.processmining.tb.core.util.backgroundwork.CachedBackgroundTaskService;

public class HFDDGraph {
  private static final Logger logger = LogManager.getLogger(HFDDGraph.class);

  /**
   * Is the vertex probability initialized.
   */
  private boolean vertexProbInitialized;

  /**
   * Map from vertex id to vertex.
   */
  private Map<Integer, HFDDVertex> vertices;

  /**
   * Handle to a JGraphT Graph that is used to keep the graph structure.
   */
  private Graph<HFDDVertex, DefaultEdge> g;

  /**
   * Mapping from activities to categories and vice versa.
   */
  private CategoryMapper categoryMapper;

  /**
   * The classifier used to create the itemsets for the vertices.
   */
  private XEventClassifier classifier;

  /**
   * Perspectives that have been evaluated on this graph.
   * 
   * A perspective is associated with an EMD coupling measurement.
   */
  private final Set<PerspectiveDescriptor> availablePerspectives;

  /**
   * Number of vertices in the graph.
   */
  private final int nbrVertices;

  public HFDDGraph(Graph<HFDDVertex, DefaultEdge> graph, XEventClassifier classifier, CategoryMapper categoryMapper) {
    this.vertices = new HashMap<>();
    this.g = graph;
    int nbrVertices = 0;
    for (HFDDVertex v : this.g.vertexSet()) {
      this.vertices.put(v.getId(), v);
      nbrVertices++;
    }
    this.nbrVertices = nbrVertices;
    this.classifier = classifier;
    this.vertexProbInitialized = false;
    this.categoryMapper = categoryMapper;
    this.availablePerspectives = new HashSet<>();

  }

  public HFDDGraph(Set<HFDDVertex> vertices, XEventClassifier classifier) {
    this.vertices = new HashMap<>();
    int nbrVertices = 0;
    for (HFDDVertex v : vertices) {
      this.vertices.put(v.getId(), v);
      nbrVertices++;
    }
    this.nbrVertices = nbrVertices;
    this.classifier = classifier;
    this.vertexProbInitialized = false;
    this.availablePerspectives = new HashSet<>();
  }

  /**
   * Execute the provide measurement on all vertices (i.e., itemsets).
   * 
   * @param <V>             Variant tpye contained in the variant logs (must
   *                        extend V).
   * @param <F>             Extracted feature type
   * @param measure         Measure to be applied to every vertex based on the
   *                        data.
   * @param biCompDS        Datasource providing data for the measurement
   * @param saveMeasurement Save the measurement result in the HFDDGraph
   * @throws SLDSTransformerBuildingException Something went wrong when querying
   *                                          the data source
   */
  public <B extends V, V extends CVariant, F extends TraceDescriptor> void applyMeasure(
      final HFDDVertexMeasurer<V, F> measure, final BiComparisonDataSource<B> biCompDS, final boolean saveMeasurement)
      throws SLDSTransformerBuildingException {

    logger.info("Running HFDD graph measurement...");
    StopWatch stopWatch = StopWatch.createStarted();

    List<Future<Boolean>> runningMeasurments = new LinkedList<>();

    // run measurements concurrently
    for (HFDDVertex v : getVertices()) {
      runningMeasurments.add(CachedBackgroundTaskService.getInstance()
          .submit(new HFDDMeasureVertexWrapper<B, V, F>(v, measure, biCompDS, saveMeasurement)));
    }

    // Join all measurements before we return
    try {
      for (Future<Boolean> f : runningMeasurments) {
        f.get();
      }
    } catch (InterruptedException e1) {
      logger.warn("Measurement interrupted!");
      // TODO
    } catch (ExecutionException e) {
      e.printStackTrace();
      logger.error("Error while running concurrent measurement on vertices");
    }
    availablePerspectives.add(measure.getMeasurementDescription());
    stopWatch.stop();

    logger.info("HFDD graph measurement completed in {}", stopWatch.toString());
  }

  /**
   * Execute the provide measurement on all vertices (i.e., itemsets) and returns
   * the scores.
   * 
   * @param <V>      Variant tpye contained in the variant logs (must extend V).
   * @param <F>      Extracted feature type
   * @param measure  Measure to be applied to every vertex based on the data.
   * @param biCompDS Datasource providing data for the measurement
   * @throws SLDSTransformerBuildingException Something went wrong when querying
   *                                          the data source
   */
  public <B extends V, V extends CVariant, F extends TraceDescriptor> double[] calculateMeasure(
      final HFDDVertexMeasurer<V, F> measure, final BiComparisonDataSource<B> biCompDS)
      throws SLDSTransformerBuildingException {

    logger.info("Running HFDD graph measurement calculation...");
    StopWatch stopWatch = StopWatch.createStarted();

    List<Future<Optional<Double>>> runningCalculations = new LinkedList<>();
    // run measurements concurrently
    for (HFDDVertex v : getVertices()) {
      runningCalculations.add(CachedBackgroundTaskService.getInstance()
          .submit(new CalcMeasureSPSVertexWrapper<B, V, F>(v, measure, biCompDS)));
    }
    double[] resMeasures = null;
    // Join all measurements before we return
    resMeasures = runningCalculations.stream()
        .map(f -> {
          try {
            return f.get();
          } catch (InterruptedException e) {
            return Optional.<Double>empty();
          } catch (ExecutionException e) {
            return Optional.<Double>empty();
          }
        })
        .filter(Optional::isPresent)
        .mapToDouble(o -> o.get())
        .toArray();
      
    stopWatch.stop();

    logger.info("HFDD graph measurement calculation completed in {}", stopWatch.toString());
    return resMeasures;
  }

  public Collection<Pair<Integer, HFDDMeasurement>> getMeasurements(final PerspectiveDescriptor perspective) {
    Collection<HFDDVertex> vertices = getVertices();
    Set<Pair<Integer, HFDDMeasurement>> measurments = vertices.stream()
        .map(v -> Pair.of(v.getId(), v.getVertexInfo().getMeasurements().get(perspective))) // Vertex id, measurment
                                                                                            // pairs
        .filter(p -> !(p.getRight() == null)) // Filter null measurments
        .collect(Collectors.toSet());

    if (measurments.size() > 0 && vertices.size() != measurments.size()) {
      logger.warn("Probably undesired result: There are measurements for the perspective key {}; "
          + "however neither none nor every vertex contain this measurement", perspective.toString());
    }

    return measurments;
  }

  public Collection<HFDDVertex> getVertices() {
    return vertices.values();
  }

  public Collection<DefaultEdge> getEdges() {
    return g.edgeSet();
  }

  public XEventClassifier getClassifier() {
    return classifier;
  }

  public boolean isVertexProbInitialized() {
    return vertexProbInitialized;
  }

  public void setVertexProbInitialized(boolean vertexProbInitialized) {
    this.vertexProbInitialized = vertexProbInitialized;
  }

  public HFDDVertex getVertexbyID(int vertexID) {
    return vertices.get(vertexID);
  }

  /**
   * Get the vertex that corresponds to the given activity set.
   * 
   * @param activities Activity category set
   * @return The vertex or null if vertex is not part of the graph
   */
  public HFDDVertex getVertex(BitSet activities) {
    for (HFDDVertex v : g.vertexSet()) {
      if (v.getVertexInfo().getActivities().equals(activities)) {
        return v;
      }
    }
    return null;
  }

  /**
   * 
   * Get the vertex that corresponds to the given activity names.
   * 
   * @param activities Activity names
   * @return The vertex or null if vertex is not part of the graph
   */
  public HFDDVertex getVertex(String[] activities) {
    BitSet searchSet = new BitSet(categoryMapper.getMaxCategoryCode() + 1);
    Integer c;
    for (String a : activities) {
      c = categoryMapper.getCategory4Activity(a);
      if (c == null) {
        return null;
      }
      searchSet.set(c);
    }
    return this.getVertex(searchSet);
  }

  public Graph<HFDDVertex, DefaultEdge> getGraph() {
    return g;
  }

  public CategoryMapper getCategoryMapper() {
    return categoryMapper;
  }

  public void setCategoryMapper(CategoryMapper categoryMapper) {
    this.categoryMapper = categoryMapper;
  }

  public Set<PerspectiveDescriptor> getAvailablePerspectives() {
    return availablePerspectives;
  }

  public int getNbrVertices() {
    return nbrVertices;
  }

  /**
   * Wrapper for a vertex measurement. Use for scheduling measurements to a thread
   * pool.
   * 
   * @author brockhoff
   *
   * @param <V> Type of the variants.
   * @param <F> Type of the extracted feature.
   */
  public static class HFDDMeasureVertexWrapper<B extends V, V extends CVariant, F extends TraceDescriptor>
      implements Callable<Boolean> {

    /**
     * Vertex to run the measurement on.
     */
    private final HFDDVertex v;

    /**
     * Measurement execution routine.
     */
    private final HFDDVertexMeasurer<V, F> measure;

    /**
     * Data Source to measure on.
     */
    private final BiComparisonDataSource<B> biCompDS;

    /**
     * Save the measurement in the vertex.
     */
    private final boolean saveMeasurement;

    public HFDDMeasureVertexWrapper(HFDDVertex v, HFDDVertexMeasurer<V, F> measure, BiComparisonDataSource<B> biCompDS,
        boolean saveMeasurement) {
      super();
      this.v = v;
      this.measure = measure;
      this.biCompDS = biCompDS;
      this.saveMeasurement = saveMeasurement;
    }

    @Override
    public Boolean call() throws Exception {
      this.measure.measureVertex(this.v, this.biCompDS, this.saveMeasurement);
      return true;
    }
  }

  /**
   * Wrapper for a vertex measurement calculation which returns the calculated
   * score of the vertex. Use for scheduling measurements to a thread pool.
   * 
   * @author brockhoff
   *
   * @param <V> Type of the variants.
   * @param <F> Type of the extracted feature.
   */
  public static class CalcMeasureSPSVertexWrapper<B extends V, V extends CVariant, F extends TraceDescriptor>
      implements Callable<Optional<Double>> {

    /**
     * Vertex to run the measurement on.
     */
    private final HFDDVertex v;

    /**
     * Measurement execution routine.
     */
    private final HFDDVertexMeasurer<V, F> measure;

    /**
     * Data Source to measure on.
     */
    private final BiComparisonDataSource<B> biCompDS;

    public CalcMeasureSPSVertexWrapper(HFDDVertex v, HFDDVertexMeasurer<V, F> measure,
				BiComparisonDataSource<B> biCompDS) {
			super();
			this.v = v;
			this.measure = measure;
			this.biCompDS = biCompDS;
		}

    @Override
    public Optional<Double> call() throws Exception {
      HFDDMeasurement m = this.measure.measureVertex(this.v, this.biCompDS, false);
      return m.getMetric();
    }
  }

}
