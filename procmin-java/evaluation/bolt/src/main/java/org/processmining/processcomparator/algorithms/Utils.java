package org.processmining.processcomparator.algorithms;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JDialog;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.transitionsystem.miner.TSMinerInput;

public class Utils {

	private static XEventClassifier classifier;
	private static int dateParser = 0;
	private static int matrixSize;
	public static Map<DotEdge, JDialog> edgePopups = new HashMap<DotEdge, JDialog>();
	public static Map<DotNode, JDialog> nodePopups = new HashMap<DotNode, JDialog>();

	//methods for handling the event classifier
	public static void setClassifier(XEventClassifier clas) {
		classifier = clas;
	}

	public static XEventClassifier getClassifier() {
		return classifier;
	}

	public static String getClassifierValue(XEvent event) {
		XAttributeMap attmap = event.getAttributes();
		String[] keys = classifier.getDefiningAttributeKeys();

		String result = ""; //stores the joint values when the event classifier is composed by more than one attributes

		for (String k : keys) {
			if (k != null && !k.isEmpty() && attmap.get(k) != null) {
				result += attmap.get(k).toString();
				result += "+";
			}
		}

		if (result.matches(""))
			result = null;

		else if (result.startsWith("+"))
			result.substring(1);

		else if (result.endsWith("+"))
			result.substring(0, result.length() - 1);

		return result;
	}

	public static long getTimestampValue(XEvent event) //returns the timestamp as a long in seconds
	{
		long result;
		if (dateParser == 0)
			dateParser = detectTimestampParser(event.getAttributes().get("time:timestamp").toString());

		try {
			result = change_date_format(event.getAttributes().get("time:timestamp").toString()) / 1000;
		} catch (Exception e) {
			dateParser = detectTimestampParser(event.getAttributes().get("time:timestamp").toString());
			result = change_date_format(event.getAttributes().get("time:timestamp").toString()) / 1000;
		}
		return result;
	}

	//methods for handling the matrix size
	public static void setMatrixSize(int size) {
		matrixSize = size;
	}

	public static int getMatrixSize() {
		return matrixSize;
	}

	public static double[][] createMatrix() //returns a matrix filled with 0s
	{

		double[][] result = new double[matrixSize][matrixSize];

		for (int i = 0; i < result.length; i++)
			for (int j = 0; j < result.length; j++)
				result[i][j] = 0;
		return result;
	}

	private static int detectTimestampParser(String input) {
		DateFormat df;
		Date aux = null;
		try {
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return 1;
		} catch (ParseException e) {
		}

		try {
			df = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return 2;
		} catch (ParseException e) {
		}

		try {
			df = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return 3;
		} catch (ParseException e) {
		}

		try {
			df = new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return 4;
		} catch (ParseException e) {
		}

		try {
			df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return 5;
		} catch (ParseException e) {
		}

		try {
			df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			aux = df.parse(input);
			aux.getTime();
			return 6;
		} catch (ParseException e) {
		}

		try {
			df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			aux = df.parse(input);
			aux.getTime();
			return 7;
		} catch (ParseException e) {
		}

		try {
			df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			aux = df.parse(input);
			aux.getTime();
			return 8;
		} catch (ParseException e) {
		}

		return 0;
	}

	public static long change_date_format(String input) {

		DateFormat df = null;
		long result;
		try {
			switch (dateParser) {
				case 1 :
					df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					break;
				case 2 :
					df = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");
					break;
				case 3 :
					df = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");
					break;
				case 4 :
					df = new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss");
					break;
				case 5 :
					df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					break;
				case 6 :
					df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					break;
				case 7 :
					df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
					break;
				case 8 :
					df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					break;
				default :
					df = null;
			}

			result = df.parse(input).getTime();
			df = null;
			return result;
		} catch (ParseException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static void printer(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++)
				System.out.print(matrix[i][j] + "\t");
			System.out.print("\n");
		}
		System.out.print("\n");
	}

	public static String getColor(byte option, byte type) {
		//type is 1 for frequency, 2 for performance
		//options go from -4 standard deviations to +4 standard deviations
		String answer = "";

		if (option < -5)
			option = -4; //negative limit
		if (option > 5)
			option = 4; //positive limit

		//frequency colors
		if (type == 1)
			switch (option) {
				case -4 :
					answer = "#a50026"; //dark red
					break;
				case -3 :
					answer = "#d73027";
					break;
				case -2 :
					answer = "#f46d43";
					break;
				case -1 :
					answer = "#fdae61"; //light red
					break;

				//there is no case 0, since those are black

				case 1 :
					answer = "#abd9e9"; //light blue
					break;
				case 2 :
					answer = "#74add1";
					break;
				case 3 :
					answer = "#4575b4";
					break;
				case 4 :
					answer = "#313695"; //dark blue
					break;
			}

		//performance colors
		if (type == 2)
			switch (option) {
				case -4 :
					answer = "#40004b"; //dark purple
					break;
				case -3 :
					answer = "#762a83";
					break;
				case -2 :
					answer = "#9970ab";
					break;
				case -1 :
					answer = "#c2a5cf"; //light purple
					break;

				//there is no case 0, since those are black

				case 1 :
					answer = "#a6dba0"; //light green
					break;
				case 2 :
					answer = "#5aae61";
					break;
				case 3 :
					answer = "#1b7837";
					break;
				case 4 :
					answer = "#00441b"; //dark green
					break;
			}
		return answer;
	}

	/**
	 * moved to StatisticsUtils
	 * 
	 * @param a
	 * @param b
	 * @param alpha
	 * @return
	 */
	@Deprecated
	public static boolean mannUTest(DescriptiveStatistics a, DescriptiveStatistics b, double alpha) {

		//if there is no variance, the means are different
		if (a.getVariance() == 0 && b.getVariance() == 0) {
			if (a.getMean() != b.getMean())
				return true;
			else
				return false;
		}

		MannWhitneyUTest test = new MannWhitneyUTest();
		double p = 1;
		try {
			p = test.mannWhitneyUTest(a.getValues(), b.getValues());
		} catch (NoDataException e) {
			return true;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (p <= alpha)
			return true;
		return false;
	}

	/**
	 * moved to StatisticsUtils
	 * 
	 * @param a
	 * @param b
	 * @param alpha
	 * @return
	 */
	@Deprecated
	public static boolean welchTTest(DescriptiveStatistics a, DescriptiveStatistics b, double alpha) //this method checks if there is a significant difference in the means of two samples (Welch's T two-tailed test)
	{

		//if there is no variance, the means are different
		if (a == null || b == null)
			return true;

		if (a.getVariance() == 0 && b.getVariance() == 0) {
			if (a.getMean() != b.getMean())
				return true;
			else
				return false;
		}

		double t = (a.getMean() - b.getMean()) / Math.sqrt((a.getVariance() / a.getN()) + (b.getVariance() / b.getN()));
		double degrees_of_freedom = Math.rint(Math.pow((a.getVariance() / a.getN()) + (b.getVariance() / b.getN()), 2)
				/ ((Math.pow(a.getVariance(), 2) / (Math.pow(a.getN(), 2) * (a.getN() - 1)))
						+ (Math.pow(b.getVariance(), 2) / (Math.pow(b.getN(), 2) * (b.getN() - 1)))));

		if (t > 0)
			t = 0 - Math.abs(t);

		TDistribution t_dist = new TDistribution(degrees_of_freedom);

		double p_value = t_dist.cumulativeProbability(t);

		if (p_value <= alpha / 2) //two tailed check
		{
			//System.out.println(p_value);
			return true;
		}

		else
			return false;
	}

	public static String ConvertSecondToHHMMString(long milisecondtTime) {

		long secondsTime = milisecondtTime / 1000;
		long miliseconds = milisecondtTime % 1000;
		long seconds = secondsTime % 60;
		long minutes = (secondsTime / 60) % 60;
		long hours = (secondsTime / 3600) % 24;
		long days = secondsTime / 86400;
		return "" + days + "d, " + String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":"
				+ String.format("%02d", seconds) + "." + String.format("%03d", miliseconds);
	}

	public static String getAsPercentage(double input) {
		DecimalFormat format = new DecimalFormat("###.##%");
		return format.format(input);
	}

	public static void highlightEdge(DotEdge edge, int color, int type) {
		edge.setOption("color", Utils.getColor((byte) color, (byte) type));
		edge.setOption("fontcolor", Utils.getColor((byte) color, (byte) type));
	}

	public static void highlightEdge(DotEdge edge, int color1, int type1, int color2, int type2) {
		edge.setOption("color",
				Utils.getColor((byte) color1, (byte) type1) + ":" + Utils.getColor((byte) color2, (byte) type2));
	}

	public static void highlightNode(DotNode node, int color, int type) {
		node.setOption("style", "filled");
		node.setOption("fillcolor", Utils.getColor((byte) color, (byte) type));
	}

	public static void highlightNode(DotNode node, int color1, int type1, int color2, int type2) {
		node.setOption("style", "wedged");
		node.setOption("fillcolor",
				Utils.getColor((byte) color1, (byte) type1) + ";0.5:" + Utils.getColor((byte) color2, (byte) type2));
	}

	public static XLog mergeLogs(XLog... logs) {
		XLog result = new XLogImpl(logs[0].getAttributes());
		for (XLog l : logs)
			for (XTrace t : l)
				result.add(t);

		return result;
	}

	public static XLog mergeLogs(List<XLog> logs) {
		XLog result = new XLogImpl(logs.get(0).getAttributes());
		for (XLog l : logs)
			for (XTrace t : l)
				result.add(t);

		return result;
	}

	public static void hideAllPopups() {
		for (JDialog dialog : edgePopups.values())
			dialog.setVisible(false);
		for (JDialog dialog : nodePopups.values())
			dialog.setVisible(false);
	}

	public static TSMinerInput getTSSettings(PluginContext context, XLog log) {
		List<XEventClassifier> stateClassifier = new Vector<XEventClassifier>();

		stateClassifier.add(new XEventNameClassifier());
//		stateClassifier.add(new XEventLifeTransClassifier());
//		stateClassifier.add(new XEventResourceClassifier());

		XEventClassifier transitionClassifier;

		transitionClassifier = new XEventNameClassifier();
		//transitionClassifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());

		TSMinerInput settings = new TSMinerInput(context, log, stateClassifier, transitionClassifier);
		settings.setMaxStates(-1);
		
		return settings;
	}

	public static List<String> getAttributeList(XLog log) {
		Set<String> attributeSet = new TreeSet<String>();

		for (XTrace t : log)
			for (XEvent e : t)
				attributeSet.addAll(e.getAttributes().keySet());

		attributeSet.remove("concept:name");

		List<String> result = new ArrayList<String>();
		result.addAll(attributeSet);

		return result;
	}
}
