package org.processmining.processcomparator.controller.dialog;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.deckfour.xes.model.XLog;
import org.processmining.processcomparator.algorithms.DrawUtils;
import org.processmining.processcomparator.algorithms.SettingsUtils;
import org.processmining.processcomparator.algorithms.TransitionSystemUtils;
import org.processmining.processcomparator.controller.MainController;
import org.processmining.processcomparator.model.InputObject;
import org.processmining.processcomparator.model.ResultsObject;
import org.processmining.processcomparator.model.SettingsObject;
import org.processmining.processcomparator.view.dialog.ProcessSelectorDialog;
import org.processmining.processcomparator.view.dialog.ProgressBarDialog;

public class ProcessSelectorController {

	private MainController parent;
	private ProcessSelectorDialog dialog;
	private InputObject input;

	private Thread thread;

	public ProcessSelectorController(MainController parent, InputObject input) {
		this.parent = parent;
		this.input = input;
		initialize();
	}

	private void initialize() {

		dialog = new ProcessSelectorDialog(parent.getPanel(), input.getLogArray());

		dialog.addGoButtonListener(new GoButtonListener());
		dialog.addHintsButtonListener(new HintButtonListener());
		dialog.setLocationRelativeTo(parent.getPanel());

		dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setAlwaysOnTop(true);
		showSelector();
	}

	public void showSelector() {
		assert dialog != null;

		dialog.setVisible(true);

		input.setSelected_A(Arrays.asList(dialog.getSelectedA()));
		input.setSelected_B(Arrays.asList(dialog.getSelectedB()));
		dialog.dispose();
	}

	class HintButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			final XLog[] logs = input.getLogArray();
			final List<Triple<Integer, Integer, Double>> triplets = new ArrayList<Triple<Integer, Integer, Double>>();

			int combinations = 0;
			if (logs.length > 2)
				combinations = factorial(logs.length) / (2 * (factorial(logs.length - 2)));
			else
				combinations = 1;
			final ProgressBarDialog pb = new ProgressBarDialog(combinations);

			new Thread() { //thread for painting the progress bar
				public void run() {
					pb.addButtonListener(new Cancel(pb));
					pb.setAlwaysOnTop(true);
					pb.setUndecorated(true);
					pb.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
					pb.setLocationRelativeTo(dialog);

					pb.setVisible(true);
				}
			}.start();

			new Thread() { //thread for making the calculations
				public void run() {
					long time = 0;
					XLog logA = null;
					XLog logB = null;

					for (int i = 0; i < logs.length; i++)
						for (int j = i + 1; j < logs.length; j++) {
							time = System.currentTimeMillis();
							logA = logs[i];
							logB = logs[j];
							pb.appendText("START comparison (" + logA.getAttributes().get("concept:name").toString()
									+ " vs " + logB.getAttributes().get("concept:name").toString() + ")");

							InputObject input = new InputObject(logs);
							List<XLog> aux = new ArrayList<XLog>();
							aux.add(logA);
							input.setSelected_A(aux);
							List<XLog> aux2 = new ArrayList<XLog>();
							aux2.add(logB);
							input.setSelected_B(aux2);

							SettingsObject settings = SettingsUtils.createSettingsObject(input,
									parent.getPluginContext());
							ResultsObject results = TransitionSystemUtils.createResultsObject(parent.getPluginContext(),
									settings, input, pb);

							triplets.add(new ImmutableTriple<Integer, Integer, Double>(i, j,
									DrawUtils.get_perentageOfDifferences(
											DrawUtils.createGraph(results, settings, parent.getPanel(), input))));

							pb.appendText("END comparison (" + logA.getAttributes().get("concept:name").toString()
									+ " vs " + logB.getAttributes().get("concept:name").toString() + "): took "
									+ (System.currentTimeMillis() - time) / 1000 + " sec");
							pb.setProgress(pb.getProgress() + 1);

						}

					pb.dispose();
					//dialog.updateNames(values);

					//sort on the "difference" value
					Collections.sort(triplets, new Comparator<Triple<Integer, Integer, Double>>() {
						@Override
						public int compare(final Triple<Integer, Integer, Double> o1,
								final Triple<Integer, Integer, Double> o2) {
							return (o1.getRight().compareTo(o2.getRight())) * -1;
						}
					});

					String rankedList = "";

					for (Triple<Integer, Integer, Double> t : triplets) {
						rankedList = rankedList + "\n" + String.format("(%02.2f", t.getRight()) + "%) "
								+ logs[t.getLeft()].getAttributes().get("concept:name").toString() + " vs "
								+ logs[t.getMiddle()].getAttributes().get("concept:name").toString();
					}

					JOptionPane.showMessageDialog(dialog,
							"The following list ranks pairs of event logs based on their percentage "
							+ "of control-flow differences. \n We recommend that you select the highest "
							+ "ranked pair as a starting point:" + rankedList,
							"Pair-wise comparison recommendation", JOptionPane.PLAIN_MESSAGE);

					dialog.revalidate();
				}
			}.start();
		}
	}

	class GoButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			dialog.dispose();
		}
	}

	class Cancel implements ActionListener {
		ProgressBarDialog pb;

		public Cancel(ProgressBarDialog p) {
			pb = p;
		}

		@SuppressWarnings("deprecation")
		public void actionPerformed(ActionEvent e) {
			pb.setVisible(false);
			pb.dispose();
			thread.stop(); //not safe... but screw you ProM for not letting me do it naturally!!!
		}

	}

	public static int factorial(int n) {
		int fact = 1; // this  will be the result
		for (int i = 1; i <= n; i++) {
			fact *= i;
		}
		return fact;
	}
}
