package org.processmining.processcomparator.controller.result.wrapper;

import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.processcomparator.controller.MainController;
import org.processmining.processcomparator.controller.result.GraphController;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.model.ResultsObject;
import org.processmining.processcomparator.model.SettingsObject;
import org.processmining.processcomparator.view.result.wrapper.ResultsWrapperPanel;

public class ResultsWrapperController {

	private ResultsWrapperPanel resultsWrapperPanel;

	// Results Object
	@SuppressWarnings("unused")
	private ResultsObject results;

	// Controllers
	@SuppressWarnings("unused")
	private MainController mainController;
	private GraphController graphController;

	public ResultsWrapperController(MainController mainController) {
		this.mainController = mainController;
		resultsWrapperPanel = new ResultsWrapperPanel(null);
		graphController = new GraphController(this);
	}

	public ResultsWrapperPanel getPanel() {
		return resultsWrapperPanel;
	}

	public void updateContents(DotPanel graph) {
		graphController.setPanel(graph);
		resultsWrapperPanel.setContents(graphController.getPanel());
		
		resultsWrapperPanel.doLayout();
		resultsWrapperPanel.repaint();		
	}

	public void calculateResults(InputObject input, SettingsObject settings) {

		results = null;
		//TODO finishes with updateContents(JPanel p1, JPanel p2)
	}

}
