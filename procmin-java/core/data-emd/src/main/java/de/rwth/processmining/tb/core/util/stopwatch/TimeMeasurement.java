package de.rwth.processmining.tb.core.util.stopwatch;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class that represents a time measurement.
 * Should only be used to measure long-running code blocks.
 * Class is not tuned for performance. 
 * 
 * @author brockhoff
 *
 */
public class TimeMeasurement {

	final static Logger logger = LogManager.getLogger(TimeMeasurement.class);
	
	/**
	 * ID (despite generation is relative expensive)
	 */
	private final UUID id;
	
	/**
	 * Timestamp of the last start.
	 */
	private long lastStart;
	
	/**
	 * Aggregated time over all start and pauses.
	 */
	private long aggTime;
	

	/**
	 * Timestamp of the stop.
	 */
	private long lastStop;
	
	/**
	 * Current status.
	 */
	private StopwatchLifeCycle status;
	
	public TimeMeasurement() {
		this.id = UUID.randomUUID();
		this.lastStart = 0;
		this.aggTime = 0;
		this.lastStop = 0; 
		this.status = StopwatchLifeCycle.INIT;
	}
	
	public void start() {
		if (status == StopwatchLifeCycle.INIT) {
			this.lastStart = System.currentTimeMillis();
			this.status = StopwatchLifeCycle.RUNNING;
		}
		else {
			throw new IllegalStateException("Cannot start in status " + this.status);
		}
	}
	
	public void pause() {
		if (status == StopwatchLifeCycle.RUNNING) {
			this.lastStop = System.currentTimeMillis();
			this.aggTime += this.lastStop - this.lastStart;
			this.status = StopwatchLifeCycle.PAUSED;
		}
		else {
			throw new IllegalStateException("Cannot pause in status " + this.status);
		}
	}
	
	public void resume() {
		if (status == StopwatchLifeCycle.PAUSED) {
			this.lastStart = System.currentTimeMillis();
			this.status = StopwatchLifeCycle.RUNNING;
		}
		else {
			throw new IllegalStateException("Cannot resume in status " + this.status);
		}
	}
	
	public void stop() {
		if (status == StopwatchLifeCycle.PAUSED) {
			this.status = StopwatchLifeCycle.STOPPED;
		}
		else if (status == StopwatchLifeCycle.RUNNING) {
			this.lastStop = System.currentTimeMillis();
			this.aggTime += this.lastStop - this.lastStart;
			this.status = StopwatchLifeCycle.STOPPED;
		}
		else {
			throw new IllegalStateException("Cannot stop in status " + this.status);
		}
	}
	
	public UUID getID() {
		return this.id;
	}
	
	public long getTime() {
		if (status == StopwatchLifeCycle.STOPPED) {
			return aggTime;
		}
		else {
			throw new IllegalStateException("Can only return time after stopping not in " + this.status);
		}
	}

}
