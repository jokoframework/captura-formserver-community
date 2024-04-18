package py.com.sodep.mobileforms.impl;

import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria.CONDITION_TYPE;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.MFRestriction;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;

public class ConditionalCriteriaBuilder {

	private CONDITION_TYPE condition = CONDITION_TYPE.AND;
	
	private MFRestriction restriction;

	public static ConditionalCriteriaBuilder get() {
		return new ConditionalCriteriaBuilder();
	}
	
	public ConditionalCriteriaBuilder equals(String propertyName, Object propertyValue) {
		restriction = new Criteria(propertyName, OPERATOR.EQUALS, propertyValue);
		return this;
	}
	
	public ConditionalCriteriaBuilder in(String propertyName, Object propertyValue) {
		restriction = new Criteria(propertyName, OPERATOR.IN, propertyValue);
		return this;
	}
	
	public ConditionalCriteria build() {
		ConditionalCriteria c = new ConditionalCriteria(condition);
		c.add(restriction);
		return c;
	}
}
