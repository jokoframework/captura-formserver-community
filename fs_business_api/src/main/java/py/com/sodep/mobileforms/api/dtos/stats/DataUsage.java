package py.com.sodep.mobileforms.api.dtos.stats;

public class DataUsage implements Comparable<DataUsage>{

	private long appId;

	private String appName;

	private boolean active;

	private long uploadedData;

	private long documentStorage;

	public long getUploadedData() {
		return uploadedData;
	}

	public void setUploadedData(long uploadedData) {
		this.uploadedData = uploadedData;
	}

	public long getDocumentStorage() {
		return documentStorage;
	}

	public void setDocumentStorage(long documentStorage) {
		this.documentStorage = documentStorage;
	}

	public long getAppId() {
		return appId;
	}

	public void setAppId(long appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public int compareTo(DataUsage dU) {	
		return new Long(this.uploadedData).compareTo(new Long(dU.uploadedData));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (uploadedData ^ (uploadedData >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataUsage other = (DataUsage) obj;
		if (uploadedData != other.uploadedData)
			return false;
		return true;
	}
}
