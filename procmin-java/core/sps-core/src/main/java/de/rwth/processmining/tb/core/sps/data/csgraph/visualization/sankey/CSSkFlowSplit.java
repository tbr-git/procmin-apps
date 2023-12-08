package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.rwth.processmining.tb.core.sps.data.csgraph.CSMeasurementTypes;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertexCS;

public class CSSkFlowSplit extends CSSkVertex {
	
	@JsonIgnore
	private CSGraphVertexCS csCornerstone;

	public CSSkFlowSplit(int id, boolean isLeft, CSGraphVertexCS csCornerstone) {
		// Probability mass of split vertex equals residual probability of parent
		super(id, isLeft, 
				isLeft ? csCornerstone.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL).left() :
					csCornerstone.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL).right(), 			
				isLeft ? csCornerstone.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL).left() :
					csCornerstone.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL).right(), 
				csCornerstone);
		this.csCornerstone = csCornerstone;
	}

	@Override
	public String toString() {
		return String.format("FlowSplit(id=%d, isLeft=%b, probMass=%f, refId=%d)", 
				getId(), this.isLeft(), this.getProbabilityMass(), this.getCsGraphVertex().getHfddVertexRef().getId());
	}
	
}
