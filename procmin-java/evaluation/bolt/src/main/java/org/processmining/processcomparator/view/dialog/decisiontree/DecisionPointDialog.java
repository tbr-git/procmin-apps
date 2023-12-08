//package org.processmining.processcomparator.view.dialog.decisiontree;
//
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.JDialog;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTabbedPane;
//import javax.swing.JTable;
//import javax.swing.JTextArea;
//import javax.swing.border.LineBorder;
//
//import org.processmining.processcomparator.controller.dialog.DecisionTreeDialogController;
//
//public class DecisionPointDialog extends JDialog {
//	public DecisionPointDialog(DecisionTreeDialogController dpController) {
//		setTitle("Decision Point Explorer");
//		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
//
//		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
//
//		//panels for the trees
//		JPanel treeA = dpController.getTreeVisualization_A();
//		treeA.setPreferredSize(new Dimension(500, 500));
//		treeA.setBorder(new LineBorder(Color.BLACK));
//		JPanel treeB = dpController.getTreeVisualization_B();
//		treeB.setPreferredSize(new Dimension(500, 500));
//		treeB.setBorder(new LineBorder(Color.BLACK));
//		JPanel agreementDisagreement = dpController.getTreeVisualization_AD();
//		agreementDisagreement.setPreferredSize(new Dimension(500, 500));
//		agreementDisagreement.setBorder(new LineBorder(Color.BLACK));
//
//		//self classification variables
//		double self_CorrectlyClassifiedPercentage_A = dpController.getSelfAccurracyA();
//		double self_CorrectlyClassifiedPercentage_B = dpController.getSelfAccurracyB();
//
//		double self_CorrectlyClassified_A = dpController.getCorrectInstancesGroupA();
//		double self_CorrectlyClassified_B = dpController.getCorrectInstancesGroupB();
//
//		double self_instances_A = dpController.getNumInstancesGroupA();
//		double self_instances_B = dpController.getNumInstancesGroupB();
//
//		String self_ConfusionMatrix_A = dpController.getSelfConfusionMatrixA();
//		String self_ConfusionMatrix_B = dpController.getSelfConfusionMatrixB();
//
//		Map<String, Double> self_fScores_A = dpController.getSelfFScoreA();
//		Map<String, Double> self_fScores_B = dpController.getSelfFScoreB();
//
//		//self classification components
//		JPanel selfClassificationPanel = new JPanel();
//		selfClassificationPanel.setLayout(new BoxLayout(selfClassificationPanel, BoxLayout.X_AXIS));
//		{
//			Box box1 = Box.createVerticalBox();
//			box1.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Decision Tree A (trained with Group A)");
//				label1.setFont(new Font("Tahoma", Font.BOLD, 16));
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box1.add(aux);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			box1.add(treeA);
//			box1.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Classification Results (tested with Group A)");
//				label1.setFont(new Font("Tahoma", Font.BOLD, 16));
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box1.add(aux);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel(
//						String.format("Correctly Classified Instances: %.2f", self_CorrectlyClassifiedPercentage_A)
//								+ "% " + String.format("(%.0f of %.0f total instances)", self_CorrectlyClassified_A,
//										self_instances_A));
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box1.add(aux);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Confusion Matrix");
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box1.add(aux);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				JTextArea confusionMatrix = new JTextArea();
//				confusionMatrix.setText(self_ConfusionMatrix_A);
//				confusionMatrix.setEditable(false);
//				box1.add(confusionMatrix);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("F-Score per Class");
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box1.add(aux);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				Object[] columnNames = new Object[] { "Class", "F-Score" };
//				Object[][] data = new Object[self_fScores_A.size()][2];
//				int i = 0;
//				for (Entry<String, Double> entry : self_fScores_A.entrySet()) {
//					data[i][0] = entry.getKey();
//					data[i][1] = entry.getValue();
//					i++;
//				}
//
//				JTable fScoreTable = new JTable(data, columnNames);
//				JScrollPane scrollPane = new JScrollPane(fScoreTable);
//				box1.add(scrollPane);
//			}
//			selfClassificationPanel.add(box1);
//
//			selfClassificationPanel.add(Box.createHorizontalStrut(5));
//
//			Box box2 = Box.createVerticalBox();
//			box2.add(Box.createVerticalStrut(5));
//
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Decision Tree B (trained with Group B)");
//				label1.setFont(new Font("Tahoma", Font.BOLD, 16));
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box2.add(aux);
//			}
//			box2.add(Box.createVerticalStrut(5));
//			box2.add(treeB);
//			box2.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Classification Results (tested with Group B)");
//				label1.setFont(new Font("Tahoma", Font.BOLD, 16));
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box2.add(aux);
//			}
//			box2.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel(
//						String.format("Correctly Classified Instances: %.2f", self_CorrectlyClassifiedPercentage_B)
//								+ "% " + String.format("(%.0f of %.0f total instances)", self_CorrectlyClassified_B,
//										self_instances_B));
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box2.add(aux);
//			}
//			box2.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Confusion Matrix");
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box2.add(aux);
//			}
//			box2.add(Box.createVerticalStrut(5));
//			{
//				JTextArea confusionMatrix = new JTextArea();
//				confusionMatrix.setText(self_ConfusionMatrix_B);
//				confusionMatrix.setEditable(false);
//				box2.add(confusionMatrix);
//			}
//			box2.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("F Score per Class");
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box2.add(aux);
//			}
//			box2.add(Box.createVerticalStrut(5));
//			{
//				Object[] columnNames = new Object[] { "Class", "F Score" };
//				Object[][] data = new Object[self_fScores_B.size()][2];
//				int i = 0;
//
//				for (Entry<String, Double> entry : self_fScores_B.entrySet()) {
//					data[i][0] = entry.getKey();
//					data[i][1] = entry.getValue();
//					i++;
//				}
//
//				JTable fScoreTable = new JTable(data, columnNames);
//				JScrollPane scrollPane = new JScrollPane(fScoreTable);
//				box2.add(scrollPane);
//			}
//			selfClassificationPanel.add(box2);
//		}
//		tabbedPane.addTab("Self-Classification", null, selfClassificationPanel, null);
//
//		//cross classification variables
//
//		double cross_CorrectlyClassifiedPercentage_TreeA_GroupB = dpController
//				.getCorrectlyClassifiedPercentage_TreeA_GroupB();
//		double cross_CorrectlyClassifiedPercentage_TreeB_GroupA = dpController
//				.getCorrectlyClassifiedPercentage_TreeB_GroupA();
//		
//		double cross_incorrectlyClassifiedPercentage_TreeA_GroupB = dpController
//				.getIncorrectlyClassifiedPercentage_TreeA_GroupB();
//		double cross_incorrectlyClassifiedPercentage_TreeB_GroupA = dpController
//				.getIncorrectlyClassifiedPercentage_TreeB_GroupA();
//
//		double cross_CorrectlyClassified_TreeA_GroupB = dpController.getCorrectlyClassified_TreeA_GroupB();
//		double cross_CorrectlyClassified_TreeB_GroupA = dpController.getCorrectlyClassified_TreeB_GroupA();
//
//		double correctlyClassifiedFScore_TreeA_GroupB = dpController.getCorrectlyClassifiedFScore_TreeA_GroupB();
//		double correctlyClassifiedFScore_TreeB_GroupA = dpController.getCorrectlyClassifiedFScore_TreeB_GroupA();
//		
//		double cross_incorrectlyClassified_TreeA_GroupB = dpController.getIncorrectlyClassified_TreeA_GroupB();
//		double cross_incorrectlyClassified_TreeB_GroupA = dpController.getIncorrectlyClassified_TreeB_GroupA();
//
//		double incorrectlyClassifiedFScore_TreeA_GroupB = dpController.getIncorrectlyClassifiedFScore_TreeA_GroupB();
//		double incorrectlyClassifiedFScore_TreeB_GroupA = dpController.getIncorrectlyClassifiedFScore_TreeB_GroupA();
//
//		String crossConfusionMatrixTreeA_GroupB = dpController.getCrossConfusionMatrixTreeA_GroupB();
//		String crossConfusionMatrixTreeB_GroupA = dpController.getCrossConfusionMatrixTreeB_GroupA();
//
//		Map<String, Double> crossFScoreTreeA_GroupB = dpController.getCrossFScoreTreeA_GroupB();
//		Map<String, Double> crossFScoreTreeB_GroupA = dpController.getCrossFScoreTreeB_GroupA();
//
//		JPanel crossClassificationPanel = new JPanel();
//		crossClassificationPanel.setLayout(new BoxLayout(crossClassificationPanel, BoxLayout.X_AXIS));
//		{
//			Box box0 = Box.createVerticalBox();
//			box0.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Textual Analysis");
//				label1.setFont(new Font("Tahoma", Font.BOLD, 16));
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box0.add(aux);
//			}
//			box0.add(Box.createVerticalStrut(5));
//			{
//				JTextArea confusionMatrix = new JTextArea();
//				confusionMatrix.setWrapStyleWord(true);
//				confusionMatrix.setLineWrap(true);
//				String text = String.format(
//						"From the %.0f instances of Group B that were correctly classified by the Decision "
//								+ "Tree B (of %.0f total instances), %.0f instances (%.2f percent) were also correctly "
//								+ "classified by Decision Tree A (F-Score = %.4f)",
//						self_CorrectlyClassified_B, self_instances_B, cross_CorrectlyClassified_TreeA_GroupB,
//						cross_CorrectlyClassifiedPercentage_TreeA_GroupB,correctlyClassifiedFScore_TreeA_GroupB);
//				confusionMatrix.setText(text);
//				confusionMatrix.setEditable(false);
//				box0.add(confusionMatrix);
//			}
//			box0.add(Box.createVerticalStrut(5));
//			{
//				JTextArea confusionMatrix = new JTextArea();
//				confusionMatrix.setWrapStyleWord(true);
//				confusionMatrix.setLineWrap(true);
//				String text = String.format(
//						"From the %.0f instances of Group B that were incorrectly classified by the Decision "
//								+ "Tree B (of %.0f total instances), %.0f instances (%.2f percent) were also incorrectly "
//								+ "classified by Decision Tree A (F-Score = %.4f)",
//								self_instances_B - self_CorrectlyClassified_B, self_instances_B, cross_incorrectlyClassified_TreeA_GroupB,
//						cross_incorrectlyClassifiedPercentage_TreeA_GroupB,incorrectlyClassifiedFScore_TreeA_GroupB);
//				confusionMatrix.setText(text);
//				confusionMatrix.setEditable(false);
//				box0.add(confusionMatrix);
//			}
//			box0.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Classification Results (Decision Tree A tested with Group B)");
//				label1.setFont(new Font("Tahoma", Font.BOLD, 16));
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box0.add(aux);
//			}
//			box0.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Confusion Matrix");
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box0.add(aux);
//			}
//			box0.add(Box.createVerticalStrut(5));
//			{
//				JTextArea confusionMatrix = new JTextArea();
//				confusionMatrix.setText(crossConfusionMatrixTreeA_GroupB);
//				confusionMatrix.setEditable(false);
//				box0.add(confusionMatrix);
//			}
//			box0.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("F-Score per Class");
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box0.add(aux);
//			}
//			box0.add(Box.createVerticalStrut(5));
//			{
//				Object[] columnNames = new Object[] { "Class", "F-Score" };
//				Object[][] data = new Object[crossFScoreTreeA_GroupB.size()][2];
//				int i = 0;
//				for (Entry<String, Double> entry : crossFScoreTreeA_GroupB.entrySet()) {
//					data[i][0] = entry.getKey();
//					data[i][1] = entry.getValue();
//					i++;
//				}
//
//				JTable fScoreTable = new JTable(data, columnNames);
//				JScrollPane scrollPane = new JScrollPane(fScoreTable);
//				box0.add(scrollPane);
//			}
//			crossClassificationPanel.add(box0);
//			crossClassificationPanel.add(Box.createHorizontalStrut(5));
//			
//			Box box1 = Box.createVerticalBox();
//			box1.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Textual Analysis");
//				label1.setFont(new Font("Tahoma", Font.BOLD, 16));
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box1.add(aux);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				JTextArea confusionMatrix = new JTextArea();
//				confusionMatrix.setWrapStyleWord(true);
//				confusionMatrix.setLineWrap(true);
//				String text = String.format(
//						"From the %.0f instances of Group A that were correctly classified by the Decision "
//								+ "Tree A (of %.0f total instances), %.0f instances (%.2f percent) were also correctly "
//								+ "classified by Decision Tree B (F-Score = %.4f)",
//						self_CorrectlyClassified_A, self_instances_A, cross_CorrectlyClassified_TreeB_GroupA,
//						cross_CorrectlyClassifiedPercentage_TreeB_GroupA,correctlyClassifiedFScore_TreeB_GroupA);
//				confusionMatrix.setText(text);
//				confusionMatrix.setEditable(false);
//				box1.add(confusionMatrix);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				JTextArea confusionMatrix = new JTextArea();
//				confusionMatrix.setWrapStyleWord(true);
//				confusionMatrix.setLineWrap(true);
//				String text = String.format(
//						"From the %.0f instances of Group A that were incorrectly classified by the Decision "
//								+ "Tree A (of %.0f total instances), %.0f instances (%.2f percent) were also incorrectly "
//								+ "classified by Decision Tree B (F-Score = %.4f)",
//								self_instances_A - self_CorrectlyClassified_A, self_instances_A, cross_incorrectlyClassified_TreeB_GroupA,
//						cross_incorrectlyClassifiedPercentage_TreeB_GroupA,incorrectlyClassifiedFScore_TreeB_GroupA);
//				confusionMatrix.setText(text);
//				confusionMatrix.setEditable(false);
//				box1.add(confusionMatrix);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Classification Results (Decision Tree B tested with Group A)");
//				label1.setFont(new Font("Tahoma", Font.BOLD, 16));
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box1.add(aux);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Confusion Matrix");
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box1.add(aux);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				JTextArea confusionMatrix = new JTextArea();
//				confusionMatrix.setText(crossConfusionMatrixTreeB_GroupA);
//				confusionMatrix.setEditable(false);
//				box1.add(confusionMatrix);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("F-Score per Class");
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box1.add(aux);
//			}
//			box1.add(Box.createVerticalStrut(5));
//			{
//				Object[] columnNames = new Object[] { "Class", "F-Score" };
//				Object[][] data = new Object[crossFScoreTreeB_GroupA.size()][2];
//				int i = 0;
//				for (Entry<String, Double> entry : crossFScoreTreeB_GroupA.entrySet()) {
//					data[i][0] = entry.getKey();
//					data[i][1] = entry.getValue();
//					i++;
//				}
//
//				JTable fScoreTable = new JTable(data, columnNames);
//				JScrollPane scrollPane = new JScrollPane(fScoreTable);
//				box1.add(scrollPane);
//			}
//			crossClassificationPanel.add(box1);
//		}
//		tabbedPane.addTab("Cross-Classification", null, crossClassificationPanel, null);
//
//		JPanel agreementsDisagreementsPanel = new JPanel();
//		agreementsDisagreementsPanel.setLayout(new BoxLayout(agreementsDisagreementsPanel, BoxLayout.X_AXIS));
//		{
//			Box box0 = Box.createVerticalBox();
//			box0.add(Box.createVerticalStrut(5));
//			{
//				box0.add(agreementDisagreement);
//			}
//			box0.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Tree String Description");
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box0.add(aux);
//			}
//			box0.add(Box.createVerticalStrut(5));
//			{
//				JTextArea confusionMatrix = new JTextArea();
//				confusionMatrix.setText(dpController.getTreeDescription());
//				confusionMatrix.setEditable(false);
//				box0.add(confusionMatrix);
//			}
//			box0.add(Box.createVerticalStrut(5));
//			{
//				Box aux = Box.createHorizontalBox();
//				aux.add(Box.createHorizontalGlue());
//				JLabel label1 = new JLabel("Confusion Matrix");
//				aux.add(label1);
//				aux.add(Box.createHorizontalGlue());
//				box0.add(aux);
//			}
//			box0.add(Box.createVerticalStrut(5));
//			{
//				JTextArea confusionMatrix = new JTextArea();
//				confusionMatrix.setText(dpController.getAgreeDisagreeConfusionMatrix());
//				confusionMatrix.setEditable(false);
//				box0.add(confusionMatrix);
//			}
//			agreementsDisagreementsPanel.add(box0);
//		}
//		tabbedPane.addTab("Agreements / Disagreements", null, agreementsDisagreementsPanel, null);
//
//		getContentPane().add(tabbedPane);
//	}
//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = -1716260267075768335L;
//}
