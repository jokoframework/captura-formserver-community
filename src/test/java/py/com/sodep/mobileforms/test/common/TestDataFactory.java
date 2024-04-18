package py.com.sodep.mobileforms.test.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria.CONDITION_TYPE;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.workflow.MFIncominDataWorkflow;

public class TestDataFactory {

	public static final Long STATE = 1L;
	
	public static final Long NEW_STATE = 2L;

	public static final String NAME_PROPERTY = "name";

	public static final String NAME_VALUE = "Clark Kent";
	
	public static final String LANG = "en";

	public static final String COMMENT_VALUE = "comentario de cambio de estado";

	public static final String TEST_ROLE_NAME = "testRole";
	
	public static final String TEST_GROUP_NAME = "TEST_GROUP";


	private static String testUserMail = "jgonzalez@sodep.mobileforms.com";
	
	private static String roleName = "A test Role";

	private static String roleDesc = "Something long that the user can write to explain what this role is for";


	public static User getTestUser() {
		User u = new User();
		u.setFirstName("Juan");
		u.setLanguage("en");
		u.setPassword("123456");
		u.setLastName("Gonzalez");
		u.setMail(testUserMail);
	
		return u;
	}
	
	public static WorkflowData forWorkflow() {
		WorkflowData data = new WorkflowData();
		data.setIncoming(getWorkflowIncoming());
		data.setMeta(getWorkflowMeta());
		data.setData(getWorkflowData());
		data.setDataList(getWorkflowDataList());
		data.setFilterByName(getWorkflowFilterByName());
		return data;
	}

	private static List<MFIncomingDataI> getWorkflowIncoming() {
		List<MFIncomingDataI> rows = new ArrayList<>(1);
		Map<String, Object> data = getWorkflowData();

		Map<String, Object> meta = getWorkflowMeta();

		MFIncominDataWorkflow mfIncominDataWorkflow = new MFIncominDataWorkflow(0, data, meta);
		rows.add(mfIncominDataWorkflow);
		return rows;
	}


	private static Map<String, Object> getWorkflowMeta() {
		Map<String, Object> meta = new HashMap<>();
		meta.put(MFIncominDataWorkflow.META_FIELD_STATE_ID, STATE);
		meta.put(MFIncominDataWorkflow.META_FIELD_COMMENT, COMMENT_VALUE);
		return meta;
	}


	private static Map<String, Object> getWorkflowData() {
		Map<String, Object> data = new HashMap<>();
		data.put(NAME_PROPERTY, NAME_VALUE);
		return data;
	}


	private static List<Map<String, Object>> getWorkflowDataList() {
		Map<String, Object> data = getWorkflowData();
		return Arrays.asList(data);
	}
	
	// No estoy completamente seguro si esta definición está correcta
	public static MFDataSetDefinition getDataSetDef() {
		MFDataSetDefinition def = new MFDataSetDefinition();

		MFField pkField = new MFField(FIELD_TYPE.NUMBER, "ID");
		def.addField(pkField);
		def.addField(new MFField(FIELD_TYPE.STRING, "name"));
		def.addField(new MFField(FIELD_TYPE.NUMBER, MFIncominDataWorkflow.META_FIELD_STATE_ID));

		return def;
	}
	
	public static final RoleDTO getRole() {

		RoleDTO dto = new RoleDTO();
		dto.setName(roleName);
		dto.setDescription(roleDesc);
		return dto;
	}
	private static ConditionalCriteria getWorkflowFilterByName() {
		ConditionalCriteria filter = new ConditionalCriteria(CONDITION_TYPE.AND);
		Criteria c = new Criteria(NAME_PROPERTY, OPERATOR.EQUALS, NAME_VALUE);
		filter.add(c);
		return filter;
	}
	
}
