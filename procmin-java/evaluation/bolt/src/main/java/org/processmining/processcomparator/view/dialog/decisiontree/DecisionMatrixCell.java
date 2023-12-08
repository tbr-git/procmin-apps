package org.processmining.processcomparator.view.dialog.decisiontree;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class DecisionMatrixCell extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 454112517671331032L;

	final static String CHART_NAME = "pieChart", CONFMATRIX_NAME = "confusionMatrix";

	private JButton showChart, showConfusionMatrix;

	public DecisionMatrixCell(double correct, double incorrect, String correctLabel, String incorrectLabel,
			double[][] confMatrix, Object[] classes) {

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(300,300));

		final JPanel container;
		JPanel buttons, chart, confusionMatrix;

		container = new JPanel();
		container.setLayout(new CardLayout());

		chart = makePieChart(correct, incorrect, correctLabel, incorrectLabel);
		container.add(chart, CHART_NAME);

		confusionMatrix = makeConfusionMatrix(classes, confMatrix);
		container.add(confusionMatrix, CONFMATRIX_NAME);

		add(container, BorderLayout.CENTER);

		//buttons
		buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		showChart = new JButton("Show Pie Chart");
		showChart.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				CardLayout c = (CardLayout) container.getLayout();
				c.show(container, CHART_NAME);
				container.doLayout();
				container.repaint();
			}

		});
		showConfusionMatrix = new JButton("Show Confusion Matrix");
		showConfusionMatrix.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				CardLayout c = (CardLayout) container.getLayout();
				c.show(container, CONFMATRIX_NAME);
				container.doLayout();
				container.repaint();
			}

		});

		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(showChart);
		box.add(Box.createHorizontalGlue());
		box.add(showConfusionMatrix);
		box.add(Box.createHorizontalGlue());
		buttons.add(box);

		add(buttons, BorderLayout.SOUTH);
	}

	private ChartPanel makePieChart(double correct, double incorrect, String correctLabel, String incorrectLabel) {
		DefaultPieDataset dataset = new DefaultPieDataset();

		dataset.setValue(incorrectLabel, incorrect);
		dataset.setValue(correctLabel, correct);

		JFreeChart pieChart = ChartFactory.createPieChart("Classification Results", dataset, true, true, false);
		PiePlot plot = (PiePlot) pieChart.getPlot();
		plot.setLabelGenerator(null);
		ChartPanel result = new ChartPanel(pieChart);
		//result.setPreferredSize(new Dimension(200, 150));
		return result;
	}

	private JPanel makeConfusionMatrix(Object[] classes, double[][] confusionMatrix) {

		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		

		String[] headers = new String[classes.length + 1];
		headers[0] = "Classified as -->";
		for (int i = 0; i < classes.length; i++) {
			headers[i + 1] = classes[i].toString();
		}
		Object[][] data = new Object[classes.length][classes.length + 1];
		//add row names
		for (int i = 0; i < classes.length; i++) {
			data[i][0] = classes[i].toString();
		}
		for(int i = 0 ; i < confusionMatrix.length ; i++)
			for(int j = 0 ; j < confusionMatrix.length ; j++){
				data[i][j + 1] = confusionMatrix[i][j];
			}
		
		JTable table = new JTable(data,headers);

		result.add(new JScrollPane(table), BorderLayout.CENTER);

		return result;
	}

}
