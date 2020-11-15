package py.com.sodep.mobileforms.impl.services.workers;

import org.springframework.core.task.TaskExecutor;

import py.com.sodep.mobileforms.api.services.workers.IBackgroundWorker;

public abstract class BackgroundWorker implements IBackgroundWorker {

	protected TaskExecutor executor;

	public BackgroundWorker(TaskExecutor executor) {
		this.executor = executor;
	}

	protected abstract void doWork();

	@Override
	public void run() {
		try {
			doWork();
		} catch (Exception e) {
			// There was an unexpected error on the bean

		}

	}

	public TaskExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(TaskExecutor executor) {
		this.executor = executor;
	}

	/**
	 * Schedule a working that will be handled ASAP
	 */
	public void scheduleWork() {
		executor.execute(this);

	}

}
