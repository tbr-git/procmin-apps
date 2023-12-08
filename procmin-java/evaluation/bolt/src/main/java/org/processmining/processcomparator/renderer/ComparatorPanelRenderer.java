package org.processmining.processcomparator.renderer;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.processcomparator.view.ComparatorPanel;

@Plugin(name = "Visualize Comparator Panel", level = PluginLevel.PeerReviewed, parameterLabels = { "ComparatorPanel" }, returnLabels = {
		"visualization" }, returnTypes = { JComponent.class })
@Visualizer
public class ComparatorPanelRenderer {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, ComparatorPanel output) {
		return output;
	}
}
