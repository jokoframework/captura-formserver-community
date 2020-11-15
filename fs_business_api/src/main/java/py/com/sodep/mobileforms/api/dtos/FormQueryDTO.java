package py.com.sodep.mobileforms.api.dtos;

import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mobileforms.api.services.data.OrderBy;

public class FormQueryDTO {

	private Long formId;
	private Long version;
	private Long stateId;
	private ConditionalCriteria restrictions;
	private Integer page;
	private Integer rows;
	private OrderBy orderBy;
	public static class Builder {
		private Long formId;
		private Long version;
		
		private ConditionalCriteria restrictions;
		private Integer page;
		private Integer rows;
		private OrderBy orderBy;
		private Long stateId;
		
		public Builder formId(Long formId) {
			this.formId = formId;
			return this;
		}

		public Builder version(Long version) {
			this.version = version;
			return this;
		}


		public Builder restrictions(ConditionalCriteria restrictions) {
			this.restrictions = restrictions;
			return this;
		}
		
		public Builder page(Integer page) {
			this.page = page;
			return this;
		}
		
		public Builder rows(Integer rows) {
			this.rows = rows;
			return this;
		}
		
		public Builder orderBy(OrderBy orderBy) {
			this.orderBy = orderBy;
			return this;
		}

		public Builder stateId(Long stateId) {
			this.stateId = stateId;
			return this;
		}
		public FormQueryDTO build() {
			return new FormQueryDTO(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}
	
	private FormQueryDTO(Builder builder) {
		this.formId = builder.formId;
		this.version = builder.version;
		this.restrictions = builder.restrictions;
		this.page = builder.page;
		this.rows = builder.rows;
		this.orderBy = builder.orderBy;
		this.stateId = builder.stateId;
	}

	public Long getFormId() {
		return formId;
	}

	public Long getVersion() {
		return version;
	}
	
	public ConditionalCriteria getRestrictions() {
		return restrictions;
	}
	public Integer getPage() {
		return page;
	}
	public Integer getRows() {
		return rows;
	}
	public OrderBy getOrderBy() {
		return orderBy;
	}
	
	public Long getStateId() {
		return this.stateId;
	}

	public FormDTO getFormDto() {
		FormDTO f = new FormDTO();
		f.setId(formId);
		f.setVersion(version);
		return f;
	}

}
