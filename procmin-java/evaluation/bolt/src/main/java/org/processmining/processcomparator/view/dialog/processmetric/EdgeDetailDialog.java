package org.processmining.processcomparator.view.dialog.processmetric;

import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.processmining.processcomparator.algorithms.Utils;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class EdgeDetailDialog extends JDialog{
	
	/**
	 * to show the details when you click on an edge
	 */
	private static final long serialVersionUID = 8213446178266549472L;
	
	private int posX, posY;
	private JTextField textField_15;
	private JTextField textField_16;
	private JTextField textField_17;
	
	
	public EdgeDetailDialog(String Tran_label, String Sour_label, String Targ_label,
							double R_f_a, double R_f_s, double R_d_a, double R_d_s, double R_n,
							double A_f_a, double A_f_s, double A_d_a, double A_d_s, double A_n,
							double B_f_a, double B_f_s, double B_d_a, double B_d_s, double B_n) {
		
		setSize(430,255);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		
		SlickerFactory factory = SlickerFactory.instance();
		
		JPanel panel = factory.createRoundedPanel();
		panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panel.setBounds(0,0,350,150);
		panel.setLayout(null);
		
		getContentPane().add(panel);
				
		JLabel lblProcessGroupA = factory.createLabel("Group A");
		lblProcessGroupA.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblProcessGroupA.setHorizontalAlignment(SwingConstants.CENTER);
		lblProcessGroupA.setBounds(218, 95, 97, 16);
		panel.add(lblProcessGroupA);
		
		JLabel lblMeanFrequency = factory.createLabel("Freq avg:");
		lblMeanFrequency.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMeanFrequency.setBounds(12, 119, 98, 16);
		panel.add(lblMeanFrequency);
		
		JLabel lblProcessGroupB = factory.createLabel("Group B");
		lblProcessGroupB.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblProcessGroupB.setHorizontalAlignment(SwingConstants.CENTER);
		lblProcessGroupB.setBounds(322, 95, 97, 16);
		panel.add(lblProcessGroupB);
		
		JLabel lblPerfAvg = factory.createLabel("Duration avg:");
		lblPerfAvg.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPerfAvg.setBounds(12, 172, 98, 16);
		panel.add(lblPerfAvg);
		
		JLabel lblPerfStdDev = factory.createLabel("Duration std dev:");
		lblPerfStdDev.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPerfStdDev.setBounds(12, 196, 98, 16);
		panel.add(lblPerfStdDev);
		
		JTextField textField_7 = new JTextField(Utils.ConvertSecondToHHMMString((long) R_d_s));
		textField_7.setHorizontalAlignment(SwingConstants.LEFT);
		textField_7.setCaretPosition(0);
		textField_7.setEditable(false);
		textField_7.setColumns(10);
		textField_7.setBounds(114, 192, 97, 22);
		panel.add(textField_7);
		
		JTextField textField_8 = new JTextField(Utils.ConvertSecondToHHMMString((long) R_d_a));
		textField_8.setHorizontalAlignment(SwingConstants.LEFT);
		textField_8.setCaretPosition(0);
		textField_8.setEditable(false);
		textField_8.setColumns(10);
		textField_8.setBounds(114, 168, 97, 22);
		panel.add(textField_8);
		
		JTextField textField_10 = new JTextField(Utils.getAsPercentage(R_f_a));
		textField_10.setHorizontalAlignment(SwingConstants.CENTER);
		textField_10.setEditable(false);
		textField_10.setColumns(10);
		textField_10.setBounds(114, 115, 97, 22);
		panel.add(textField_10);
		
		JLabel lblReference = factory.createLabel("Reference");
		lblReference.setHorizontalAlignment(SwingConstants.CENTER);
		lblReference.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblReference.setBounds(114, 95, 97, 16);
		panel.add(lblReference);
		
		JLabel lblObservations = factory.createLabel("# observations:");
		lblObservations.setHorizontalAlignment(SwingConstants.TRAILING);
		lblObservations.setBounds(12, 225, 98, 16);
		panel.add(lblObservations);
		
		JTextField textField = new JTextField(String.valueOf((long)R_n));
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setEditable(false);
		textField.setColumns(10);
		textField.setBounds(114, 221, 97, 22);
		panel.add(textField);
		
		JTextField textField_1 = new JTextField(Utils.getAsPercentage(A_f_a));
		textField_1.setHorizontalAlignment(SwingConstants.CENTER);
		textField_1.setEditable(false);
		textField_1.setColumns(10);
		textField_1.setBounds(218, 115, 97, 22);
		panel.add(textField_1);
		
		JTextField textField_3 = new JTextField(Utils.ConvertSecondToHHMMString((long) A_d_a));
		textField_3.setHorizontalAlignment(SwingConstants.LEFT);
		textField_3.setCaretPosition(0);
		textField_3.setEditable(false);
		textField_3.setColumns(10);
		textField_3.setBounds(218, 168, 97, 22);
		panel.add(textField_3);
		
		JTextField textField_4 = new JTextField(Utils.ConvertSecondToHHMMString((long) A_d_s));
		textField_4.setHorizontalAlignment(SwingConstants.LEFT);
		textField_4.setCaretPosition(0);
		textField_4.setEditable(false);
		textField_4.setColumns(10);
		textField_4.setBounds(218, 192, 97, 22);
		panel.add(textField_4);
		
		JTextField textField_5 = new JTextField(String.valueOf((long)A_n));
		textField_5.setHorizontalAlignment(SwingConstants.CENTER);
		textField_5.setEditable(false);
		textField_5.setColumns(10);
		textField_5.setBounds(218, 221, 97, 22);
		panel.add(textField_5);
		
		JTextField textField_6 = new JTextField(Utils.getAsPercentage(B_f_a));
		textField_6.setHorizontalAlignment(SwingConstants.CENTER);
		textField_6.setEditable(false);
		textField_6.setColumns(10);
		textField_6.setBounds(322, 115, 97, 22);
		panel.add(textField_6);
		
		JTextField textField_12 = new JTextField(Utils.ConvertSecondToHHMMString((long) B_d_a));
		textField_12.setHorizontalAlignment(SwingConstants.LEFT);
		textField_12.setCaretPosition(0);
		textField_12.setEditable(false);
		textField_12.setColumns(10);
		textField_12.setBounds(322, 168, 97, 22);
		panel.add(textField_12);
		
		JTextField textField_13 = new JTextField(Utils.ConvertSecondToHHMMString((long) B_d_s));
		textField_13.setHorizontalAlignment(SwingConstants.LEFT);
		textField_13.setCaretPosition(0);
		textField_13.setEditable(false);
		textField_13.setColumns(10);
		textField_13.setBounds(322, 192, 97, 22);
		panel.add(textField_13);
		
		JTextField textField_14 = new JTextField(String.valueOf((long)B_n));
		textField_14.setHorizontalAlignment(SwingConstants.CENTER);
		textField_14.setEditable(false);
		textField_14.setColumns(10);
		textField_14.setBounds(322, 221, 97, 22);
		panel.add(textField_14);
		
		JLabel lblTransitionLabel = factory.createLabel("Transition Label:");
		lblTransitionLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		lblTransitionLabel.setBounds(12, 12, 111, 16);
		panel.add(lblTransitionLabel);
		
		JLabel lblSourceStateLabel = factory.createLabel("Source State Label:");
		lblSourceStateLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		lblSourceStateLabel.setBounds(12, 37, 111, 16);
		panel.add(lblSourceStateLabel);
		
		JLabel lblTargetStateLabel = factory.createLabel("Target State Label:");
		lblTargetStateLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		lblTargetStateLabel.setBounds(12, 63, 111, 16);
		panel.add(lblTargetStateLabel);
		
		textField_15 = new JTextField(Tran_label);
		textField_15.setBounds(124, 8, 295, 22);
		textField_15.setEditable(false);
		panel.add(textField_15);
		textField_15.setColumns(10);
		
		textField_16 = new JTextField(Sour_label);
		textField_16.setColumns(10);
		textField_16.setBounds(124, 33, 295, 22);
		textField_16.setEditable(false);
		panel.add(textField_16);
		
		textField_17 = new JTextField(Targ_label);
		textField_17.setColumns(10);
		textField_17.setBounds(124, 59, 295, 22);
		textField_17.setEditable(false);
		panel.add(textField_17);
		
		enableDragging(this); //check if this works, if not, use panel instead of "this"
	}
	
	private void enableDragging(Component component){
	    
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
	            EdgeDetailDialog.this.setBounds(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY, rectangle.width, rectangle.height);
	        }
	    });
	}
}
