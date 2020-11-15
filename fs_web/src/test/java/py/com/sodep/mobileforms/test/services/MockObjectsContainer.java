package py.com.sodep.mobileforms.test.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;
import py.com.sodep.mf.form.model.MFBaseModel;
import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mf.form.model.element.MFElement;
import py.com.sodep.mf.form.model.prototype.MFInput;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.ProjectDTO;
import py.com.sodep.mobileforms.api.dtos.ReportFilterOptionDTO;
import py.com.sodep.mobileforms.api.editor.Command;
import py.com.sodep.mobileforms.api.editor.Command.Type;
import py.com.sodep.mobileforms.api.editor.MFRef;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.Input;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.entities.projects.ProjectDetails;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.forms.model.ICommandService;
import py.com.sodep.mobileforms.api.services.forms.model.IFormModelService;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormModificationService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.common.TestDataFactory;
import py.com.sodep.mobileforms.test.metadata.UserServiceIT;
import py.com.sodep.mobileforms.web.utils.ReportHelper;

@Transactional
public class MockObjectsContainer {

	public static final String ADMIN_MAIL = "admin@sodep.mobileforms.com.py";
	public static final String ROOT_MAIL = "root@mobileforms.sodep.com.py";

	@Autowired
	private IUserService userService;

	@Autowired
	private IAuthorizationControlService authControlService;

	@Autowired
	private IApplicationService appService;

	@Autowired
	private IFormModelService formModelService;

	@Autowired
	private IFormService formService;
	
	@Autowired
	private IDataAccessService dataAccessService;
	
	@Autowired
	private IProjectService projectService;

	@Autowired
	private IFormModificationService formModificationService;

	
	private Map<py.com.sodep.mf.form.model.prototype.MFInput.Type, Long> elementTypeToPrototypeId;
	@Autowired
	private IElementPrototypeService elementService;

	@Autowired
	private ICommandService commandService;
	
	
	
	public static ProjectDTO getProjectDTP(Application app) {
		ProjectDTO dto = new ProjectDTO();
		dto.setApplicationId(app.getId());
		dto.setDescription("A project desc");
		dto.setLabel("A label");
		dto.setLanguage("en");

		return dto;
	}

	public static Project getTestProject(Application app, User owner) {
		Project p = new Project();
		p.setApplication(app);
		p.setDefaultLanguage("en");
		p.setOwner(owner);
		ProjectDetails d = new ProjectDetails();
		d.setLabel("A project label");
		d.setDescription("A project desc");
		d.setLanguage("en");
		ArrayList<ProjectDetails> details = new ArrayList<ProjectDetails>();
		details.add(d);
		p.setDetails(details);
		return p;
	}

	public static Form getTestForm(Project p, User owner) {
		Form f = new Form();
		f.setLabel("en", "A test form");
		f.setProject(p);
		f.setDefaultLanguage("en");

		return f;
	}

	public User getRootUser() {
		User rootUser = userService.findByMail(ROOT_MAIL);
		authControlService.computeUserAccess(rootUser);
		return rootUser;
	}

	public Application getTestApplication() {
		List<Application> app = appService.findAll();
		Application defaultApp = app.get(0);
		return defaultApp;
	}

	public User getTestApplicationOwner() {
		User owner = getTestApplication().getOwner();
		authControlService.computeUserAccess(owner);
		return owner;
	}

	public User getDummyUser(Application app) {

		User aNewUser = UserServiceIT.getTestUser();
		User savedUser = userService.findByMail(aNewUser.getMail());
		if (savedUser == null) {
			// simulates that the owner of the application is making the request
			User owner = app.getOwner();
			authControlService.computeUserAccess(owner);
			AuthorizationAspect.setUserInRequest(owner);
			savedUser = userService.addNewUser(getRootUser(), app, aNewUser);
		}
		authControlService.computeUserAccess(savedUser);
		return savedUser;
	}
	
	

	public ConditionalCriteria getFilterBy(FormDTO form, String property, String value) {
		FormDTO formDTO = formService.getFormLastVersion(form.getId(), TestDataFactory.LANG);
		MFForm mfform = formModelService.getMFForm(formDTO.getId(), formDTO.getVersion(), TestDataFactory.LANG);
		Map<String, String> map = getElementNameToIdMap(mfform);
		ReportFilterOptionDTO option = new ReportFilterOptionDTO(map.get(property), OPERATOR.EQUALS.name(), value);
		return ReportHelper.toConditionalCriteria(mfform.elementsMappedByName(), Arrays.asList(option));
	}
	
	public ConditionalCriteria getMetaFilterBy(FormDTO form, String property, String value) {
		FormDTO formDTO = formService.getFormLastVersion(form.getId(), TestDataFactory.LANG);
		MFForm mfform = formModelService.getMFForm(formDTO.getId(), formDTO.getVersion(), TestDataFactory.LANG);
		String metaProperty = "meta_" + property;
		ReportFilterOptionDTO option = new ReportFilterOptionDTO(metaProperty, OPERATOR.EQUALS.name(), value, FIELD_TYPE.STRING);
		return ReportHelper.toConditionalCriteria(mfform.elementsMappedByName(), Arrays.asList(option));
	}
	

	/**
	 * Given a <code>propToValues</code> map with: <'name', 'rodrigo'> pairs, it returns a list
	 * with the same pairs but with their keys as 'elementXXX', which is the format the system stores
	 * documents in Mongo:
	 * <pre>
	 * [
	 *  "element123": "rodrigo"
	 * ]
	 * </pre>
	 * 
	 * @param form
	 * @param propToValues
	 * @return
	 */
	public List<Map<String, Object>> asSearchableDataList(FormDTO form, Map<String, Object> propToValues) {
		FormDTO formDTO = formService.getFormLastVersion(form.getId(), TestDataFactory.LANG);
		MFForm mfform = formModelService.getMFForm(formDTO.getId(), formDTO.getVersion(), TestDataFactory.LANG);
		HashMap<String, String> map = getElementNameToIdMap(mfform);
		Map<String, Object> document = new HashMap<String, Object>();
		
		Set<String> keySet = propToValues.keySet();
		for (String prop : keySet) {
			Object value = propToValues.get(prop);
			document.put(map.get(prop), value);
		}
		
		return Arrays.asList(document);
	}
	
	public void defineTestDataSet(FormDTO formDto) {
		MFDataSetDefinition ddl = TestDataFactory.getDataSetDef();
		MFDataSetDefinitionMongo define = dataAccessService.define(ddl);
		
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		form.setDataSetDefinition(define.getMetaDataRef());
		form.setDatasetVersion(define.getVersion());
		formService.defineDataSet(form);
	}
	
	public void defineForm(FormDTO formDTO, User appOwner) {
		// TODO Auto-generated method stub
		// Step 3: DEFINE THE FORM
		// Add a page to the form
		List<Command> commands = new ArrayList<Command>();
		Command cmd = createPage();
		MFRef pageRef = cmd.getRef();
		commands.add(cmd);

		// 3.2 Command to add an Input of type Text

		cmd = addElement(pageRef, "name", MFInput.Type.TEXT);
		commands.add(cmd);
		
		commandService.executeSave(appOwner, formDTO.getId(), commands);
		// Step 4: PUBLISH THE FORM
		formModificationService.publish(formDTO.getId());
		
		
	}
	
	// Esto se usa en dos partes. Ver cómo generalizar
	public FormDTO createTestForm(Application testApp, User appOwner) {
		AuthorizationAspect.setUserInRequest(appOwner);

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

		// before we save we have to do this
		AuthorizationAspect.setUserInRequest(appOwner);

		return formModificationService.create(p, formDTO, appOwner, TestDataFactory.LANG);

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

	

	private HashMap<String, String> getElementNameToIdMap(MFForm mfform) {
		List<MFElement> elements = mfform.listAllElements();
		HashMap<String, String> labelToId = new HashMap<String, String>();
		for (MFElement mfElement : elements) {
			String id = mfElement.getInstanceId();
			String label = mfElement.getProto().getLabel();
			labelToId.put(label, id);
		}
		return labelToId;
	}
	



	

}
