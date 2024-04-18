package py.com.sodep.mobileforms.test.common;

import java.util.List;
import java.util.Map;

import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;

// Contenedor de datos de documentos
// para usar en test de integración (o unitarios)
// en workflow.
public class WorkflowData {

	private List<MFIncomingDataI> incoming;
	
	private Map<String, Object> data;
	
	private Map<String, Object> meta;
	
	private List<Map<String, Object>> dataList;
	
	private ConditionalCriteria filterByName;

	/**
	 * @return the filterByName
	 */
	public ConditionalCriteria getFilterByName() {
		return filterByName;
	}

	/**
	 * @param filterByName the filterByName to set
	 */
	public void setFilterByName(ConditionalCriteria filterByName) {
		this.filterByName = filterByName;
	}

	/**
	 * @return the incoming
	 */
	public List<MFIncomingDataI> getIncoming() {
		return incoming;
	}

	/**
	 * @return the data
	 */
	public Map<String, Object> getData() {
		return data;
	}

	/**
	 * @return the meta
	 */
	public Map<String, Object> getMeta() {
		return meta;
	}

	/**
	 * @return the dataList
	 */
	public List<Map<String, Object>> getDataList() {
		return dataList;
	}

	public void setIncoming(List<MFIncomingDataI> workflowIncoming) {
		this.incoming = workflowIncoming;
	}

	public void setData(Map<String, Object> workflowData) {
		this.data = workflowData;
	}

	public void setMeta(Map<String, Object> workflowMeta) {
		this.meta = workflowMeta;
	}

	public void setDataList(List<Map<String, Object>> workflowDataList) {
		this.dataList = workflowDataList;
	}

	

}
