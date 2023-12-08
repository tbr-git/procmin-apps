package org.processmining.processcomparator.controller.settings.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.processmining.processcomparator.controller.settings.SettingsController;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.view.settings.component.VariantSettingsPanel;

public class VariantSettingsController {

	private SettingsController parent;
	private VariantSettingsPanel panel;

	private InputObject input;

	public VariantSettingsController(SettingsController parent, InputObject input) {
		this.parent = parent;
		this.input = input;
		initialize();
	}

	/**
	 * initialize controller: listeners, etc...
	 */
	private void initialize() {
		panel = new VariantSettingsPanel(input);
		panel.getButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openVariantSelector();
			}
		});
		panel.repaint();
	}

	/**
	 * when the open variant button has been clicked...
	 */
	private void openVariantSelector() {
		parent.openVariantSelector();
		panel.updateLists(getInput());	

	}

	public JPanel getPanel() {
		return panel;
	}

	public InputObject getInput() {
		return input;
	}

}
