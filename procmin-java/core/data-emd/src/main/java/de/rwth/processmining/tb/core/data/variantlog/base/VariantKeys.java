package de.rwth.processmining.tb.core.data.variantlog.base;


public class VariantKeys {
	
	public int compositeKey;
	
	public VariantKeys(int compositeKey) {
		this.compositeKey = compositeKey;
	}
	
	public VariantKeys(int... compositeKeys) {
    this.compositeKey = 0;
    for (int compositeKey : compositeKeys) {
      this.compositeKey |= compositeKey;
    }
	}

	@Override
	public String toString() {
		return Integer.toString(compositeKey);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariantKeys other = (VariantKeys) obj;
		return this.compositeKey == other.compositeKey;
	}
	
	public boolean containedIn(VariantKeys other) {
		if (this == other) {
			return true;
		}
		else {
			int tmp = this.compositeKey & other.compositeKey;
			if (tmp == this.compositeKey) {
				return true;
			}
			else {
				return false;
			}
		}
	}
}
