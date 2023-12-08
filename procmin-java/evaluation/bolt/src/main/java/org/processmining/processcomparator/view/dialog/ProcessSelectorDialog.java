package org.processmining.processcomparator.view.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMTextArea;

import com.fluxicon.slickerbox.factory.SlickerFactory;

@SuppressWarnings("serial")
public class ProcessSelectorDialog extends JDialog {

	private final SlickerFactory factory = SlickerFactory.instance();
	private JButton btnGo, btnAvailable_to_A, btnAvailable_to_B, btnA_to_Available, btnB_to_Available, btnHints;
	
	private ProMList<String> list_processA, list_processB, list_available;
	
	private XLog[] processes;
	private Map<String,XLog> mapper;
	
	private DefaultListModel<String> model_A, model_B, model_available;
	
	public ProcessSelectorDialog(Container parent, XLog... pro) {
				
		//logic & data handling first
		processes = pro;
		
		mapper = new HashMap<String,XLog>();
		
		for(int i = 0 ; i < processes.length ; i ++)
		{
			mapper.put(processes[i].getAttributes().get("concept:name").toString(),processes[i]);
		}
		
		//now the drawing part
		setTitle("Merging step");		
		
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		getContentPane().setLayout(new BorderLayout());
		
		JPanel panel = factory.createGradientPanel();
		panel.setBounds(100, 100, 450, 300);	
		getContentPane().add(panel);
		panel.setLayout(null);
		
		
		model_A = new DefaultListModel<String>();
		model_B = new DefaultListModel<String>();
		model_available = new DefaultListModel<String>();
		
		for(String s : mapper.keySet())
			model_available.addElement(s);
		
		list_processA = new ProMList<String>("Create \"Group A\" by merging:",model_A);
		list_processA.setBounds(12, 217, 223, 260);
		panel.add(list_processA);
		
		list_available = new ProMList<String>("Available processes:",model_available);
		list_available.setBounds(280, 217, 223, 260);
		panel.add(list_available);
		
		list_processB = new ProMList<String>("Create \"Group B\" by merging:",model_B);
		list_processB.setBounds(547, 217, 223, 260);
		panel.add(list_processB);
		
		ProMTextArea textArea = new ProMTextArea();
		textArea.getTextArea().setFont(new Font("Serif", Font.PLAIN, 12));
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setText("Welcome to the Process Comparator Plugin!"
				+ "\r\n\r\nThis plugin allows the comparison of two groups of processes: A and B. "
				+ "This is done through merging the collections of traces included in the event log(s) selected for each group. "
				+ "\n\n The term \"% \u0394 freq\" is the percentage of edges of the graph that represents that process that present statistically significant "
				+ "differences in terms of frequence when compared against all the other processes together."
				+ " This metric is also calculated for performance (% \u0394 perf). "
				+ "\n\n The \"Give me a Hint\" button will select the process that has the highest percentage of statistically significant differences whith the rest of the processes in both states and transitions."
				+ " This provides a guidance on where to start looking for differences.");
		textArea.setBounds(12, 13, 758, 200);
		panel.add(textArea);
		
		btnGo = factory.createButton("Go!");
		btnGo.setBounds(433, 506, 97, 36);
		panel.add(btnGo);
		
		btnHints = factory.createButton("Give me a Hint");
		btnHints.setBounds(235, 506, 157, 36);
		panel.add(btnHints);
		
		JLabel lblProcessnameFreq = factory.createLabel("Format: process_name [ \u0394 % ]");
		lblProcessnameFreq.setHorizontalAlignment(SwingConstants.CENTER);
		lblProcessnameFreq.setBounds(235, 477, 311, 16);
		panel.add(lblProcessnameFreq);
		
		btnAvailable_to_A = new JButton("\u25C0");
		btnAvailable_to_A.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAvailable_to_A.setBackground(textArea.getBackground());
		btnAvailable_to_A.setForeground(Color.WHITE);
		btnAvailable_to_A.setContentAreaFilled(false);
		btnAvailable_to_A.setOpaque(true);
		btnAvailable_to_A.setBounds(235, 353, 43, 26);
		panel.add(btnAvailable_to_A);
		
		btnAvailable_to_A.setFont(new Font("Arial Unicode MS", Font.BOLD, 16));
		btnAvailable_to_A.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(String s: list_available.getSelectedValuesList())
				{
					model_A.addElement(s);
					model_available.remove(model_available.indexOf(s));
				}
				list_processA.validate();
				list_available.validate();
			}
		});
		
		
		btnAvailable_to_B = new JButton("\u25B6");
		btnAvailable_to_B.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAvailable_to_B.setBackground(textArea.getBackground());
		btnAvailable_to_B.setForeground(Color.WHITE);
		btnAvailable_to_B.setContentAreaFilled(false);
		btnAvailable_to_B.setOpaque(true);
		btnAvailable_to_B.setFont(new Font("Arial Unicode MS", Font.BOLD, 16));
		btnAvailable_to_B.setBounds(503, 322, 43, 26);
		panel.add(btnAvailable_to_B);
		
		btnAvailable_to_B.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(String s: list_available.getSelectedValuesList())
				{
					model_B.addElement(s);
					model_available.remove(model_available.indexOf(s));
				}
				list_processB.validate();
				list_available.validate();
			}
		});
		
		btnA_to_Available = new JButton("\u25B6");
		btnA_to_Available.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnA_to_Available.setBackground(textArea.getBackground());
		btnA_to_Available.setForeground(Color.WHITE);
		btnA_to_Available.setContentAreaFilled(false);
		btnA_to_Available.setOpaque(true);
		btnA_to_Available.setFont(new Font("Arial Unicode MS", Font.BOLD, 16));
		btnA_to_Available.setBounds(235, 322, 43, 26);
		panel.add(btnA_to_Available);
		
		btnA_to_Available.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(String s: list_processA.getSelectedValuesList())
				{
					model_available.addElement(s);
					model_A.remove(model_A.indexOf(s));
				}
				list_processA.validate();
				list_available.validate();
			}
		});
		
		btnB_to_Available = new JButton("\u25C0");
		btnB_to_Available.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnB_to_Available.setBackground(textArea.getBackground());
		btnB_to_Available.setForeground(Color.WHITE);
		btnB_to_Available.setContentAreaFilled(false);
		btnB_to_Available.setOpaque(true);
		btnB_to_Available.setFont(new Font("Arial Unicode MS", Font.BOLD, 16));
		btnB_to_Available.setBounds(503, 353, 43, 26);
		panel.add(btnB_to_Available);	
		
		btnB_to_Available.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(String s: list_processB.getSelectedValuesList())
				{
					model_available.addElement(s);
					model_B.remove(model_B.indexOf(s));
				}
				list_processB.validate();
				list_available.validate();
			}
		});
	}
	
	public void updateNames(byte[] values)
	{
		model_A.clear(); //clear all list models
		model_B.clear();
		model_available.clear();
		
		mapper.clear(); //clear the mapper
		
		for(int i = 0 ; i < processes.length ; i ++)
		{
			mapper.put(processes[i].getAttributes().get("concept:name").toString() + " ["+values[i]+"%]",processes[i]);
			System.out.println("length " + processes.length);
		}
		
		SortedSet<String> set = new TreeSet<String>(new NameComparator());
		
		set.addAll(mapper.keySet());			
		
		int counter = 0; // first element goes to list A, the rest to list B 
		for(String s : set)
		{
			if(counter == 0 )
			{
				model_A.addElement(s);
				counter++;
			}
			else
				model_B.addElement(s);
		}

		//repaint all lists
		list_processA.validate();
		list_processB.validate();
		list_available.validate();
	}
	
	public XLog[] getSelectedA()
	{
		XLog[] logs = new XLog[model_A.size()];
		
		for(int i = 0 ; i < logs.length ; i++)
			logs[i] = mapper.get(model_A.getElementAt(i));
		
		return logs;
	}
	public XLog[] getSelectedB()
	{		
		XLog[] logs = new XLog[model_B.size()];
		
		for(int i = 0 ; i < logs.length ; i++)
			logs[i] = mapper.get(model_B.getElementAt(i));
		
		return logs;
	}
	
	public void addGoButtonListener(ActionListener a)
	{
		btnGo.addActionListener(a);
	}
	public void addHintsButtonListener(ActionListener a)
	{
		btnHints.addActionListener(a);
	}
	
	class NameComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			// TODO Auto-generated method stub
			
			String s1 = o1.substring(o1.indexOf("[")).replaceAll("\\D+","");
			String s2 = o2.substring(o2.indexOf("[")).replaceAll("\\D+","");
			
			double p1 = Integer.parseInt(s1);
			double p2 = Integer.parseInt(s2);
			
			if(p1 > p2)
				return -1;
			else if (p1 < p2)
				return 1;
			else
				return o1.compareTo(o2);
		}

	}
}
