package org.processmining.processcomparator.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.plugins.transitionsystem.miner.TSMiner;
import org.processmining.plugins.transitionsystem.miner.TSMinerOutput;
import org.processmining.plugins.transitionsystem.miner.TSMinerTransitionSystem;
import org.processmining.plugins.transitionsystem.miner.ui.TSMinerUI;
import org.processmining.plugins.tsanalyzer2.AnnotatedTransitionSystem;
import org.processmining.plugins.tsanalyzer2.TSAnalyzer;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.model.ResultsObject;
import org.processmining.processcomparator.model.SettingsObject;
import org.processmining.processcomparator.model.TsSettingsObject;
import org.processmining.processcomparator.view.dialog.ProgressBarDialog;

/**
 * This class provides the functionalities to create annotated transition
 * systems from transitions systems and event logs.
 * 
 * @author abolt
 *
 */
public class TransitionSystemUtils {

	public static TSMinerTransitionSystem createTS(PluginContext pluginContext, TsSettingsObject settings, XLog log) {
		TSMiner miner = new TSMiner(pluginContext);
		if (settings == null)
			settings = new TsSettingsObject(Utils.getTSSettings(pluginContext, log));
		TSMinerOutput output = miner.mine(settings.getObject());
		return output.getTransitionSystem();
	}

	public static AnnotatedTransitionSystem createATS(PluginContext pluginContext, TSMinerTransitionSystem ts,
			XLog log) {
		TSAnalyzer analyzer = new TSAnalyzer(pluginContext, ts, log);
		AnnotatedTransitionSystem annotation = analyzer.annotate();
		return annotation;
	}

	public static TsSettingsObject createTSSettingsObject(PluginContext pluginContext, XLog log) {
		TSMinerUI gui = new TSMinerUI((UIPluginContext) pluginContext);
		List<XEventClassifier> stateClassifier = new ArrayList<XEventClassifier>();

		stateClassifier.add(new XEventNameClassifier());
		stateClassifier.add(new XEventLifeTransClassifier());
		stateClassifier.add(new XEventResourceClassifier());


		XEventClassifier transitionClassifier;

		transitionClassifier = new XEventNameClassifier();
		//transitionClassifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());

		return new TsSettingsObject(gui.getInputWithGUI(log, stateClassifier, transitionClassifier));
	}

	public static ResultsObject createResultsObject(PluginContext pluginContext, SettingsObject settings,
			InputObject input, ProgressBarDialog dialog) {

		long time = System.currentTimeMillis();

		TsSettingsObject tsSettings = null;
		if (!settings.isGraph_isDefaultSelected())
			tsSettings = settings.getGraph_tsSettings();
		else
			tsSettings = new TsSettingsObject(Utils.getTSSettings(pluginContext, input.getMerged()));

		TSMinerTransitionSystem ts = createTS(pluginContext, tsSettings, input.getMerged());

		if (dialog != null) {
			dialog.appendText("Transition System created (" + (System.currentTimeMillis() - time) / 1000 + " sec)");
			dialog.increment();
			time = System.currentTimeMillis();
		}

		AnnotatedTransitionSystem ats_A = createATS(pluginContext, ts, Utils.mergeLogs(input.getSelected_A()));
		if (dialog != null) {
			dialog.appendText("Annotated TS (1 of 3) created (" + (System.currentTimeMillis() - time) / 1000 + " sec)");
			dialog.increment();
			time = System.currentTimeMillis();
		}
		AnnotatedTransitionSystem ats_B = createATS(pluginContext, ts, Utils.mergeLogs(input.getSelected_B()));
		if (dialog != null) {
			dialog.appendText("Annotated TS (2 of 3) created (" + (System.currentTimeMillis() - time) / 1000 + " sec)");
			dialog.increment();
			time = System.currentTimeMillis();
		}
		AnnotatedTransitionSystem ats_Union = createATS(pluginContext, ts, input.getMerged());
		if (dialog != null) {
			dialog.appendText("Annotated TS (3 of 3) created (" + (System.currentTimeMillis() - time) / 1000 + " sec)");
			dialog.increment();
		}

		//set the translator based on this
		EventToStateTranslator.setTS(ts);
		EventToStateTranslator.setSettings(tsSettings);

		return new ResultsObject(ts, ats_A, ats_B, ats_Union);
	}

	public static Set<State> getNextStates(TSMinerTransitionSystem ts, State source) {
		
		Set<State> result = new TreeSet<State>();
		for(Transition t : ts.getEdges()){
			if(t.getSource().equals(source))
				result.add(t.getTarget());
		}
		return result;
	}
}
