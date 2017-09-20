package edu.udel.cis.vsl.gmc.concurrent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The threadPool That is used to manage the threads used in the algorithm.
 * 
 * @author yanyihao
 *
 */
public class MyThreadPool {
	/**
	 * The upper bound of active threads that are executing.
	 */
	private int parallelism = Runtime.getRuntime().availableProcessors();

	/**
	 * The number of threads that are waiting.
	 */
	private AtomicInteger waitingNum = new AtomicInteger(0);

	/**
	 * The max number of threads which includes all threads that are executing
	 * and waiting.
	 */
	private int maxNumOfThread = 2 * Runtime.getRuntime().availableProcessors();

	private AtomicInteger totalThreadsExist = new AtomicInteger(0);

	private ForkJoinPool pool;

	public MyThreadPool(int parallelism) {
		this.parallelism = parallelism;
		// maxNumOfThread = 2 * parallelism;
		pool = new ForkJoinPool(maxNumOfThread);
	}

	public MyThreadPool() {
		pool = new ForkJoinPool(parallelism);
	}

	/**
	 * Increase the counter for waiting threads.
	 */
	public void incrementWaiting() {
		waitingNum.incrementAndGet();
	}

	/**
	 * Decrease the counter for waiting threads.
	 */
	public void decrementWaiting() {
		waitingNum.decrementAndGet();
	}

	public void incrementTotal() {
		totalThreadsExist.incrementAndGet();
	}

	public void decrementTotal() {
		totalThreadsExist.decrementAndGet();
	}

	/**
	 * @return the number of threads that are currently executing (not include
	 *         waiting threads).
	 */
	public int getActiveNum() {
		// return pool.getActiveThreadCount();
		return totalThreadsExist.get() - waitingNum.get();
	}

	/**
	 * @return the total number of threads (include threads that are waiting).
	 */
	public int getRunningNum() {
		return totalThreadsExist.get();
	}

	public void submit(ForkJoinTask<Integer> task) {
		totalThreadsExist.incrementAndGet();
		pool.submit(task);
	}

	public boolean isQuiescent() {
		return pool.isQuiescent();
	}

	public int getMaxNumOfThread() {
		return maxNumOfThread;
	}

	public void shutdown() {
		pool.shutdown();
	}
}
