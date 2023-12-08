package de.rwth.processmining.tb.core.emd.dataview;

public class PerspectiveDescriptionLog extends PerspectiveDescriptor {
	
	private final LogType logType;
	
	public PerspectiveDescriptionLog(LogType logType) {
		super();
		this.logType = logType;
	}

	public enum LogType {
		CONTEXT,
		FOCUS
	}

	@Override
	public String getID() {
		switch(this.logType) {
			case CONTEXT:
				return "Context";
			case FOCUS:
				return "Focus";
			default:
				return "Log type undefined";
		}
	}

	@Override
	public int hashCode() {
		return logType.hashCode();
	}
}
