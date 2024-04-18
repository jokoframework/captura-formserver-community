package py.com.sodep.mobileforms.test.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.form.model.MFBaseModel;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.PoolDTO;
import py.com.sodep.mobileforms.api.dtos.ProcessItemDTO;
import py.com.sodep.mobileforms.api.dtos.ProjectDTO;
import py.com.sodep.mobileforms.api.editor.Command;
import py.com.sodep.mobileforms.api.editor.Command.Type;
import py.com.sodep.mobileforms.api.editor.MFRef;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementInstance;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.forms.page.Page;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.exceptions.ElementPrototypeInUseException;
import py.com.sodep.mobileforms.api.services.forms.model.ExecResponse;
import py.com.sodep.mobileforms.api.services.forms.model.ICommandService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormModificationService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
import py.com.sodep.mobileforms.api.services.metadata.pools.IPoolService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;
import py.com.sodep.mobileforms.web.entitybuilders.ProcessItemBuilder;

/**
 * This class test logical delete over projects and
 * forms 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class LogicalDeleteIT {
	
	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IPoolService poolService;

	@Autowired
	private IElementPrototypeService elementPrototypeService;

	@Autowired
	private ICommandService commandService;

	@Autowired
	private IFormModificationService formModificationService;

	@Autowired
	private MockObjectsContainer mockObjectsContainer;

	@Autowired
	private IFormService formService;

	private Project addProject(String language, String label) {
		Application app = mockObjectsContainer.getTestApplication();
		User user = mockObjectsContainer.getTestApplicationOwner();

		ProjectDTO dto = new ProjectDTO();
		dto.setDescription("App to test deletion logic");
		dto.setLanguage(language);
		dto.setLabel(label);
		// This data should be taken from the arguments, shouldn't it?
		// dto.setApplicationId(app.getId());
		// dto.setOwnerId(user.getId());

		AuthorizationAspect.setUserInRequest(user);
		Project project = projectService.createNew(app, user, dto);

		Assert.assertNotNull(project);
		Assert.assertFalse(project.getDeleted());
		return project;
	}

	private Pool addPool(String name) {
		Application app = mockObjectsContainer.getTestApplication();
		User user = mockObjectsContainer.getTestApplicationOwner();

		PoolDTO dto = new PoolDTO();
		dto.setName(name);
		dto.setDescription("Pool to test deletion logic");
		// dto.setApplicationId(app.getId());

		Pool pool = poolService.createNew(app, user, dto);

		Assert.assertNotNull(pool);
		Assert.assertFalse(pool.getDeleted());
		return pool;
	}
	
	/**
	 * Create projects and forms, delete some that and
	 * verify correct cascade delete
	 */
	@Test
	public void deleteTest() {
		String language = "en";
		// Create two project
		Project project0 = addProject(language, "Project #0");
		Project project1 = addProject(language, "Project #1");
		
		// Create two pools
		Pool pool0 = addPool("pool #0");
		Pool pool1 = addPool("pool #1");

		// Add a process item to the first pool
		ElementPrototype prototype = addProcessItemInPool(language, pool0);
		// Add process items to the second pool
		addProcessItemInPool(language, pool1, "xxx 0");
		addProcessItemInPool(language, pool1, "xxx 1");

		// Add a form that uses that process item to the first project
		FormDTO formDTO = addForm(project0, language, "Test Form 0");
		addProccesItem(formDTO, prototype, 0);
		formModificationService.publish(formDTO.getId());

		// Add forms to the second project
		addForm(project1, language, "Test Form 1");
		addForm(project1, language, "Test Form 2");

		// Test the cascade delete
		// Delete the second project	
		project1 = projectService.logicalDelete(project1);
		Assert.assertTrue(project1.getDeleted());
		for (Form f : project1.getForms()) {
			Assert.assertTrue(f.getDeleted());
		}

		// Delete the second pool
		pool1 = poolService.logicalDelete(pool1);
		Assert.assertTrue(pool1.getDeleted());
		for (ElementPrototype p : pool1.getPrototypes()) {
			Assert.assertTrue(p.getDeleted());
		}

		// Test the constraint on process item deletion
		try {
			prototype = elementPrototypeService.logicalDelete(prototype);
		} catch (ElementPrototypeInUseException e) {
			List<Form> forms = e.getForms();
			Form formUsingProcessItem = forms.get(0);
			Assert.assertEquals(formUsingProcessItem.getId(), formDTO.getId());
		}

		Assert.assertFalse(prototype.getDeleted());

		try {
			pool0 = poolService.logicalDelete(pool0);
		} catch (ElementPrototypeInUseException e) {
			Map<Form, List<ElementPrototype>> mapFormPrototypes = e.getMapFormPrototypes();
			Set<Form> keySet = mapFormPrototypes.keySet();
			Form formUsingProcessItem = keySet.iterator().next();
			Assert.assertEquals(formUsingProcessItem.getId(), formDTO.getId());

			List<ElementPrototype> list = mapFormPrototypes.get(formUsingProcessItem);
			ElementPrototype elementPrototypeBeingUsed = list.get(0);
			Assert.assertEquals(prototype.getId(), elementPrototypeBeingUsed.getId());
		}

		// Delete the first form
		Form deletedForm = formService.logicalDelete(formDTO);
		Assert.assertTrue(deletedForm.getDeleted());
		for (Page page : deletedForm.getPages()) {
			Assert.assertTrue(page.getDeleted());
			for (ElementInstance element : page.getElements()) {
				Assert.assertTrue(element.getDeleted());
			}
		}

		// Now we should be able to delete the process item
		prototype = elementPrototypeService.logicalDelete(prototype);
		Assert.assertTrue(prototype.getDeleted());

		pool0 = poolService.logicalDelete(pool0);
		Assert.assertTrue(pool0.getDeleted());
		
		project0 = projectService.logicalDelete(project0);
		Assert.assertTrue(project0.getDeleted());
	}

	private ElementPrototype addProcessItemInPool(String language, Pool pool1) {
		return addProcessItemInPool(language, pool1, "Test Text");
	}

	private ElementPrototype addProcessItemInPool(String language, Pool pool1, String label) {
		ProcessItemBuilder builder = new ProcessItemBuilder(language);
		ProcessItemDTO dto = new ProcessItemDTO();

		dto.setLabel(label);
		dto.setPool(pool1.getId());
		dto.setRequired(false);
		dto.setType("text");

		ElementPrototype prototype = builder.newElementPrototype(dto);
		prototype = elementPrototypeService.create(prototype, pool1, language, label);

		Assert.assertNotNull(prototype);
		Assert.assertFalse(prototype.getDeleted());
		return prototype;
	}

	private void addProccesItem(FormDTO formDTO, ElementPrototype prototype, int position) {
		User user = mockObjectsContainer.getTestApplicationOwner();
		// TODO Auto-generated method stub
		List<Command> commands = new ArrayList<Command>();
		Command createPageCmd = createPageCommand();
		commands.add(createPageCmd);

		MFRef pageRef = createPageCmd.getRef();
		Command addElementCmd = addElementCmd(pageRef, prototype, position);
		commands.add(addElementCmd);

		ExecResponse response = commandService.executeSave(user, formDTO.getId(), commands);
		Assert.assertNotNull(response);
		Assert.assertTrue(response.isSuccess());
	}

	private FormDTO addForm(Project project1, String language, String label) {
		User user = mockObjectsContainer.getTestApplicationOwner();

		FormDTO formDTO = new FormDTO();
		formDTO.setLabel(label);
		formDTO.setDescription("A description");
		formDTO.setProjectId(project1.getId());
		formDTO.setActive(true);

		formDTO = formModificationService.create(project1, formDTO, user, language);
		Assert.assertNotNull(formDTO.getId());
		return formDTO;
	}

	private Command addElementCmd(MFRef pageRef, ElementPrototype prototype, int position) {
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
		att.put("value", prototype.getId().toString());
		// (select p.id,i.type from
		// forms.element_prototypes p join
		// forms.elements_inputs i on p.id=i.id where
		// p.instantiability=2;)
		atts.add(att);

		att = new HashMap<String, String>();
		// command.attributes.push({"name" : 'position', value :
		// element.position});
		att.put("name", "position");
		att.put("value", Integer.toString(position));
		atts.add(att);

		cmd.setAttributes(atts);
		return cmd;
	}

	// FIXME copied from FormDataIT
	private Command createPageCommand() {
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

}
