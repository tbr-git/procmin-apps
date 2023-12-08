package org.processmining.processcomparator.algorithms;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.processcomparator.controller.settings.SettingsController;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.model.SettingsObject;

/**
 * Static util methods for the use of settings
 * 
 * @author abolt
 *
 */
public class SettingsUtils {

	public static SettingsObject createSettingsObject(SettingsController controller) {
		//TODO this method should take a settingsController and it should produce an object that reflects the status of the settigns.
		return new SettingsObject(controller);
	}

	public static SettingsObject createSettingsObject(InputObject input, PluginContext pluginContext) {
		return new SettingsObject(input, pluginContext);
	}
}
