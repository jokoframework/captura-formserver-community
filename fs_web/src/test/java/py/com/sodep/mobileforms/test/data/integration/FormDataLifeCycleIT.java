package py.com.sodep.mobileforms.test.data.integration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.RowCheckError;
import py.com.sodep.mf.form.model.MFBaseModel;
import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mf.form.model.element.MFElement;
import py.com.sodep.mf.form.model.prototype.MFInput;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.ProjectDTO;
import py.com.sodep.mobileforms.api.dtos.QueryDefinitionDTO;
import py.com.sodep.mobileforms.api.editor.Command;
import py.com.sodep.mobileforms.api.editor.Command.Type;
import py.com.sodep.mobileforms.api.editor.MFRef;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.elements.Input;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.services.data.IFormDataService;
import py.com.sodep.mobileforms.api.services.forms.model.ExecResponse;
import py.com.sodep.mobileforms.api.services.forms.model.ICommandService;
import py.com.sodep.mobileforms.api.services.forms.model.IFormModelService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormModificationService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.api.services.reports.IReportQueryService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class FormDataLifeCycleIT {
	private static Logger logger = LoggerFactory.getLogger(FormDataLifeCycleIT.class);
	@Autowired
	private MockObjectsContainer mockContainer;

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IFormModificationService formModificationService;

	@Autowired
	private ICommandService commandService;

	@Autowired
	private IFormDataService formDataService;

	@Autowired
	private IFormModelService formModelService;

	@Autowired
	private IFormService formService;

	@Autowired
	private IElementPrototypeService elementService;

	@Autowired
	private IReportQueryService queryService;

	private final String language = "en";
	private Map<py.com.sodep.mf.form.model.prototype.MFInput.Type, Long> elementTypeToPrototypeId;

	private Map<MFInput.Type, Long> elementTypeToPrototypeId() {
		List<Input> inputs = elementService.systemInputFields();
		HashMap<MFInput.Type, Long> map = new HashMap<MFInput.Type, Long>();
		for (Input i : inputs) {
			py.com.sodep.mf.form.model.prototype.MFInput.Type type = i.getType();
			i.getId();
			map.put(type, i.getId());
		}
		return map;

	}

	private FormDTO createTestForm(Application testApp, User appOwner) {

		// create a test project
		ProjectDTO pDTO = MockObjectsContainer.getProjectDTP(testApp);
		pDTO.setLabel("Test Project3");
		Project p = projectService.createNew(testApp, appOwner, pDTO);
		Assert.assertNotNull(p);

		// Step 2: ADMIN USER IS CREATING A FORM
		FormDTO formDTO = new FormDTO();
		formDTO.setLabel("Test Form3");
		formDTO.setDescription("A description");
		formDTO.setProjectId(p.getId());
		formDTO.setActive(true);
		AuthorizationAspect.setUserInRequest(appOwner);

		return formModificationService.create(p, formDTO, appOwner, language);

	}

	private Command createPage() {
		// 3.1 Command to add a Page
		Command cmd = new Command();
		// 3.1.1 The command type: ADD
		cmd.setType(Type.ADD);
		// 3.1.2 The object on which the command is executed
		MFRef ref = new MFRef();
		ref.setPosition(0);
		ref.setType(MFBaseModel.Type.PAGE);
		cmd.setRef(ref);
		// 3.1.3 The attributes are the parameters of the command
		List<Map<String, String>> atts = new ArrayList<Map<String, String>>();
		HashMap<String, String> att = new HashMap<String, String>();
		// model.js (line 340)
		// command.attributes.push({"name" : 'label', "language" : lang, "value"
		// : page.label});
		// command.attributes.push({"name" : 'position', "value" :
		// page.position});
		att.put("name", "position");
		att.put("value", "0");
		atts.add(att);

		att = new HashMap<String, String>();
		att.put("name", "label");
		att.put("value", "Page 1");
		atts.add(att);
		cmd.setAttributes(atts);
		return cmd;
	}

	private Command addElement(MFRef pageRef, String label, MFInput.Type type) {
		if (elementTypeToPrototypeId == null) {
			elementTypeToPrototypeId = elementTypeToPrototypeId();
		}
		Long prototypeId = elementTypeToPrototypeId.get(type);
		Assert.assertNotNull(prototypeId);
		Command cmd = new Command();
		// 3.2.1 The command type: ADD
		cmd.setType(Type.ADD);
		MFRef refElement = new MFRef();
		cmd.setRef(refElement);
		refElement.setContainer(pageRef); // when a command is executed on an
		// element, a reference to the
		// containing page must be present
		refElement.setPosition(0);
		refElement.setType(MFBaseModel.Type.ELEMENT);
		List<Map<String, String>> atts = new ArrayList<Map<String, String>>();
		HashMap<String, String> att;
		att = new HashMap<String, String>();
		att.put("name", "prototypeId");
		att.put("value", prototypeId.toString());// This is one of the
													// predefined templates.
		// (select p.id,i.type from
		// forms.element_prototypes p join
		// forms.elements_inputs i on p.id=i.id where
		// p.instantiability=2;)
		atts.add(att);

		att = new HashMap<String, String>();
		// command.attributes.push({"name" : 'position', value :
		// element.position});
		att.put("name", "position");
		att.put("value", "0");
		atts.add(att);
		// The attribute is "proto_label" and not label because when
		// adding/editing an element, the prefix "proto_" is used to distinguish
		// between instance properties and prototype properties
		att = new HashMap<String, String>();
		att.put("name", "proto_label");
		att.put("value", label);
		atts.add(att);
		cmd.setAttributes(atts);
		return cmd;
	}

	private HashMap<String, String> obtainLabelToElementIdMap(MFForm mfform) {
		List<MFElement> elements = mfform.listAllElements();
		HashMap<String, String> labelToId = new HashMap<String, String>();
		for (MFElement mfElement : elements) {
			String id = mfElement.getInstanceId();
			String label = mfElement.getProto().getLabel();
			labelToId.put(label, id);
		}
		return labelToId;
	}
	
	/**
	 * This method create a project, form, define and 
	 * publish the form. Add some data to form and test that 
	 * data are obtained in report.
	 * 
	 * @return {@link FormDTO}
	 * @throws InterruptedException
	 */
	//@Test
	public FormDTO testAddData() throws InterruptedException {

		Application testApp = mockContainer.getTestApplication();
		User appOwner = mockContainer.getTestApplicationOwner();

		// Step 1: ADMIN USER IS CREATING A PROJECT
		// simulates that the admin user is making the requests
		AuthorizationAspect.setUserInRequest(appOwner);

		FormDTO formDTO = createTestForm(testApp, appOwner);
		Assert.assertNotNull(formDTO.getId());

		// Step 3: DEFINE THE FORM
		// Add a page to the form
		List<Command> commands = new ArrayList<Command>();
		Command cmd = createPage();
		MFRef pageRef = cmd.getRef();
		commands.add(cmd);

		// 3.2 Command to add an Input of type Text

		cmd = addElement(pageRef, "Apellido", MFInput.Type.TEXT);
		commands.add(cmd);

		cmd = addElement(pageRef, "Cantidad", MFInput.Type.INTEGER);
		commands.add(cmd);

		cmd = addElement(pageRef, "TestFecha", MFInput.Type.DATE);
		commands.add(cmd);

		cmd = addElement(pageRef, "Medida", MFInput.Type.DECIMAL);
		commands.add(cmd);

		cmd = addElement(pageRef, "TuDescripcion", MFInput.Type.TEXTAREA);
		commands.add(cmd);

		cmd = addElement(pageRef, "Hora", MFInput.Type.TIME);
		commands.add(cmd);

		cmd = addElement(pageRef, "Email", MFInput.Type.EMAIL);
		commands.add(cmd);
		
		// TODO add a location

		// TODO add a select element

		ExecResponse response = commandService.executeSave(appOwner, formDTO.getId(), commands);
		Assert.assertNotNull(response);
		Assert.assertTrue(response.isSuccess());
		// Step 4: PUBLISH THE FORM
		formModificationService.publish(formDTO.getId());
		// Step 5: ADD SOME DATA
		formDTO = formService.getFormLastVersion(formDTO.getId(), language);
		MFForm mfform = formModelService.getMFForm(formDTO.getId(), formDTO.getVersion(), language);

		HashMap<String, Object> document = new HashMap<String, Object>();

		HashMap<String, String> labelToId = obtainLabelToElementIdMap(mfform);
		document.put(labelToId.get("Apellido"), "Gonzalez");
		document.put(labelToId.get("Cantidad"), new Integer(10));
		document.put(labelToId.get("TestFecha"), new Date());
		document.put(labelToId.get("Medida"), new Double(49292.72383));
		document.put(labelToId.get("TuDescripcion"),
				"This is just supposed to be a long descripcion of something that the end-user might write");
		document.put(labelToId.get("Hora"), new Date());
		document.put(labelToId.get("Email"), "test@email.com");

		MFOperationResult savedData = formDataService.saveData(appOwner, document, null, formDTO.getId(),
				formDTO.getVersion());
		List<RowCheckError> errorList = savedData.getErrors();
		if (errorList != null) {
			logger.error("There were some errors saving the form");
			for (RowCheckError error : errorList) {
				logger.error(error.toString());
			}
			Assert.fail("There were some errors saving the form");
		}
		Assert.assertEquals(1, savedData.getNumberOfAffectedRows());

		// Step 6: TEST THAT THE DATA CAN BE OBTAINED FROM THE REPORT

		PagedData<List<MFManagedData>> list = formDataService.getFormData(appOwner, mfform.getId(),
				mfform.getVersion(), 1, 10);
		// Since we have add a single document there should only be one document
		// here
		Assert.assertEquals(new Long(1l), new Long(list.getTotalCount()));
		Assert.assertEquals(new Long(1l), new Long(list.getTotalCount().longValue()));
		MFManagedData docStored = list.getData().get(0);

		List<MFField> fields = formService.listFields(mfform);
		for (MFField field : fields) {
			Object expectedValue = document.get(field.getColumnName());
			Object storedValue = docStored.getValue(field.getColumnName());
			Assert.assertNotNull(expectedValue);
			Assert.assertNotNull(storedValue);
			logger.debug(field.getColumnName() + " - " + storedValue);
			Assert.assertEquals(expectedValue, storedValue);

			// check that the expected data types are returned
			if (field.getType().equals(MFField.FIELD_TYPE.NUMBER)) {
				if (!(storedValue instanceof Number)) {
					Assert.fail("Expected Number but got " + field.getType().getClass() + " for "
							+ field.getColumnName());
				}

			} else if (field.getType().equals(MFField.FIELD_TYPE.DATE)) {
				if (!(storedValue instanceof Date)) {
					Assert.fail("Expected Date but got " + field.getType().getClass() + " for " + field.getColumnName());
				}
			} else if (field.getType().equals(MFField.FIELD_TYPE.BOOLEAN)) {
				if (!(storedValue instanceof Boolean)) {
					Assert.fail("Expected Boolean but got " + field.getType().getClass() + " for "
							+ field.getColumnName());
				}
			}

		}
		return formDTO;
	}

	/**
	 * Insert document test data and create query
	 * for that data
	 * @throws InterruptedException
	 */
	@Test
	public void testQuery() throws InterruptedException {
		FormDTO formDTO = testAddData();
		
		QueryDefinitionDTO def = new QueryDefinitionDTO();
		def.setName("A query");
		def.setFormId(formDTO.getId());
		def.setVersion(formDTO.getVersion());
		ArrayList<String> list = new ArrayList<String>();
		list.add("element77");
		def.setSelectedTableColumns(list);

		QueryDefinitionDTO queryDef = queryService.saveQuery(formDTO.getId(), def, language);
		Assert.assertNotNull(queryDef);
		Assert.assertNotNull(queryDef.getId());
		
	}

}
