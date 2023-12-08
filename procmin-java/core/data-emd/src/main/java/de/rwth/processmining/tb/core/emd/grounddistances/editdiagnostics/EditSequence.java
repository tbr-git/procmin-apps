package de.rwth.processmining.tb.core.emd.grounddistances.editdiagnostics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class EditSequence implements Iterable<LVSEditOperation>{
	
	public abstract static class BuilderAbstract<T extends BuilderAbstract<T>> {
		
		private List<LVSEditOperation> lEditOperations;
		
		private double cost = -1;

		public BuilderAbstract() {
			lEditOperations = new LinkedList<>();
		}
		
		public abstract T getThis();
		
		public T addEditOperationReverse(LVSEditOperation editOperation) {
			lEditOperations.add(0, editOperation);
			return getThis();
		}
		
		public T setCost(double cost) {
			this.cost = cost;
			return getThis();
		}
		
		public double getCost() {
			return cost;
		}
			
		public EditSequence build() {
			return new EditSequence(this);
//			LVSEditOperation[] editOperations = new LVSEditOperation[lEditOperations.size()];
//			lEditOperations.toArray(editOperations);
//			return new EditSequence(editOperations, cost);
		}
	}
	
	public static class Builder extends BuilderAbstract<Builder> {

		double test = -1; 

		@Override
		public Builder getThis() {
			return this;
		}
		
	}
	
	/**
	 * 
	 */
	private final ArrayList<LVSEditOperation> editOperations;
	
	private final double editCost;
	
	protected EditSequence(BuilderAbstract<?> builder) {
		this.editCost = builder.cost;
		this.editOperations = new ArrayList<LVSEditOperation>(builder.lEditOperations);
	}
	
//	private EditSequence(LVSEditOperation[] editOperations, double editCost) {
//		this.editOperations = editOperations;
//		this.editCost = editCost;
//	}	
	
	public LVSEditOperation getEditOp(int index) {
		return editOperations.get(index);
	}
	
	public double getEditCost() {
		return editCost;
	}
	
//	public JSONArray getJSON() {
//		JSONArray ja = new JSONArray();
//		for(LVSEditOperation op : editOperations) {
//			ja.put(op.getJSON());
//		}
//		return ja;
//	}

	@Override
	public Iterator<LVSEditOperation> iterator() {
		return editOperations.iterator();
	}


}
