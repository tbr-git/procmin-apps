package org.processmining.processcomparator.listeners.node;

import java.awt.MouseInfo;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.processcomparator.algorithms.Utils;
import org.processmining.processcomparator.controller.dialog.DecisionTreeDialogController;
import org.processmining.processcomparator.listeners.NodeListener;
import org.processmining.processcomparator.model.decisiontree.DecisionPoint;
import org.processmining.processcomparator.view.ComparatorPanel;
import org.processmining.processcomparator.view.dialog.decisiontree.DecisionPointDialog2;

import com.kitfox.svg.SVGDiagram;

public class DecisionTreeNodeListener extends NodeListener {

	private DecisionPoint dp;

	public DecisionTreeNodeListener(ComparatorPanel parentComponent, State s, DecisionPoint dp) {
		super(parentComponent, s);
		this.dp = dp;
	}

	public void selected(DotNode node, SVGDiagram image) {
		DotPanel.setCSSAttributeOf(DotPanel.getSVGElementOf(image, node).getChild(1), "stroke-dasharray", "5,2"); // 1 else

		DecisionPointDialog2 dialog = (DecisionPointDialog2) Utils.nodePopups.get(node);
		if (dialog != null) {
			dialog.setLocation(MouseInfo.getPointerInfo().getLocation());
			dialog.setVisible(true);
		} else {

			DecisionTreeDialogController dpController = null;
			try {
				dpController = new DecisionTreeDialogController(dp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Utils.nodePopups.put(node, dpController.getDialog());
		}
		refreshPanel();

	}

	public void deselected(DotNode element, SVGDiagram diagram) {
		DotPanel.setCSSAttributeOf(DotPanel.getSVGElementOf(diagram, element).getChild(1), "stroke-dasharray", ""); // 1 else
		DecisionPointDialog2 dialog = (DecisionPointDialog2) Utils.nodePopups.get(element);
		if (dialog != null)
			dialog.setVisible(false);

		refreshPanel();
	}

	private void refreshPanel() {
		parentComponent.doLayout();
		parentComponent.repaint();
	}
}
