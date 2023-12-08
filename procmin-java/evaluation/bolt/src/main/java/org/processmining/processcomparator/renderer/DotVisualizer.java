package org.processmining.processcomparator.renderer;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

@Plugin(name = "Visualize Dot Panel", level = PluginLevel.PeerReviewed, parameterLabels = { "DotPanel" }, returnLabels = {
		"visualization" }, returnTypes = { JComponent.class })
@Visualizer
public class DotVisualizer {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, DotPanel output) {
		return output;
	}

}
