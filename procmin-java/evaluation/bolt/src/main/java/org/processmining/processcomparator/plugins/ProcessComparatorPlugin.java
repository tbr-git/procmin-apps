package org.processmining.processcomparator.plugins;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.processcomparator.controller.MainController;
import org.processmining.processcomparator.help.YourHelp;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.view.ComparatorPanel;

@Plugin(name = "Process Comparator", level = PluginLevel.PeerReviewed, parameterLabels = { "Event Array",
		"Process Comparator Settings" }, returnLabels = {
				"Process Comparator Panel" }, returnTypes = { ComparatorPanel.class }, help = YourHelp.TEXT)
public class ProcessComparatorPlugin {

	/**
	 * Version WITH UI, DEFAULT PARAMETERS, expecting an event log array
	 * 
	 * @param context
	 * @param input
	 *            = the set of logs to be used
	 * @return a comparator panel
	 */
	@UITopiaVariant(affiliation = "Eindhoven University of Technology", author = "Alfredo Bolt", email = "a.bolt@tue.nl")
	@PluginVariant(variantLabel = "Process Comparator, without UI, default parameters, set of logs", requiredParameterLabels = {
			0 })
	public ComparatorPanel runDefault(UIPluginContext context, XLog... input) {

		// Apply the algorithm 
		try {
			return run(context, input);
		} catch (IndexOutOfBoundsException e) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}

	/**
	 * Apply the algorithm depending on whether a connection already exists.
	 * 
	 * @param context
	 *            The context to run in.
	 * @param input1
	 *            The first input.
	 * @param input2
	 *            The second input.
	 * @return The output.
	 */
	public ComparatorPanel run(PluginContext context, XLog... logs) {
		InputObject input = new InputObject(logs);

		Set<String> logNames = new HashSet<String>();
		for (XLog log : logs)
			logNames.add(log.getAttributes().get("concept:name").toString());

		if (logNames.size() != logs.length) {
			JOptionPane.showMessageDialog(null, "Duplicated names found. Please make sure that all the names of the event logs are distinct", "Error", JOptionPane.ERROR_MESSAGE);
			throw new IndexOutOfBoundsException();
		}
		
		MainController mainController = new MainController(context, input);

		return mainController.getPanel();
	}

}
