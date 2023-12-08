package org.processmining.processcomparator.algorithms.runners;

import org.processmining.processcomparator.algorithms.TransitionSystemUtils;
import org.processmining.processcomparator.controller.MainController;
import org.processmining.processcomparator.controller.settings.SettingsController;
import org.processmining.processcomparator.model.TsSettingsObject;
import org.processmining.processcomparator.view.dialog.ProgressBarDialog;

public class TsSettingsRunner implements Runnable {

	ProgressBarDialog dialog;
	SettingsController settingsController;

	public TsSettingsRunner(ProgressBarDialog d, SettingsController settingsController) {

		if (d == null)
			dialog = new ProgressBarDialog(1);
		else
			dialog = d;

		this.settingsController = settingsController;
	}

	public void run() {

		dialog.setVisible(true);

		MainController main = settingsController.getMainController();

		dialog.toBack();

		TsSettingsObject settings = TransitionSystemUtils.createTSSettingsObject(main.getPluginContext(),
				main.getInput().getMerged());

		settingsController.getGraphSettingsController().setTsSettings(settings);

		dialog.toFront();
		dialog.dispose();
	}
}