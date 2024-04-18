package py.com.sodep.mobileforms.impl.services.data;

/**
 * This is a report about the status of a dataSet.
 * <ul>
 * <li>dataSet =1 indicates that the dataSet is active, 0 indicates that it
 * doesn't exists.</li>
 * <li>dataBag=1 means that the there was at least one insert performed.</li>
 * <li>ddls contains the number of versions of the dataSet. If dataSet=1 ddls
 * should be >0</li>
 * <ul>
 * 
 * 
 * @author danicricco
 * 
 */
public class DataDefinitionReport {

	private final int dataBag;
	private final int dataSet;
	private final int ddls;

	public DataDefinitionReport(int dataBag, int dataSet, int ddls) {
		super();
		this.dataBag = dataBag;
		this.dataSet = dataSet;
		this.ddls = ddls;
	}

	public int getDataBag() {
		return dataBag;
	}

	public int getDataSet() {
		return dataSet;
	}

	public int getDdls() {
		return ddls;
	}

}
