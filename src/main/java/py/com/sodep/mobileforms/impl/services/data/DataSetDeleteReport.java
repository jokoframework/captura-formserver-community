package py.com.sodep.mobileforms.impl.services.data;

/**
 * This is a class that represents the result of deleting a dataSet. Ideally it
 * should have the following values: dataSet =1 ddls >0 dataBag=1 .
 * 
 * @author danicricco
 * 
 */
public class DataSetDeleteReport {

	private final int dataSet;
	private final int ddls;
	private final int dataBag;

	public DataSetDeleteReport(int dataSet, int ddls, int dataBag) {

		this.dataSet = dataSet;
		this.ddls = ddls;
		this.dataBag = dataBag;
	}

	public int getDataSet() {
		return dataSet;
	}

	public int getDdls() {
		return ddls;
	}

	public int getDataBag() {
		return dataBag;
	}

}
