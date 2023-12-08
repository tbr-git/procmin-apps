package org.processmining.processcomparator.model;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.processcomparator.algorithms.Utils;

/**
 * Utility class to wrap inputs. It also holds the selections of event logs into
 * groups A and B.
 * 
 * @author abolt
 *
 */
public class InputObject {

	private XLog[] logs;

	private XLog merged;

	private List<XLog> selected_A, selected_B;

	public InputObject(XLog... input) {
		logs = input;
		merged = Utils.mergeLogs(input);
	}

	public int getInputSize() {
		return logs.length;
	}

	public XLog getLog(int index) {
		assert index < logs.length && index >= 0;
		return logs[index];
	}

	public XLog[] getLogArray() {
		return logs;
	}

	public List<XLog> getSelected_A() {
		return selected_A;
	}

	public void setSelected_A(List<XLog> list) {
		selected_A = list;
		updateMerged();
	}

	public List<XLog> getSelected_B() {
		return selected_B;
	}

	public void setSelected_B(List<XLog> list) {
		selected_B = list;
		updateMerged();
	}

	public XLog getMerged() {
		if (merged == null) {
			updateMerged();
		}
		return merged;
	}

	private void updateMerged() {
		List<XLog> aux = new ArrayList<XLog>();
		if (selected_A != null)
			aux.addAll(selected_A);
		if (selected_B != null)
			aux.addAll(selected_B);
		merged = Utils.mergeLogs(aux);
	}
}
