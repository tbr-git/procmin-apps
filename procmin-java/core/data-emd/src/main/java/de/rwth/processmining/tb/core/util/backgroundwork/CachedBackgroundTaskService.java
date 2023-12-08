package de.rwth.processmining.tb.core.util.backgroundwork;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CachedBackgroundTaskService implements BackgroundTaskService {
	private static final Logger logger = LogManager.getLogger(CachedBackgroundTaskService.class);
	
	private static class SingletonHelper {
		private static final CachedBackgroundTaskService INSTANCE = new CachedBackgroundTaskService();
	}
	
	public static CachedBackgroundTaskService getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private ExecutorService executorService;
//    private ExecutorService executorService = Executors.newCachedThreadPool();
//    private ExecutorService executorService = new ThreadPoolExecutor(0, 
//    		Runtime.getRuntime().availableProcessors() - 1, 60L, TimeUnit.SECONDS, 
//  	      new LinkedBlockingQueue<Runnable>());
    
    private CachedBackgroundTaskService() {
    	logger.info("Creating CachedBackgroundTaskSerive with at most {} threads", 
    			Runtime.getRuntime().availableProcessors() - 1);
    	executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    }

    @Override
    public void execute(SwingWorker<?, ?> worker) {
        executorService.execute(worker);
    }

    @Override
    public void execute(Runnable work) {
        executorService.execute(work);
    }

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return executorService.submit(task);
	}

	@Override
	public void shutdown() {
		this.executorService.shutdown();
	}
	
}
