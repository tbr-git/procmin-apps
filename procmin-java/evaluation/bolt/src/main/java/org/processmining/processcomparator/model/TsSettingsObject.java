package org.processmining.processcomparator.model;

import org.processmining.plugins.transitionsystem.miner.TSMinerInput;

public class TsSettingsObject {

	private TSMinerInput tsSettings;

	public TsSettingsObject(TSMinerInput input) {
		tsSettings = input;
	}

	public TSMinerInput getObject() {
		return tsSettings;
	}

	public boolean isEqual(TsSettingsObject o1) {
		return this.equals(o1);
	}

}
