package py.com.sodep.mobileforms.api.dtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import py.com.sodep.mobileforms.api.services.workflow.MFIncominDataWorkflow;

public class DocumentDTO {
	
	private final List<Map<String,Object>> dataList;
	private final Map<String, Object> meta;
	private final FormDTO form;
	private final Long docId;
	/**
	 * Field for batch document's operation
	 * if it's set, field 'docId' isn't
	 */
	private final List<Long> docIds;
	private final StateDTO state;
	private final Map<String, Object> data;
	
	
	/**
	 * @return the form
	 */
	public FormDTO getForm() {
		return form;
	}
	
	
	/**
	 * @return the stateId
	 */
	public StateDTO getState() {
		return state;
	}
	/**
	 * @return the dataList
	 */
	public List<Map<String, Object>> getDataList() {
		return dataList;
	}
	/**
	 * @return the meta
	 */
	public Map<String, Object> getMeta() {
		return meta;
	}
	
	public Long getDocId() {
		return this.docId;
	}

	public Map<String, Object> getData() {
		return this.data;
	}

	
	public static class Builder {
		public List<Map<String, Object>> dataList = new ArrayList<>();;
		public Map<String, Object> meta = new HashMap<String, Object>();;
		private FormDTO form;
		private Long docId;
		private StateDTO state;
		private Map<String, Object> data;
		private List<Long> docIds;
		
		
		public DocumentDTO build() {
			return new DocumentDTO(this);
		}


		public Builder metaData(Map<String, Object> meta) {
			this.meta = meta;
			return this;
		}


		public Builder dataList(List<Map<String, Object>> dataList) {
			this.dataList = dataList;
			return this;
		}


		public Builder form(FormDTO form) {
			this.form = form;
			return this;
		}
		
		public Builder docId(Long docId) {
			this.docId = docId;
			return this;
		}
		

		public Builder state(StateDTO state) {
			this.state = state;
			return this;
		}


		public Builder data(Map<String, Object> data) {
			this.data = data;
			return this;
		}


		public Builder stateAsMeta(StateDTO state) {
			if (state != null) {
				this.meta.put(MFIncominDataWorkflow.META_FIELD_STATE_ID, state.getId());
				this.meta.put(MFIncominDataWorkflow.META_FIELD_COMMENT, state.getComment());
			}
			return this;
		}
		
		public Builder docIds(List<Long> docIds) {
			this.docIds = docIds;
			return this;
		}
	}
	
	private DocumentDTO(Builder builder) {
		this.dataList = builder.dataList;
		this.meta = builder.meta;
		this.form = builder.form;
		this.docId = builder.docId;
		this.state = builder.state;
		this.data = builder.data;
		this.docIds = builder.docIds;
	}
	
	public static Builder builder() {
		return new Builder();
	}


	public boolean hasWorkflow() {
		return this.state != null;
	}


	public List<Long> getDocIds() {
		return docIds;
	}



	

}
