package de.rwth.processmining.tb.core.sps.data.csgraph.graph;

import java.util.EnumMap;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;

import de.rwth.processmining.tb.core.emd.language.transformer.ProbMassNonEmptyTrace;
import de.rwth.processmining.tb.core.sps.data.csgraph.CSMeasurementTypes;
import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey.CSSkEdge;
import de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey.CSSkVertex;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;

public abstract sealed class CSGraphVertex permits CSGraphVertexCS, CSGraphVertexSupport {
	private static final Logger logger = LogManager.getLogger(CSGraphVertex.class);
	
	/**
	 * Reference to the corresponding HFDDVertex.
	 */
	private final HFDDVertex hfddVertexRef;
	
	/**
	 * The probability mass associated with non-empty traces for the given measurment key.
	 */
	private final EnumMap<CSMeasurementTypes, ProbMassNonEmptyTrace> probMassesNonEmptyTrace;
	
	public CSGraphVertex(HFDDVertex hfddVertexRef) {
		super();
		this.hfddVertexRef = hfddVertexRef;
		this.probMassesNonEmptyTrace = new EnumMap<>(CSMeasurementTypes.class);
	}

	public HFDDVertex getHfddVertexRef() {
		return hfddVertexRef;
	}
	
	/**
	 * Hashing only considers {@link this#hfddVertexRef#id};
	 */
	@Override
	public int hashCode() {
		return this.hfddVertexRef.getId();
	}

	/**
	 * Equality only considers {@link this#hfddVertexRef#id};
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(!obj.getClass().equals(this.getClass())) {
			return false;
		}
		else {
			CSGraphVertex other = (CSGraphVertex) obj;
			return other.getHfddVertexRef().getId() == this.hfddVertexRef.getId();
		}
	}
	
	public abstract Pair<? extends CSSkVertex, ? extends CSSkVertex> createAndAddSankeySubgraphs(Graph<CSSkVertex, CSSkEdge> g, 
			Iterator<Integer> idGenerator);

	@Override
	public String toString() {
		return String.format("CSVertex(items = {%s})", String.join(", ", hfddVertexRef.getVertexInfo().getItemsetHumanReadable()));
	}

	public void setProbabilityMassInfo(CSMeasurementTypes type, ProbMassNonEmptyTrace probInfo) {
		this.probMassesNonEmptyTrace.put(type, probInfo);
	}

	public ProbMassNonEmptyTrace getProbabilityMassInfo(CSMeasurementTypes type) {
		return this.probMassesNonEmptyTrace.get(type);
	}
	
}
