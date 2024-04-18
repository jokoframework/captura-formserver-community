package py.com.sodep.mobileforms.api.documents.upload;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockManager {

	// Should we have a pool of this objects?
	private class LockObject {
		private int count = 1;
		private Lock lock = new ReentrantLock(true);
	}

	private Map<String, LockObject> locks = new HashMap<String, LockObject>();

	public void lock(String handle, LockCallback lockCallback) {
		LockObject lockObject = getAndLock(handle);
		try {
			lockCallback.doInLock();
		} finally {
			lockObject.lock.unlock();
			removeIfUnused(handle, lockObject);
		}
	}

	private synchronized void removeIfUnused(String handle, LockObject lockObject) {
		lockObject.count--;
		if (lockObject.count == 0) {
			locks.remove(handle);
		}
	}

	private synchronized LockObject getAndLock(String handle) {
		LockObject lockObject = locks.get(handle);
		if (lockObject == null) {
			lockObject = new LockObject();
			locks.put(handle, lockObject);
		}
		lockObject.count++;
		lockObject.lock.lock();
		return lockObject;
	}

}
