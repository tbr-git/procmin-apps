package de.rwth.processmining.tb.core.util.stopwatch;

import java.util.EnumMap;
import java.util.stream.Collectors;

public class PerformanceLogger<T extends Enum<T>> {
	
	private EnumMap<T, TimeMeasurement> measurements;
	
	public PerformanceLogger(Class<T> cl) {
		this.measurements = new EnumMap<>(cl);
	}
	
	public void startMeasurement(T milestone) {
		TimeMeasurement measurement = measurements.get(milestone);
		if (measurement == null) {
			measurement = new TimeMeasurement();
			measurements.put(milestone, measurement);
		}
		measurement.start();
	}
	
	public void pauseMeasurement(T milestone) {
		TimeMeasurement measurement = getMeasurement(milestone);
		measurement.pause();
	}
	
	public void resumeMeasurement(T milestone) {
		TimeMeasurement measurement = getMeasurement(milestone);
		measurement.resume();
	}

	public void stopMeasurement(T milestone) {
		TimeMeasurement measurement = getMeasurement(milestone);
		measurement.stop();
	}
	
	public String toString() {
		return measurements.entrySet().stream()
			.map(e -> (e.getKey() + ": " + e.getValue().getTime()))
			.sorted()
			.collect(Collectors.joining(", ", "{", "}"));
	}

	private TimeMeasurement getMeasurement(T milestone) {
		TimeMeasurement measurement = measurements.get(milestone);
		if (measurement == null) {
			throw new IllegalStateException("No measurement " + milestone);
		}
		else {
			return measurement;
		}
		
	}
	
	public EnumMap<T, TimeMeasurement> getMeasurements() {
		return this.measurements;
	}

}
