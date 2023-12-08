package org.processmining.processcomparator.controller.result;

import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.processcomparator.controller.result.abstr.ResultController;
import org.processmining.processcomparator.controller.result.wrapper.ResultsWrapperController;

/**
 * Controller for handling comparison results shown as a graph
 * 
 * @author abolt
 *
 */
public class GraphController implements ResultController<DotPanel> {

	private DotPanel graphPanel;
	@SuppressWarnings("unused")
	private ResultsWrapperController resultsWrapperController;

	public GraphController(ResultsWrapperController resultsWrapperController) {
		this.resultsWrapperController = resultsWrapperController;
	}

	public DotPanel getPanel() {
		return graphPanel;
	}

	public void setPanel(DotPanel panel) {
		graphPanel = panel;
	}

	//TODO add interaction listeners

}
