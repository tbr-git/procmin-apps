package org.processmining.processcomparator.view.result;

import javax.swing.JPanel;

/**
 * The TablePanel shows a table with the most relevant differences. These
 * differences are ranked by some criteria that can be defined by the user.
 * 
 * @author abolt
 *
 */
public class TablePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3098680707529840939L;

	JPanel content;

	public TablePanel(JPanel content) {
		this.content = content;
	}

	public JPanel getPanel() {
		return content;
	}
}
