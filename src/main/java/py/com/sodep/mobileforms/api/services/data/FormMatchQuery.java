package py.com.sodep.mobileforms.api.services.data;

import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;

public class FormMatchQuery {

	private Long matchQueryFormId;
	private Long matchQueryFormVersion;
	private String matchQueryElementId;
	private ConditionalCriteria matchQueryRestrictions;
	private String elementId;
	
	private Long formId;
	private Long version;
	private ConditionalCriteria restrictions;
	private Integer page;
	private Integer rows;
	private OrderBy orderBy;
	
	public static class Builder {
		private Long formId;
		private Long version;
		private Long matchQueryFormId;
		private Long matchQueryFormVersion;
		private String matchQueryElementId;
		private ConditionalCriteria matchQueryRestrictions;
		private String elementId;
		private ConditionalCriteria restrictions;
		private Integer page;
		private Integer rows;
		private OrderBy orderBy;
		
		public Builder formId(Long formId) {
			this.formId = formId;
			return this;
		}

		public Builder version(Long version) {
			this.version = version;
			return this;
		}

		public Builder matchQueryFormId(Long matchQueryFormId) {
			this.matchQueryFormId = matchQueryFormId;
			return this;
		}

		public Builder matchQueryFormVersion(Long matchQueryFormVersion) {
			this.matchQueryFormVersion = matchQueryFormVersion;
			return this;
		}

		public Builder matchQueryElementId(String matchQueryElementId) {
			this.matchQueryElementId = matchQueryElementId;
			return this;
		}

		public Builder matchQueryRestrictions(
				ConditionalCriteria matchQueryRestrictions) {
			this.matchQueryRestrictions = matchQueryRestrictions;
			return this;
		}

		public Builder elementId(String elementId) {
			this.elementId = elementId;
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

		public FormMatchQuery build() {
			return new FormMatchQuery(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}
	private FormMatchQuery(Builder builder) {
		this.formId = builder.formId;
		this.version = builder.version;
		this.matchQueryFormId = builder.matchQueryFormId;
		this.matchQueryFormVersion = builder.matchQueryFormVersion;
		this.matchQueryElementId = builder.matchQueryElementId;
		this.matchQueryRestrictions = builder.matchQueryRestrictions;
		this.elementId = builder.elementId;
		this.restrictions = builder.restrictions;
		this.page = builder.page;
		this.rows = builder.rows;
		this.orderBy = builder.orderBy;
	}

	public Long getFormId() {
		return formId;
	}

	public Long getVersion() {
		return version;
	}

	public Long getMatchQueryFormId() {
		return matchQueryFormId;
	}

	public Long getMatchQueryFormVersion() {
		return matchQueryFormVersion;
	}

	public String getMatchQueryElementId() {
		return matchQueryElementId;
	}

	public ConditionalCriteria getMatchQueryRestrictions() {
		return matchQueryRestrictions;
	}

	public String getElementId() {
		return elementId;
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

}
