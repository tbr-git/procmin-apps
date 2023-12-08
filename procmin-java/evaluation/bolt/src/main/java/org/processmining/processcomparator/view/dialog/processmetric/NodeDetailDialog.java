package org.processmining.processcomparator.view.dialog.processmetric;

import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.processmining.processcomparator.algorithms.Utils;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class NodeDetailDialog extends JDialog implements WindowListener {

	/**
	 * to show the details when you click on an edge
	 */
	private static final long serialVersionUID = 821344617822342372L;

	private int posX, posY;
	private JTextField textField_15;
	private JTextField textField_16;
	private JTextField textField_17;
	private JTextField textField_18;
	private JTextField textField_19;
	private JTextField textField_20;
	private JTextField textField_21;
	private JTextField textField_22;
	private JTextField textField_23;
	private JTextField textField_24;
	private JTextField textField_25;
	private JTextField textField_26;
	private JTextField textField_28;

	public NodeDetailDialog(String s_label, double R_f_a, double R_f_v, double R_e_a, double R_e_v, double R_r_a,
			double R_r_v, double R_s_a, double R_s_v, double R_n, double A_f_a, double A_f_v, double A_e_a,
			double A_e_v, double A_r_a, double A_r_v, double A_s_a, double A_s_v, double A_n, double B_f_a,
			double B_f_v, double B_e_a, double B_e_v, double B_r_a, double B_r_v, double B_s_a, double B_s_v,
			double B_n) {

		setSize(459, 320);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);

		SlickerFactory factory = SlickerFactory.instance();

		JPanel panel = factory.createRoundedPanel();
		panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panel.setBounds(0, 0, 350, 150);
		panel.setLayout(null);

		getContentPane().add(panel);

		JLabel lblProcessGroupA = factory.createLabel("Group A");
		lblProcessGroupA.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblProcessGroupA.setHorizontalAlignment(SwingConstants.CENTER);
		lblProcessGroupA.setBounds(248, 53, 97, 16);
		panel.add(lblProcessGroupA);

		JLabel lblMeanFrequency = factory.createLabel("Freq avg:");
		lblMeanFrequency.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMeanFrequency.setBounds(12, 77, 120, 16);
		panel.add(lblMeanFrequency);

		JLabel lblProcessGroupB = factory.createLabel("Group B");
		lblProcessGroupB.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblProcessGroupB.setHorizontalAlignment(SwingConstants.CENTER);
		lblProcessGroupB.setBounds(352, 53, 97, 16);
		panel.add(lblProcessGroupB);

		JLabel lblPerfAvg = factory.createLabel("Elapsed (t) avg:");
		lblPerfAvg.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPerfAvg.setBounds(12, 130, 120, 16);
		panel.add(lblPerfAvg);

		JLabel lblPerfStdDev = factory.createLabel("Elapsed (t) std dev:");
		lblPerfStdDev.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPerfStdDev.setBounds(12, 154, 120, 16);
		panel.add(lblPerfStdDev);

		JTextField textField_7 = new JTextField(Utils.ConvertSecondToHHMMString((long) R_e_v));
		textField_7.setHorizontalAlignment(SwingConstants.LEFT);
		textField_7.setCaretPosition(0);
		textField_7.setEditable(false);
		textField_7.setColumns(10);
		textField_7.setBounds(144, 150, 97, 22);
		panel.add(textField_7);

		JTextField textField_8 = new JTextField(Utils.ConvertSecondToHHMMString((long) R_e_a));
		textField_8.setHorizontalAlignment(SwingConstants.LEFT);
		textField_8.setCaretPosition(0);
		textField_8.setEditable(false);
		textField_8.setColumns(10);
		textField_8.setBounds(144, 126, 97, 22);
		panel.add(textField_8);

		JTextField textField_10 = new JTextField(Utils.getAsPercentage(R_f_a));
		textField_10.setHorizontalAlignment(SwingConstants.CENTER);
		textField_10.setEditable(false);
		textField_10.setColumns(10);
		textField_10.setBounds(144, 73, 97, 22);
		panel.add(textField_10);

		JLabel lblReference = factory.createLabel("Reference");
		lblReference.setHorizontalAlignment(SwingConstants.CENTER);
		lblReference.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblReference.setBounds(144, 53, 97, 16);
		panel.add(lblReference);

		JLabel lblObservations = factory.createLabel("# observations:");
		lblObservations.setHorizontalAlignment(SwingConstants.TRAILING);
		lblObservations.setBounds(12, 286, 120, 16);
		panel.add(lblObservations);

		JTextField textField = new JTextField(String.valueOf((long) R_n));
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setEditable(false);
		textField.setColumns(10);
		textField.setBounds(144, 282, 97, 22);
		panel.add(textField);

		JTextField textField_1 = new JTextField(Utils.getAsPercentage(A_f_a));
		textField_1.setHorizontalAlignment(SwingConstants.CENTER);
		textField_1.setEditable(false);
		textField_1.setColumns(10);
		textField_1.setBounds(248, 73, 97, 22);
		panel.add(textField_1);

		JTextField textField_3 = new JTextField(Utils.ConvertSecondToHHMMString((long) A_e_a));
		textField_3.setHorizontalAlignment(SwingConstants.LEFT);
		textField_3.setCaretPosition(0);
		textField_3.setEditable(false);
		textField_3.setColumns(10);
		textField_3.setBounds(248, 126, 97, 22);
		panel.add(textField_3);

		JTextField textField_4 = new JTextField(Utils.ConvertSecondToHHMMString((long) A_e_v));
		textField_4.setHorizontalAlignment(SwingConstants.LEFT);
		textField_4.setCaretPosition(0);
		textField_4.setEditable(false);
		textField_4.setColumns(10);
		textField_4.setBounds(248, 150, 97, 22);
		panel.add(textField_4);

		JTextField textField_5 = new JTextField(String.valueOf((long) A_n));
		textField_5.setHorizontalAlignment(SwingConstants.CENTER);
		textField_5.setEditable(false);
		textField_5.setColumns(10);
		textField_5.setBounds(248, 282, 97, 22);
		panel.add(textField_5);

		JTextField textField_6 = new JTextField(Utils.getAsPercentage(B_f_a));
		textField_6.setHorizontalAlignment(SwingConstants.CENTER);
		textField_6.setEditable(false);
		textField_6.setColumns(10);
		textField_6.setBounds(352, 73, 97, 22);
		panel.add(textField_6);

		JTextField textField_12 = new JTextField(Utils.ConvertSecondToHHMMString((long) B_e_a));
		textField_12.setHorizontalAlignment(SwingConstants.LEFT);
		textField_12.setCaretPosition(0);
		textField_12.setEditable(false);
		textField_12.setColumns(10);
		textField_12.setBounds(352, 126, 97, 22);
		panel.add(textField_12);

		JTextField textField_13 = new JTextField(Utils.ConvertSecondToHHMMString((long) B_e_v));
		textField_13.setHorizontalAlignment(SwingConstants.LEFT);
		textField_13.setCaretPosition(0);
		textField_13.setEditable(false);
		textField_13.setColumns(10);
		textField_13.setBounds(352, 150, 97, 22);
		panel.add(textField_13);

		JTextField textField_14 = new JTextField(String.valueOf((long) B_n));
		textField_14.setHorizontalAlignment(SwingConstants.CENTER);
		textField_14.setEditable(false);
		textField_14.setColumns(10);
		textField_14.setBounds(352, 282, 97, 22);
		panel.add(textField_14);

		JLabel lblSourceStateLabel = factory.createLabel("State Label:");
		lblSourceStateLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		lblSourceStateLabel.setBounds(12, 34, 81, 16);
		panel.add(lblSourceStateLabel);

		JLabel lblRemainingtAvg = factory.createLabel("Remaining (t) avg:");
		lblRemainingtAvg.setHorizontalAlignment(SwingConstants.TRAILING);
		lblRemainingtAvg.setBounds(12, 184, 120, 16);
		panel.add(lblRemainingtAvg);

		textField_15 = new JTextField(Utils.ConvertSecondToHHMMString((long) R_r_a));
		textField_15.setHorizontalAlignment(SwingConstants.LEFT);
		textField_15.setCaretPosition(0);
		textField_15.setEditable(false);
		textField_15.setColumns(10);
		textField_15.setBounds(144, 178, 97, 22);
		panel.add(textField_15);

		textField_16 = new JTextField(Utils.ConvertSecondToHHMMString((long) A_r_a));
		textField_16.setHorizontalAlignment(SwingConstants.LEFT);
		textField_16.setCaretPosition(0);
		textField_16.setEditable(false);
		textField_16.setColumns(10);
		textField_16.setBounds(248, 178, 97, 22);
		panel.add(textField_16);

		textField_17 = new JTextField(Utils.ConvertSecondToHHMMString((long) B_r_a));
		textField_17.setHorizontalAlignment(SwingConstants.LEFT);
		textField_17.setCaretPosition(0);
		textField_17.setEditable(false);
		textField_17.setColumns(10);
		textField_17.setBounds(352, 178, 97, 22);
		panel.add(textField_17);

		JLabel lblRemainingtStd = factory.createLabel("Remaining (t) std dev:");
		lblRemainingtStd.setHorizontalAlignment(SwingConstants.TRAILING);
		lblRemainingtStd.setBounds(12, 208, 120, 16);
		panel.add(lblRemainingtStd);

		textField_18 = new JTextField(Utils.ConvertSecondToHHMMString((long) R_r_v));
		textField_18.setHorizontalAlignment(SwingConstants.LEFT);
		textField_18.setCaretPosition(0);
		textField_18.setEditable(false);
		textField_18.setColumns(10);
		textField_18.setBounds(144, 204, 97, 22);
		panel.add(textField_18);

		textField_19 = new JTextField(Utils.ConvertSecondToHHMMString((long) A_r_v));
		textField_19.setHorizontalAlignment(SwingConstants.LEFT);
		textField_19.setCaretPosition(0);
		textField_19.setEditable(false);
		textField_19.setColumns(10);
		textField_19.setBounds(248, 204, 97, 22);
		panel.add(textField_19);

		textField_20 = new JTextField(Utils.ConvertSecondToHHMMString((long) B_r_v));
		textField_20.setHorizontalAlignment(SwingConstants.LEFT);
		textField_20.setCaretPosition(0);
		textField_20.setEditable(false);
		textField_20.setColumns(10);
		textField_20.setBounds(352, 204, 97, 22);
		panel.add(textField_20);

		JLabel lblSoujourntAvg = factory.createLabel("Soujourn (t) avg:");
		lblSoujourntAvg.setHorizontalAlignment(SwingConstants.TRAILING);
		lblSoujourntAvg.setBounds(12, 234, 120, 16);
		panel.add(lblSoujourntAvg);

		textField_21 = new JTextField(Utils.ConvertSecondToHHMMString((long) R_s_a));
		textField_21.setHorizontalAlignment(SwingConstants.LEFT);
		textField_21.setCaretPosition(0);
		textField_21.setEditable(false);
		textField_21.setColumns(10);
		textField_21.setBounds(144, 230, 97, 22);
		panel.add(textField_21);

		textField_22 = new JTextField(Utils.ConvertSecondToHHMMString((long) A_s_a));
		textField_22.setHorizontalAlignment(SwingConstants.LEFT);
		textField_22.setCaretPosition(0);
		textField_22.setEditable(false);
		textField_22.setColumns(10);
		textField_22.setBounds(248, 230, 97, 22);
		panel.add(textField_22);

		textField_23 = new JTextField(Utils.ConvertSecondToHHMMString((long) B_s_a));
		textField_23.setHorizontalAlignment(SwingConstants.LEFT);
		textField_23.setCaretPosition(0);
		textField_23.setEditable(false);
		textField_23.setColumns(10);
		textField_23.setBounds(352, 230, 97, 22);
		panel.add(textField_23);

		JLabel lblSoujourntStd = factory.createLabel("Soujourn (t) std dev:");
		lblSoujourntStd.setHorizontalAlignment(SwingConstants.TRAILING);
		lblSoujourntStd.setBounds(12, 258, 120, 16);
		panel.add(lblSoujourntStd);

		textField_24 = new JTextField(Utils.ConvertSecondToHHMMString((long) R_s_v));
		textField_24.setHorizontalAlignment(SwingConstants.LEFT);
		textField_24.setCaretPosition(0);
		textField_24.setEditable(false);
		textField_24.setColumns(10);
		textField_24.setBounds(144, 254, 97, 22);
		panel.add(textField_24);

		textField_25 = new JTextField(Utils.ConvertSecondToHHMMString((long) A_s_v));
		textField_25.setHorizontalAlignment(SwingConstants.LEFT);
		textField_25.setCaretPosition(0);
		textField_25.setEditable(false);
		textField_25.setColumns(10);
		textField_25.setBounds(248, 254, 97, 22);
		panel.add(textField_25);

		textField_26 = new JTextField(Utils.ConvertSecondToHHMMString((long) B_s_v));
		textField_26.setHorizontalAlignment(SwingConstants.LEFT);
		textField_26.setCaretPosition(0);
		textField_26.setEditable(false);
		textField_26.setColumns(10);
		textField_26.setBounds(352, 254, 97, 22);
		panel.add(textField_26);

		textField_28 = new JTextField(s_label);
		textField_28.setColumns(10);
		textField_28.setEditable(false);
		textField_28.setBounds(105, 30, 344, 22);
		panel.add(textField_28);

		enableDragging(this);
	}

	private void enableDragging(Component component) {

		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				posX = e.getX();
				posY = e.getY();
			}
		});

		component.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent evt) {
				//sets frame position when mouse dragged            
				Rectangle rectangle = getBounds();
				NodeDetailDialog.this.setBounds(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY,
						rectangle.width, rectangle.height);
			}
		});
	}

	public void windowActivated(WindowEvent arg0) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
		toFront();
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}
