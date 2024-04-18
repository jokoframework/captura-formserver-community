package py.com.sodep.mobileforms.impl.services.data;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataLock {

	private Lock dataLock = new ReentrantLock();
	private Thread activeThread;

	private Object innerLock = new Object();

	private final long maxWaitForDataLock;

	public DataLock(long maxWaitForDataLock) {
		this.maxWaitForDataLock = maxWaitForDataLock;
	}

	public boolean lock() throws InterruptedException {
		boolean locked = dataLock.tryLock(maxWaitForDataLock, TimeUnit.MILLISECONDS);
		synchronized (innerLock) {
			activeThread = Thread.currentThread();
		}
		return locked;
	}

	public Thread holdingThread() {
		synchronized (innerLock) {
			return activeThread;
		}
	}

	public boolean isLocked() {
		synchronized (innerLock) {
			if (activeThread != null) {
				return true;
			} else {
				return false;
			}
		}
	}

	public void releaseLock() {
		synchronized (innerLock) {
			activeThread = null;
		}
		dataLock.unlock();

	}

}
