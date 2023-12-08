package de.rwth.processmining.tb.core.sps.algorithm.spsvertex;

import java.util.BitSet;

import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;
import de.rwth.processmining.tb.core.util.bitsets.BitSetUtil;

public class SPSVertexOp {

  /**
   * Get the set of activities that desribes the joint vertex.
   *
   * @param u First vertex
   * @param v Second vertex
   * @return BitSet describing the vertex corresponding to the occurrence of <b>u and v</b>
   */
  public static BitSet getJointVertexDescriptor(HFDDVertex u, HFDDVertex v) {
      BitSet activitiesV = v.getVertexInfo().getActivities();
      BitSet activitiesU = u.getVertexInfo().getActivities();
      BitSet tmp = (BitSet) activitiesV.clone();

      tmp.or(activitiesU);
      return tmp;
  }

  /**
   * Determine the relation <b>u "rel" v</b>:
   *
	 * <p><ul>
   * <li> u, v are equivalent ("u = v")
	 * <li> v specialization of u ("u < v")
	 * <li> v generalization of u ("u > v")
	 * <li> u, v unrelated
	 * </ul>
   *
   * @param u Generalized vertex 
   * @param v Specialized vertex
   *
   * @return Relation between u and v
   */
  public static HFDDVertexRelation determineVertexRelation(HFDDVertex u, HFDDVertex v) {
    BitSet activitiesU = u.getVertexInfo().getActivities();
    BitSet activitiesV = v.getVertexInfo().getActivities();
    if (activitiesU.equals(activitiesV)) {
      return HFDDVertexRelation.EQUIVALENT;
    }
    else if (BitSetUtil.isSubset(activitiesU, activitiesV)) {
      return HFDDVertexRelation.SPECIALIZATION;
    }
    else if (BitSetUtil.isSubset(activitiesV, activitiesU)) {
      return HFDDVertexRelation.GENERALIZATON;
    }
    else {
      return HFDDVertexRelation.UNRELATED;
    }
  }

  /**
   * Structural vertex similarity. (Jaccard index of activity sets)
   *
   * @param u Vertex
   * @param v Vertex
   *
   * @return Jaccard index of activity sets
   */
  public static double vertexSimilarity(HFDDVertex u, HFDDVertex v) {
    BitSet activitiesU = u.getVertexInfo().getActivities();
    BitSet activitiesV = v.getVertexInfo().getActivities();

    return BitSetUtil.jaccardIndex(activitiesU, activitiesV);
  }
}

