package de.rwth.processmining.tb.core.sps.data.hfddgraph;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;

import de.rwth.processmining.tb.core.data.comparison.BiComparisonDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.util.bitsets.BitSetUtil;

public abstract class HFDDGraphBuilder {
  private static final Logger logger = LogManager.getLogger(HFDDGraphBuilder.class);

  /**
   * Build the {@link HFDDGraph} graph from a {@link BiComparisonDataSource}.
   * 
   * @param <T>           Type of the variants
   * @param dataSource    Data source
   * @param minRelSupport Minimum activity itemset support (0,1].
   * @return HFDD Graph
   * @throws SLDSTransformationError
   */
  public HFDDGraph buildBaseHFDDGraph(BiComparisonDataSource<? extends CVariant> dataSource)
      throws SLDSTransformationError {
    logger.info("Creating HFDD from a comparsion data source");
    dataSource.ensureCaching(); // Better cache some queries incoming

    ////////////////////////////////////////
    // Activity set mining
    ////////////////////////////////////////
    // Frequent pattern mining
    logger.info("FIS mining...");
    Collection<? extends Collection<Integer>> itemsets = this.mineActivityItemsets(dataSource);
    logger.info("FIS mining completed");

    ////////////////////////////////////////
    // Create Graph Structure
    ////////////////////////////////////////
    return buildBaseHFDDGraph(dataSource, itemsets);
  }

  /**
   * Build the {@link HFDDGraph} graph from a {@link BiComparisonDataSource}.
   * 
   * @param dataSource       Data source
   * @param activityItemsets Collection of activity itemsets where each itemset is
   *                         a collection of category codes
   * @return HFDD Graph
   * @throws SLDSTransformationError
   */
  private HFDDGraph buildBaseHFDDGraph(BiComparisonDataSource<? extends CVariant> dataSource,
      Collection<? extends Collection<Integer>> activityItemsets) throws SLDSTransformationError {
    CategoryMapper categoryMapper = dataSource.getDataSourceLeft().getVariantLog().getCategoryMapper();

    ////////////////////////////////////////
    // Create Graph Structure
    ////////////////////////////////////////
    logger.info("Creating HFDD graph structure with {} vertices...", activityItemsets.size());
    // Create vertices
    HFDDVertex[] vertices = new HFDDVertex[activityItemsets.size()];
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // IMPORTANT
    // Ids must 0..n.
    // For efficiency (to avoid a lot of hashing), we assume this later.
    // Since the graph size is not expected to change,
    // a consecutive numbering should be easy.
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    int id = 0;
    for (Collection<Integer> pattern : activityItemsets) {
      HFDDVertexInfo vertexInfo = HFDDVertexInfo.buildBaseInfo(pattern, categoryMapper,
          dataSource.getDataSourceLeft().getVariantLog().getMaxCategoryCode());
      HFDDVertex vertex = new HFDDVertex(id, vertexInfo);
      vertices[id] = vertex;
      id++;
    }

    // Create graph instance
    Graph<HFDDVertex, DefaultEdge> g = buildGraphStructureTransClose(vertices);
    logger.info("Created HFDD graph structure with {} vertices and {} edges.", g.vertexSet().size(),
        g.edgeSet().size());

    return new HFDDGraph(g, dataSource.getClassifier(), categoryMapper);
  }

  /**
   * Given a list of vertices, this function build the graph structure. It
   * initializes a {@link Graph} with the given vertices and a-priori (set
   * inclusion) edges.
   * 
   * @param vertices Vertices
   * @return A-priori Graph
   */
  private static Graph<HFDDVertex, DefaultEdge> buildGraphStructure1Diff(HFDDVertex[] vertices) {
    Graph<HFDDVertex, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
    // Add all vertices to the graph
    for (HFDDVertex v : vertices) {
      g.addVertex(v);
    }

    ////////////////////////////////////////
    // Add edges
    // Connect vertices iff they differ by
    // precisely one activity.
    ////////////////////////////////////////
    // Sort vertices
    Arrays.sort(vertices, (v1, v2) -> Integer.compare(v1.getVertexInfo().getActivities().cardinality(),
        v2.getVertexInfo().getActivities().cardinality()));
    // Robustness in case that the first set is empty or a singleton
    int nextCard = vertices[0].getVertexInfo().getActivities().cardinality() + 1;
    // Index of next larger cardinality
    // (initialized in first loop of iteration)
    int iNextCard = 0;
    boolean iHasMaxCard = false;
    // Add edges according to set-inclusion
    HFDDVertex vi, vj;
    BitSet activityVi, activityVj;
    for (int i = 0; !iHasMaxCard; i++) {
      vi = vertices[i];
      activityVi = vi.getVertexInfo().getActivities();

      // i.cardinality == next cardinality => SHIFT
      if (i == iNextCard) {
        while (iNextCard < vertices.length
            && vertices[iNextCard].getVertexInfo().getActivities().cardinality() < nextCard) {
          iNextCard++;
        }
        // No larger cardinality found
        if (iNextCard == vertices.length) {
          iHasMaxCard = true;
        } else {
          nextCard = vertices[iNextCard].getVertexInfo().getActivities().cardinality() + 1;
        }
      }

      if (!iHasMaxCard) {
        // Add edges to 1-larger sets
        for (int j = iNextCard; j < vertices.length; j++) {
          vj = vertices[j];
          activityVj = vj.getVertexInfo().getActivities();
          if (activityVi.cardinality() == activityVj.cardinality() - 1 && // size(vi) = size(vj) - 1
              BitSetUtil.isSubset(activityVi, activityVj)) {
            g.addEdge(vi, vj);
          }
        }
      }
    }
    return g;
  }

  /**
   * Given a list of vertices, this function build the graph structure. It
   * initializes a {@link Graph} with the given vertices and a-priori (set
   * inclusion) edges.
   * 
   * @param vertices Vertices
   * @return A-priori Graph
   */
  private static Graph<HFDDVertex, DefaultEdge> buildGraphStructureTransClose(HFDDVertex[] vertices) {
    Graph<HFDDVertex, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
    // Add all vertices to the graph
    for (HFDDVertex v : vertices) {
      g.addVertex(v);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Add edges
    // Consider all edges such that the source itemset is contained in the target
    //////////////////////////////////////////////////////////////////////////////// itemset
    // Transitive reduction
    // Directly obtained by the following procedure
    // * Sort vertices by descending order
    // * For each vertex vi in that list:
    // * For each vertex vj having smaller cardinality:
    // * If vj subset vi
    // * AND vj not subset of any vertex that has been so for connected to vi
    // * Add edge
    // Note that the sorting by descending order is important
    ////////////////////////////////////////////////////////////////////////////////
    // Sort vertices (descending order)
    Arrays.sort(vertices, (v1, v2) -> -1 * Integer.compare(v1.getVertexInfo().getActivities().cardinality(),
        v2.getVertexInfo().getActivities().cardinality()));
    // Largest cardinality - 1
    int nextCard = vertices[0].getVertexInfo().getActivities().cardinality() - 1;
    // Index of next smaller cardinality
    // (initialized in first loop of iteration)
    int iNextCard = 0;
    // We are on the smallest cardinality
    boolean isSmallestCard = false;
    ////////////////////////////////////////
    // Start adding edges
    ////////////////////////////////////////
    for (int i = 0; !isSmallestCard; i++) {
      // i.cardinality == next cardinality => SHIFT
      if (i == iNextCard) {
        while (iNextCard < vertices.length
            && vertices[iNextCard].getVertexInfo().getActivities().cardinality() > nextCard) {
          iNextCard++;
        }
        // No smaller cardinality found
        if (iNextCard == vertices.length) {
          isSmallestCard = true;
        } else {
          nextCard = vertices[iNextCard].getVertexInfo().getActivities().cardinality() - 1;
        }
      }

      if (!isSmallestCard) {

        HFDDVertex vi = vertices[i];
        BitSet activityVi = vi.getVertexInfo().getActivities();
        // List of currently attached neighbors
        List<HFDDVertex> neighbors = new LinkedList<>();
        for (int j = iNextCard; j < vertices.length; j++) {
          HFDDVertex vj = vertices[j];
          BitSet activityVj = vj.getVertexInfo().getActivities();
          // Subset of vi
          if (BitSetUtil.isSubset(activityVj, activityVi)) {
            // Not "dominated" by any of the neighbors so far
            if (!neighbors.stream().anyMatch(n -> BitSetUtil.isSubset(activityVj, n.getVertexInfo().getActivities()))) {
              g.addEdge(vj, vi);
              neighbors.add(vj);
            }
          }
        }
      }
    }
    return g;
  }

  /**
   * Given a list of vertices, this function build the graph structure. It
   * initializes a {@link Graph} with the given vertices and a-priori (set
   * inclusion) edges.
   * 
   * @param vertices Vertices
   * @return A-priori Graph
   */
  private static Graph<HFDDVertex, DefaultEdge> buildGraphStructureCoverRng(HFDDVertex[] vertices) {
    Graph<HFDDVertex, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
    // Add all vertices to the graph
    for (HFDDVertex v : vertices) {
      g.addVertex(v);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Add edges
    // Consider all edges such that the source itemset is contained in the target
    //////////////////////////////////////////////////////////////////////////////// itemset
    // Keep only those that are part of the transitive closure of the resulting
    //////////////////////////////////////////////////////////////////////////////// graph
    // Algorithm:
    // * Sort vertices descending order (activity set size)
    // * Iterate over smaller itemsets (in descending size!)
    // * Add an edge from a smaller itemset iff it covers at least an so far
    //////////////////////////////////////////////////////////////////////////////// uncovered
    //////////////////////////////////////////////////////////////////////////////// activity
    // !!!!! Not complete transitive closure !!!!!
    // {A, B, C} covered by {A, B}, {A, C} => will not add {B, C}
    ////////////////////////////////////////////////////////////////////////////////
    // Sort vertices (descending order)
    Arrays.sort(vertices, (v1, v2) -> -1 * Integer.compare(v1.getVertexInfo().getActivities().cardinality(),
        v2.getVertexInfo().getActivities().cardinality()));
    // Largest cardinality - 1
    int nextCard = vertices[0].getVertexInfo().getActivities().cardinality() - 1;
    // Index of next smaller cardinality
    // (initialized in first loop of iteration)
    int iNextCard = 0;
    // We are on the smallest cardinality
    boolean isSmallestCard = false;
    ////////////////////////////////////////
    // Start adding edges
    ////////////////////////////////////////
    HFDDVertex vi, vj;
    BitSet activityVi, activityVj, activityViUnCovered;
    for (int i = 0; !isSmallestCard; i++) {
      vi = vertices[i];
      activityVi = vi.getVertexInfo().getActivities();
      activityViUnCovered = (BitSet) activityVi.clone();

      // i.cardinality == next cardinality => SHIFT
      if (i == iNextCard) {
        while (iNextCard < vertices.length
            && vertices[iNextCard].getVertexInfo().getActivities().cardinality() > nextCard) {
          iNextCard++;
        }
        // No smaller cardinality found
        if (iNextCard == vertices.length) {
          isSmallestCard = true;
        } else {
          nextCard = vertices[iNextCard].getVertexInfo().getActivities().cardinality() - 1;
        }
      }

      if (!isSmallestCard) {
        // Add edges
        // Requirement: The set covers an uncovered activity
        for (int j = iNextCard; j < vertices.length && activityViUnCovered.cardinality() != 0; j++) {
          vj = vertices[j];
          activityVj = vj.getVertexInfo().getActivities();
          if (BitSetUtil.isSubset(activityVj, activityVi) && activityVj.intersects(activityViUnCovered)) {
            g.addEdge(vj, vi);
            activityViUnCovered.andNot(activityVj);
          }
        }
      }
    }
    return g;
  }

  /**
   * Mine / Discover interesting itemsets
   * 
   * @param dataSource Datasource
   * @return Sets of interesting activities
   * @throws SLDSTransformationError
   */
  protected abstract Collection<? extends Collection<Integer>> mineActivityItemsets(
      BiComparisonDataSource<? extends CVariant> dataSource) throws SLDSTransformationError;

}
