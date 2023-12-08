package de.rwth.processmining.tb.core.util.backgroundwork;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;

public interface BackgroundTaskService {
	/**
     * Executes SwingWorker task.
     * @param worker Worker to be executed.
     */
    void execute(SwingWorker<?,?> worker);
    /**
     * Executes Runnable on background thread, it's up to
     * Runnable's code to call invokeLater() to update the UI. Do
     * not update or work with UI from the Runnable.
     * 
     * @param work Code to execute on background thread.
     */
    void execute(Runnable work);
    
	<T> Future<T> submit(Callable<T> task);
	
	public void shutdown();
}
