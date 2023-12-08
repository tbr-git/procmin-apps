package org.processmining.processcomparator.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.processmining.processcomparator.view.result.wrapper.ResultsWrapperPanel;
import org.processmining.processcomparator.view.settings.SettingsPanel;

/**
 * This class acts like a visual wrapper, holding both the settings panel and
 * the results panel. This class was created to use a specific renderer that
 * only works with this class.
 * 
 * @author abolt
 *
 */
public class ComparatorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4341311269659817514L;

	SettingsPanel settingsPanel;
	ResultsWrapperPanel resultsWrapperPanel;

	public ComparatorPanel() {
		setLayout(new BorderLayout());

	}

	public ComparatorPanel(SettingsPanel settings, ResultsWrapperPanel results) {
		this();
		setSettingsPanel(settings);
		setResultsWrapperPanel(results);
	}

	public SettingsPanel getSettingsPanel() {
		return settingsPanel;
	}

	public void setSettingsPanel(SettingsPanel settingsPanel) {
		if (this.settingsPanel != null)
			this.remove(this.settingsPanel);
		this.settingsPanel = settingsPanel;
		add(this.settingsPanel, BorderLayout.EAST);
		update();
	}

	public ResultsWrapperPanel getResultsWrapperPanel() {
		return resultsWrapperPanel;
	}

	public void setResultsWrapperPanel(ResultsWrapperPanel resultsWrapperPanel) {

		if (this.resultsWrapperPanel != null)
			this.remove(this.resultsWrapperPanel);
		this.resultsWrapperPanel = resultsWrapperPanel;
		add(this.resultsWrapperPanel, BorderLayout.CENTER);
		update();
	}
	
	private void update()
	{		
		doLayout();
		repaint();
	}
}
