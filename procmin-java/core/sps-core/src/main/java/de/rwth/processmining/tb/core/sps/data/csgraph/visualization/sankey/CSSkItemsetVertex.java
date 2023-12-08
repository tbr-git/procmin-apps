package de.rwth.processmining.tb.core.sps.data.csgraph.visualization.sankey;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

import de.rwth.processmining.tb.core.data.variantlog.base.CategoryMapper;
import de.rwth.processmining.tb.core.sps.data.csgraph.CSMeasurementTypes;
import de.rwth.processmining.tb.core.sps.data.csgraph.graph.CSGraphVertex;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertexInfo;

public class CSSkItemsetVertex extends CSSkVertex {
	
	private String[] activityItemset;

	public CSSkItemsetVertex(int id, boolean isLeft, CSGraphVertex csGraphVertex) {
		super(id, isLeft, 
				isLeft ? csGraphVertex.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL).left() :
					csGraphVertex.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL).right(), 			
				isLeft ? csGraphVertex.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL).left() :
					csGraphVertex.getProbabilityMassInfo(CSMeasurementTypes.RESIDUAL).right(), 
				csGraphVertex);

		// Extract translated itemset of activities 
		HFDDVertexInfo vInfo = csGraphVertex.getHfddVertexRef().getVertexInfo();
		CategoryMapper cm = vInfo.getCategoryMapper();
		this.activityItemset = vInfo.getActivities().stream().mapToObj(cm::getActivity4Category).toArray(String[]::new);
	}

	@Override
	public void formatActivities(Comparator<String> itemSorter, Function<String, String> activityAbbrev) {
		super.formatActivities(itemSorter, activityAbbrev);
		// Sort "itemset"
		Arrays.sort(activityItemset, itemSorter);
	
		// Abbreviate activity names
		for (int i = 0; i < activityItemset.length; i++) {
			activityItemset[i] = activityAbbrev.apply(activityItemset[i]);
		}
	}

	@Override
	public String toString() {
		return String.format("ItemsetVertex(id=%d, isLeft=%b, probMass=%f, refId=%d, itemset=<%s>)", 
				getId(), this.isLeft(), this.getProbabilityMass(), this.getCsGraphVertex().getHfddVertexRef().getId(),
				String.join(", ", activityItemset));
	}

	public String[] getActivityItemset() {
		return activityItemset;
	}

	public void setActivityItemset(String[] activityItemset) {
		this.activityItemset = activityItemset;
	}
}
