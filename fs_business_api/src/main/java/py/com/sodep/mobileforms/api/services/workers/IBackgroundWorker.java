package py.com.sodep.mobileforms.api.services.workers;

public interface IBackgroundWorker extends Runnable {

	public void scheduleWork();

}
