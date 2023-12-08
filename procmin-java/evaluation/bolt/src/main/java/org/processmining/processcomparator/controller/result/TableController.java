package org.processmining.processcomparator.controller.result;

import javax.swing.JPanel;

import org.processmining.processcomparator.controller.result.abstr.ResultController;
import org.processmining.processcomparator.controller.result.wrapper.ResultsWrapperController;
import org.processmining.processcomparator.view.result.TablePanel;

/**
 * Controller for handling comparison results shown as a table
 * 
 * @author abolt
 *
 */
public class TableController implements ResultController<JPanel> {

	private TablePanel tablePanel;
	@SuppressWarnings("unused")
	private ResultsWrapperController resultsWrapperController;

	public TableController(ResultsWrapperController resultsWrapperController) {
		this.resultsWrapperController = resultsWrapperController;
	}

	public JPanel getPanel() {
		return tablePanel;
	}

	public void setPanel(JPanel panel) {
		tablePanel = (TablePanel) panel;

	}

}
