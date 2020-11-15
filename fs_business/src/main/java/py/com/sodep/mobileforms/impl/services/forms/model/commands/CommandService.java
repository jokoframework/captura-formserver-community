package py.com.sodep.mobileforms.impl.services.forms.model.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.form.model.MFBaseModel;
import py.com.sodep.mf.form.model.element.filter.MFFilter;
import py.com.sodep.mf.form.model.flow.MFConditionalTarget;
import py.com.sodep.mf.form.model.flow.MFConditionalTarget.Operator;
import py.com.sodep.mf.form.model.prototype.MFSelect.OptionSource;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.editor.Command;
import py.com.sodep.mobileforms.api.editor.MFRef;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.dynamicvalues.Filter;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.Barcode;
import py.com.sodep.mobileforms.api.entities.forms.elements.Checkbox;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementInstance;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype.InstantiabilityType;
import py.com.sodep.mobileforms.api.entities.forms.elements.Input;
import py.com.sodep.mobileforms.api.entities.forms.elements.Location;
import py.com.sodep.mobileforms.api.entities.forms.elements.Photo;
import py.com.sodep.mobileforms.api.entities.forms.elements.Select;
import py.com.sodep.mobileforms.api.entities.forms.elements.Signature;
import py.com.sodep.mobileforms.api.entities.forms.page.ConditionalTarget;
import py.com.sodep.mobileforms.api.entities.forms.page.Flow;
import py.com.sodep.mobileforms.api.entities.forms.page.Page;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.forms.model.ExecResponse;
import py.com.sodep.mobileforms.api.services.forms.model.ICommandService;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormModificationService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;

//FIXME there are some hardcoded messages in English in some RuntimeExceptions
@Service("CommandModelService")
@Transactional
public class CommandService implements ICommandService {

	private static Logger logger = LoggerFactory.getLogger(CommandService.class);

	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	@Autowired
	private IFormService formService;

	@Autowired
	private IFormModificationService formModificationService;

	@Autowired
	private IElementPrototypeService elementPrototypeService;

	@Autowired
	private I18nBundle i18n;

	@Autowired
	private ILookupTableService lookupTableService;

	private ObjectMapper objectMapper = new ObjectMapper();

	private static class RecoverableException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused")
		RecoverableException() {
			super();
		}

		@SuppressWarnings("unused")
		RecoverableException(String message, Throwable cause) {
			super(message, cause);
		}

		RecoverableException(String message) {
			super(message);
		}

		@SuppressWarnings("unused")
		RecoverableException(Throwable cause) {
			super(cause);
		}

	}

	@Override
	public ExecResponse executeSave(User user, Long formId, List<Command> commands) {
		return executeSaveAs(user, null, formId, null, commands);
	}

	@Override
	public ExecResponse executeSaveAs(User user, Project project, Long formId, String label, List<Command> commands) {
		ExecResponse response = new ExecResponse();
		Command lastCommand = null;

		try {
			// FIXME Standardize the way to get the language
			Form form = null;
			String language = user.getLanguage();
			if (project != null && label != null) {
				form = formModificationService.copyIntoProject(user, project, formId, label);
			} else {
				if (commands.size() == 0) {
					return null;
				}

				FormDTO formDTO = formService.getFormLastVersion(formId, language);
				Long currentVersion = formDTO.getVersion();
				List<Long> publishedVersions = formDTO.getPublishedVersions();
				// ref #1627
				if (publishedVersions == null || publishedVersions.isEmpty()) {
					form = formService.getForm(formDTO.getId(), formDTO.getVersion());
				} else {
					if (publishedVersions.contains(currentVersion)) {
						// modifications are being made to a Form which its
						// current version
						// was already published
						form = formModificationService.createNewVersion(formId);
					} else {
						// The form has been published, but another version was
						// already created which isn't published.
						// We work on that version.
						form = formService.getForm(formDTO.getId(), formDTO.getVersion());
					}
				}

			}
			commands = new ArrayList<Command>(commands);
			response.setSuccess(true);
			for (Command cmd : commands) {
				lastCommand = cmd;
				Command.Type cmdType = cmd.getType();
				switch (cmdType) {
				case ADD:
					execAddCommand(form, cmd, user);
					break;
				case EDIT:
					execEditCommand(form, cmd,user);
					break;
				case DELETE:
					execDeleteCommand(form, cmd);
					break;
				}
				lastCommand = null;
			}

			// sequentialFlowToPages(form);
			checkFormConsistency(form, language);
			response.setFormId(form.getId());
		} catch (RecoverableException e) {
			logger.debug(e.getMessage(), e);
			packResponse(response, lastCommand, e);
			rollback();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			packResponse(response, lastCommand, e);
			rollback();
		}

		return response;
	}

	private void rollback() {
		// Rollback the transaction
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	}

	private void packResponse(ExecResponse response, Command lastCommand, Exception e) {
		response.setSuccess(false);
		response.setCmd(lastCommand);
		response.setExceptionThrown(e);
	}

	private void checkFormConsistency(Form form, String language) {
		List<Page> pages = form.getPages();
		if (pages == null || pages.isEmpty()) {
			throw new RecoverableException(i18n.getLabel(language,
					"services.commandService.validation.form.isEmpty"));
		}

		for (Page p : pages) {
			if (!p.getDeleted()) {
				checkPageConsistency(form, language, p);
			}
			if (p.getFlow() != null) {
				Flow f = p.getFlow();
				if (f.getDefaultTarget() == null && (f.getTargets() == null || f.getTargets().isEmpty())) {
					p.setFlow(null);
					// FIXME remove flow or logically delete?
					em.remove(f);
				}
			}
		}
	}

	private void checkPageConsistency(Form form, String language, Page p) {
		List<ElementInstance> elements = p.getElements();
		if (elements != null && !elements.isEmpty()) {
			for (ElementInstance e : elements) {
				if (!e.getDeleted()) {
					checkElementConsistency(form, language, e);
				}
			}
		}
	}

	// TODO
	// 4) If an element - X - values (e.g. values from a dropdown) depend on
	// another element - Y -, - Y - cannot depend on X. That circular dependence
	// makes no sense and actually might made the Android app become
	// unresponsive.
	private void checkElementConsistency(Form form, String language, ElementInstance e) {
		if (e.getPrototype() instanceof Select) {
			Select select = (Select) e.getPrototype();

			if (select.getSource() == OptionSource.LOOKUP_TABLE) {
				if (select.getLookupTableId() == null) {
					String label = select.getLabel(language);
					String message = i18n.getLabel(language,
							"services.commandService.validation.dropdown.noLookupTable", label);
					throw new RecoverableException(message);
				}

				if (select.getLookupValue() == null) {
					String label = select.getLabel(language);
					String message = i18n.getLabel(language,
							"services.commandService.validation.dropdown.noLookupValue", label);
					throw new RecoverableException(message);
				}

				if (select.getLookupLabel() == null) {
					String label = select.getLabel(language);
					String message = i18n.getLabel(language,
							"services.commandService.validation.dropdown.noLookupLabel", label);
					throw new RecoverableException(message);
				}
			} else {
				// It's embedded
				// #2195
				// 1) A dropdown with a embedded data as source must not have
				// filters
				e.setFilters(null);
				e.setDefaultValueColumn(null);
				e.setDefaultValueLookupTableId(null);
				Application app = form.getProject().getApplication();
				if (select.getLookupTableId() == null) {
					String label = select.getLabel(language);
					String message = i18n.getLabel(language,
							"services.commandService.validation.dropdown.noEmbeddedData", label);
					throw new RecoverableException(message);
				}
				List<MFManagedData> listAllData = lookupTableService.listAllData(app, select.getLookupTableId());
				if (listAllData.isEmpty()) {
					String label = select.getLabel(language);
					String message = i18n.getLabel(language,
							"services.commandService.validation.dropdown.noEmbeddedData", label);
					throw new RecoverableException(message);
				}
			}
		}
	}

	private List<Page> pages(Form form) {
		TypedQuery<Page> q = em.createQuery("SELECT p FROM " + Form.class.getSimpleName() + " f JOIN f.pages p "
				+ "WHERE f = :form AND p.deleted=false ORDER BY p.position ", Page.class);
		q.setParameter("form", form);
		return q.getResultList();
	}

	private List<ElementInstance> elements(Page page) {
		TypedQuery<ElementInstance> q = em.createQuery("SELECT e FROM " + Page.class.getSimpleName()
				+ " p JOIN p.elements e " + "WHERE p =:page AND e.deleted=false ORDER BY e.position",
				ElementInstance.class);
		q.setParameter("page", page);
		return q.getResultList();
	}

	// private void sequentialFlowToPages(Form form) {
	// List<Page> pages = pages(form);
	// if (pages != null && !pages.isEmpty()) {
	// int max = pages.size() - 1;
	// for (int i = 0; i < max; i++) {
	// Page p0 = pages.get(i);
	// Page p1 = pages.get(i + 1);
	// Flow flow = p0.getFlow();
	// if (flow == null) {
	// flow = new Flow();
	// p0.setFlow(flow);
	// }
	// flow.setDefaultTarget(p1.getInstanceId());
	// p0.setSave(false);
	// }
	// // #811, given the sequential nature of the flow
	// // the last page should be saveable
	// Page last = pages.get(max);
	// last.setSave(true);
	// }
	// }

	private void execAddCommand(Form form, Command cmd, User user) {
		MFRef objectToAdd = cmd.getRef();
		MFBaseModel.Type objType = objectToAdd.getType();
		switch (objType) {
		case FORM:
			throw new RuntimeException("Invalid ADD form command");
		case PAGE:
			execAddPageCommand(form, cmd);
			break;
		case ELEMENT:
			execAddElementCommand(form, cmd, user);
			break;
		}
		em.flush();
	}

	private void editForm(Form form, Command cmd) {
		List<Map<String, String>> attributes = cmd.getAttributes();
		String defaultLanguage = form.getDefaultLanguage();
		for (Map<String, String> attr : attributes) {
			String name = attr.get("name");
			String value = attr.get("value");
			if (name.equals("label")) {
				String language = attr.get("language");
				language = language == null ? defaultLanguage : language;
				form.setLabel(language, value);
			} else if (name.equals("defaultLanguage")) {
				defaultLanguage = value;
				form.setDefaultLanguage(defaultLanguage);
			} else if (name.equals("provideLocation")) {
				Boolean provideLocation = Boolean.parseBoolean(value);
				form.setProvideLocation(provideLocation);
			} else if (name.equals("movePage")) {
				String fromStr = attr.get("from");
				int from = Integer.parseInt(fromStr);
				String toStr = attr.get("to");
				int to = Integer.parseInt(toStr);
				movePage(form, from, to);
			}
		}
	}

	private void execAddPageCommand(Form form, Command cmd) {
		if (form == null) {
			throw new RuntimeException("No form");
		}

		Page page = new Page();
		page.setDefaultLanguage(form.getDefaultLanguage());
		logger.trace("add page");
		editPage(form, page, cmd);
		// we currently always add the page to the end
		// if the page is meant to be in another position
		// then a movePage command needs to be executed
		int lastPosition = pages(form).size();
		page.setPosition(lastPosition);
		if (form.getPages() == null) {
			form.setPages(new ArrayList<Page>());
		}
		form.addPage(page);
		page.setForm(form);
	}

	private void editPage(Form form, Page page, Command cmd) {
		List<Map<String, String>> attributes = cmd.getAttributes();
		for (Map<String, String> attr : attributes) {
			final String name = attr.get("name");
			final String value = attr.get("value");
			if (name.equals("position")) {
				int position = Integer.parseInt(value);
				page.setPosition(position);
				logger.trace("page.set({ position : " + position + " })");
			} else if (name.equals("label")) {
				String language = attr.get("language");
				language = language == null ? form.getDefaultLanguage() : language;
				page.setLabel(language, value);
				logger.trace("page.set({ label : " + value + ", language :"
						+ language + " })");
			} else if (name.equals("saveable")) {
				boolean save = Boolean.parseBoolean(value);
				page.setSave(save);
				logger.trace("page.set({ saveable : " + save + " })");
			} else if (name.equals("moveElement")) {
				String fromStr = attr.get("from");
				int from = Integer.parseInt(fromStr);
				String toStr = attr.get("to");
				int to = Integer.parseInt(toStr);
				moveElement(page, from, to);
				logger.trace("page.moveElement({ from : " + from + ", to : "
						+ to + " }");
			} else if (name.equals("defaultTarget")) {
				setDefaultTarget(form, page, value);
				logger.trace("page.set({ defaultTarget : " + value + " }");
			} else if (name.equals("targets")) {
				setConditionalTargets(form, page, value);
				logger.trace("page.set({ conditionalTargets : " + value + " }");
			}
		}
	}

	private void setDefaultTarget(Form form, Page page, String value) {
		if (page.getSave()) {
			throw new RuntimeException("Page is final, cannot set a default target");
		}

		Flow flow = page.getFlow();
		if (flow == null) {
			flow = new Flow();
			page.setFlow(flow);
		}

		if (value != null) {
			int position = Integer.parseInt(value);
			List<Page> pages = pages(form);
			Page target = getPageAt(pages, position);

			if (target.getInstanceId().equals(page.getInstanceId())) { // #1919
				throw new RuntimeException("A page cannot be its own default target");
			}
			flow.setDefaultTarget(target.getInstanceId());
		} else {
			flow.setDefaultTarget(null);
		}
	}

	private void setConditionalTargets(Form form, Page page, String value) {
		if (page.getSave()) {
			throw new RuntimeException("Page is final, cannot set conditional jumps");
		}

		Flow flow = page.getFlow();
		if (flow == null) {
			flow = new Flow();
			page.setFlow(flow);
		}

		if (value != null) {
			try {
				List<Map<String, String>> newTargets = objectMapper.readValue(value,
						new TypeReference<List<Map<String, String>>>() {
						});

				List<ConditionalTarget> targets = flow.getTargets();
				if (targets != null && !targets.isEmpty()) {
					Query query = em.createQuery("DELETE FROM " + ConditionalTarget.class.getSimpleName()
							+ " c WHERE c IN (:targets)");
					query.setParameter("targets", targets);
					query.executeUpdate();
				}
				targets = new ArrayList<ConditionalTarget>();
				flow.setTargets(targets);
				List<Page> pages = pages(form);
				List<ElementInstance> pageElements = elements(page);
				for (Map<String, String> newTargetData : newTargets) {
					ConditionalTarget ct = new ConditionalTarget();

					int elementPosition = Integer.parseInt(newTargetData.get("elementPosition"));
					ElementInstance element = getElementAt(pageElements, elementPosition);
					ct.setElementId(element.getInstanceId());

					Operator operator = MFConditionalTarget.Operator.valueOf(newTargetData.get("operator"));
					ct.setOperator(operator);

					String val = newTargetData.get("value");
					ct.setValue(val);

					int targetPagePosition = Integer.parseInt(newTargetData.get("targetPagePosition"));
					Page targetPage = getPageAt(pages, targetPagePosition);
					if (targetPage.getInstanceId().equals(page.getInstanceId())) { // #1919
						throw new RuntimeException("A page cannot be its own target in a conditional jump");
					}
					ct.setTarget(targetPage.getInstanceId());

					targets.add(ct);
				}
			} catch (JsonParseException e) {
				logger.error(e.getMessage(), e);
			} catch (JsonMappingException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			List<ConditionalTarget> targets = flow.getTargets();
			if (targets != null && !targets.isEmpty()) {
				Query query = em.createQuery("DELETE FROM " + ConditionalTarget.class.getSimpleName()
						+ " c WHERE c IN (:targets)");
				query.setParameter("targets", targets);
				query.executeUpdate();
			}
			flow.setTargets(null);
		}
	}

	private void moveElement(Page page, int from, int to) {
		List<ElementInstance> elements = elements(page);
		ElementInstance element = getElementAt(elements, from);
		int start = 0, end = 0, add = 0;
		if (from < to) {
			start = from + 1;
			end = to;
			add = -1;
		} else if (from > to) {
			start = to;
			end = from - 1;
			add = 1;
		}
		for (int i = start; i <= end; i++) {
			ElementInstance e = elements.get(i);
			e.setPosition(e.getPosition() + add);
		}
		element.setPosition(to);
	}

	private void movePage(Form form, int from, int to) {
		List<Page> pages = pages(form);
		Page page = getPageAt(pages, from);
		int start = 0, end = 0, add = 0;
		if (from < to) {
			start = from + 1;
			end = to;
			add = -1;
		} else if (from > to) {
			start = to;
			end = from - 1;
			add = 1;
		}
		for (int i = start; i <= end; i++) {
			Page p = pages.get(i);
			p.setPosition(p.getPosition() + add);
		}
		page.setPosition(to);
	}

	private void execAddElementCommand(Form form, Command cmd, User user) {
		if (form == null) {
			throw new RuntimeException("No form");
		}

		List<Page> pages = pages(form);
		if (pages == null) {
			throw new RuntimeException("No pages");
		}
		MFRef ref = cmd.getRef();
		Page page = getPage(pages, ref);
		if (page == null) {
			throw new RuntimeException("Invalid page");
		}

		ElementInstance instance = new ElementInstance();
		logger.trace("add element");
		editElement(form, instance, cmd, user);
		List<ElementInstance> elements = page.getElements();
		if (elements == null) {
			elements = new ArrayList<ElementInstance>();
			page.setElements(elements);
		}
		int position = instance.getPosition();
		for (ElementInstance e : elements) {
			if (!e.getDeleted() && e.getPosition() >= position) {
				e.setPosition(e.getPosition() + 1);
			}
		}
		elements.add(instance);
		em.flush();
		instance.setInstanceId("element" + instance.getId());
	}

	private void editElement(Form form, ElementInstance instance, Command cmd, User user) {
		String language = form.getDefaultLanguage();
		List<Map<String, String>> attributes = cmd.getAttributes();
		for (Map<String, String> attr : attributes) {
			final String name = attr.get("name");
			final String value = attr.get("value");

			if (name.startsWith("proto_")) {
				// Edit prototype attributes of embedded process items
				if (!instance.isEmbedded()) {
					throw new RuntimeException("Cannot change prototype attributes of no embedded process items");
				}
				String protoAttributeName = name.substring(6, name.length());
				editPrototypeAttribute(form, instance, protoAttributeName, value, language, user);
			} else if (name.equals("position")) {
				int position = Integer.parseInt(value);
				instance.setPosition(Integer.parseInt(value));
				logger.trace("element.set({ position : " + position + " })");
			} else if (name.equals("prototypeId")) {
				ElementPrototype prototype = instance.getPrototype();
				if(prototype != null) {
					throw new RuntimeException("Cannot change the prototype");
				}
				Long prototypeId = Long.parseLong(value);
				logger.trace("element.set({ prototypeId : " + prototypeId + " })");
				TypedQuery<ElementPrototype> q = em.createQuery("FROM "
						+ ElementPrototype.class.getSimpleName()
						+ " WHERE id = :id AND deleted = false",
						ElementPrototype.class);
				q.setParameter("id", prototypeId);
				ElementPrototype proto = q.getSingleResult();
				if (proto.isTemplate()) {
					Application app = form.getProject().getApplication();
					ElementPrototype newProto = embeddedProto(proto, app);
					newProto.setDefaultLanguage(language);
					// remove all the labels which are not from the current
					// language
					removeOtherLanguages(language, newProto);
					instance.setPrototype(newProto);
				} else {
					instance.setPrototype(proto);
				}
				List<ElementInstance> elements = proto.getElements();
				if (elements == null) {
					elements = new ArrayList<ElementInstance>();
					proto.setElements(elements);
				}
				elements.add(instance);
			} else if (name.equals("required")) {
				Boolean embedded = instance.getPrototype().isEmbedded();
				if (embedded != null && !embedded) {
					throw new RuntimeException(
							"Invalid command - Cannot change required in a not embedded Process item");
				} else {
					instance.setRequired(Boolean.parseBoolean(value));
				}
			} else if (name.equals("page")) {
				int oldPagePosition = cmd.getRef().getContainer().getPosition();
				int newPagePosition = Integer.parseInt(value);

				List<Page> pages = pages(form);
				Page oldPage = getPageAt(pages, oldPagePosition);
				Page newPage = getPageAt(pages, newPagePosition);

				oldPage.getElements().remove(instance);

				List<ElementInstance> elements = newPage.getElements();
				if (elements == null) {
					elements = new ArrayList<ElementInstance>();
					newPage.setElements(elements);
				}
				logger.trace("element.changePage({ from : " + oldPagePosition
						+ ", to : " + newPagePosition + " })");
				// elements.size() shouldn't be used here because it includes
				// the deleted elements
				// #3028
				// instance.setPosition(elements.size());
				int position = elements(newPage).size();
				instance.setPosition(position);
				elements.add(instance);
				logger.trace("element.set({ position : " + position + " })");
			} else if (name.equals("itemListFilters")) {
				setFilters(form, instance, MFFilter.Type.ITEM_LIST, value);
			} else if (name.equals("defaultValueFilters")) {
				setFilters(form, instance, MFFilter.Type.DEFAULT_VALUE, value);
			} else if (name.equals("defaultValueLookupTableId")) {
				if (value != null && !value.equals("-1")) {
					Long lookupId = Long.parseLong(value);
					instance.setDefaultValueLookupTableId(lookupId);
				} else {
					instance.setDefaultValueLookupTableId(null);
					instance.setDefaultValueColumn(null);
					setFilters(form, instance, MFFilter.Type.DEFAULT_VALUE, null);
				}
			} else if (name.equals("defaultValueColumn")) {
				instance.setDefaultValueColumn(value);
			}
			// refs #805
			// else if(name.equals("fieldName")){
			// instance.setFieldName(value);
			// }
		}

		if (instance.getPosition() == null || instance.getPrototype() == null) {
			throw new RuntimeException("Not enough information to add Element");
		}
	}

	private void removeOtherLanguages(String language, ElementPrototype newProto) {
		Iterator<String> iterator = newProto.getLabels().keySet()
				.iterator();
		while (iterator.hasNext()) {
			String l = iterator.next();
			if (!l.equals(language)) {
				iterator.remove();
			}
		}
	}

	private void setFilters(Form form, ElementInstance instance, MFFilter.Type type, String value) {
		try {
			List<Filter> newFilters = null;
			if (value == null) {
				newFilters = new ArrayList<Filter>();
			} else {
				newFilters = readFilters(form, instance, type, value);
			}

			if (instance.getFilters() == null || instance.getFilters().isEmpty()) {
				instance.setFilters(newFilters);
			} else {
				Query query = em.createQuery("DELETE FROM " + Filter.class.getSimpleName()
						+ " WHERE type= :type AND elementInstance = :elementInstance");
				query.setParameter("type", type);
				query.setParameter("elementInstance", instance);
				query.executeUpdate();
				em.flush();
				List<Filter> currentFilters = instance.getFilters();
				currentFilters.addAll(newFilters);
			}
		} catch (JsonParseException e) {
			logger.error(e.getMessage(), e);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private List<Filter> readFilters(Form form, ElementInstance instance, MFFilter.Type type, String value)
			throws JsonParseException, JsonMappingException, IOException {
		@SuppressWarnings("unchecked")
		List<Map<String, String>> filterListDTO = objectMapper.readValue(value, List.class);
		List<Filter> filters = new ArrayList<Filter>();

		for (Map<String, String> filterData : filterListDTO) {
			Filter f = new Filter();
			f.setElementInstance(instance);
			f.setType(type);
			// 1. Column
			f.setColumn(filterData.get("column"));
			// 2. Condition (EQUALS, DISTINCT, etc.)
			String operatorStr = filterData.get("operator");
			MFFilter.Operator operator = MFFilter.Operator.valueOf(operatorStr);
			f.setOperator(operator);
			// 3. ElementInstance that is the right value of the expression
			ElementInstance rightValueInstance = getRightValueInstance(pages(form), filterData);
			f.setRightValue(rightValueInstance.getInstanceId());
			filters.add(f);
		}

		return filters;
	}

	private ElementInstance getRightValueInstance(List<Page> pages, Map<String, String> filterData) {
		int pagePosition = Integer.parseInt(filterData.get("pagePosition"));
		int elementPosition = Integer.parseInt(filterData.get("elementPosition"));
		Page p = getPageAt(pages, pagePosition);
		return getElementAt(p.getElements(), elementPosition);
	}

	private void editPrototypeAttribute(Form form, ElementInstance instance, String name, String value,
			String language, User user) {
		ElementPrototype proto = instance.getPrototype();
		String label = proto.getLabel(language);
		if ("label".equals(name)) {
			proto.setLabel(language, value);
		} else {
			if (proto instanceof Input) {
				Input input = (Input) proto;
				if ("defaultValue".equals(name)) {
					input.setDefaultValue(value);
				} else if ("minLength".equals(name)) {
					if (value != null) {

						try {
							int minLength = Integer.parseInt(value);
							input.setMinLength(minLength);
						} catch (NumberFormatException e) {
							logger.trace("Can't assign '" + value + "' to the property minlength");
							// do nothing we should check afterwards because
							// there might be several commands for the same
							// element in a row
						}
					} else {

						input.setMinLength(null);
					}

				} else if ("maxLength".equals(name)) {
					if (value != null) {
						try {
							int maxLength = Integer.parseInt(value);
							input.setMaxLength(maxLength);
						} catch (NumberFormatException e) {
							logger.trace("Can't assign '" + value + "' to the property maxlength");
							// do nothing we should check afterwards because
							// there might be several commands for the same
							// element in a row
						}
					} else {
						input.setMaxLength(null);
					}

				} else if ("readOnly".equals(name)) {
					input.setReadOnly(Boolean.parseBoolean(value));
				}
			} else if (proto instanceof Select) {
				Select select = (Select) proto;
				if ("defaultValue".equals(name)) {
					select.setDefaultValue(value);
				} else if ("source".equals(name)) {
					OptionSource source = OptionSource.valueOf(value);
					select.setSource(source);
				} else if ("lookupTableId".equals(name)) {
					if (value != null && !value.equals("-1")) {
						Long id = Long.parseLong(value);
						select.setLookupTableId(id);
					} else {
						select.setLookupTableId(null);
					}
				} else if ("lookupLabel".equals(name)) {
					// TODO check that the column is a valid column for the
					// given lookup table
					if (value != null && !value.equals("-1")) {
						select.setLookupLabel(value);
					} else {
						select.setLookupLabel(null);
					}
				} else if ("lookupValue".equals(name)) {
					// TODO check that the column is a valid column for the
					// given lookup table
					if (value != null && !value.equals("-1")) {
						select.setLookupValue(value);
					} else {
						select.setLookupValue(null);
					}
				} else if ("embeddedValues".equals(name)) {
					if (select.getSource() == OptionSource.EMBEDDED) {
						List<Map<String, String>> values = parseEmbeddedValues(value);
						// FIXME what about the user?
						Application app = form.getProject().getApplication();
						elementPrototypeService.createSelect(app, null, select, values, user, language,
								select.getLabel(language));
					} else {
						throw new RuntimeException("Invalid source type");
					}
				}
			} else if (proto instanceof Photo) {
				if ("cameraOnly".equals(name)) {
					Photo photo = (Photo) proto;
					photo.setCameraOnly(Boolean.parseBoolean(value));
				}
			} else if (proto instanceof Location) {

			} else if (proto instanceof Checkbox){
				if("checked".equals(name)){
					Checkbox checkbox = (Checkbox) proto;
					checkbox.setChecked(Boolean.parseBoolean(value));
				}
			} else if(proto instanceof Barcode){
				//TODO Barcode properties
			} else if(proto instanceof Signature){
				//TODO Signature properties
			}

		}
	}

	private List<Map<String, String>> parseEmbeddedValues(String value) {
		StringReader in = new StringReader(value);
		BufferedReader reader = new BufferedReader(in);
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					Map<String, String> map = new HashMap<String, String>(1);
					map.put(Select.DROPDOWN_TEXT_FIELD, line);
					list.add(map);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return list;
	}

	private ElementPrototype embeddedProto(ElementPrototype proto, Application app) {
		try {
			ElementPrototype newProto = proto.clone();
			newProto.setInstantiability(InstantiabilityType.EMBEDDED);
			newProto.setApplication(app);
			em.persist(newProto);
			// TODO
			// should root id point to itself?
			// yes, I think that it should (Daniel Cricco)
			return newProto;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}

	}

	private Page getPage(List<Page> pages, MFRef ref) {
		MFRef pageObj = ref.getContainer();
		int position = pageObj.getPosition();
		for (Page p : pages) {
			if (!p.getDeleted() && p.getPosition().equals(position)) {
				return p;
			}
		}

		return null;
	}

	private void execEditCommand(Form form, Command cmd, User user) {
		if (form == null) {
			throw new RuntimeException("Invalid form");
		}

		MFRef refToEdit = cmd.getRef();
		MFBaseModel.Type objType = refToEdit.getType();
		switch (objType) {
		case FORM:
			execEditFormCommand(form, cmd);
			break;
		case PAGE:
			execEditPageCommand(form, cmd);
			break;
		case ELEMENT:
			execEditElementCommand(form, cmd, user);
			break;
		// FIXME This enum is really awful. An Input, Location, Photo, Select or
		// Healine are Elements!. It mixes and messes up things. I'm to blame.
		// Sorry.
		// jmpr
		case HEADLINE:
		case INPUT:
		case LOCATION:
		case PHOTO:
		case SELECT:
			throw new RuntimeException("Unexpected reference type");
		}
		em.flush();
	}

	private void execEditFormCommand(Form form, Command cmd) {
		editForm(form, cmd);
	}

	private void execEditPageCommand(Form form, Command cmd) {
		Page page = getPage(form, cmd);
		logger.trace("edit page");
		editPage(form, page, cmd);
	}

	private Page getPage(Form form, Command cmd) {
		MFRef pageRef = cmd.getRef();
		Long pageId = pageRef.getId();

		List<Page> pages = pages(form);
		Page page = getPageById(pageId, pages);
		if (page == null) {
			Integer position = pageRef.getPosition();
			if (position == null) {
				throw new RuntimeException("No way to know which page to edit");
			}
			page = getPageAt(pages, position);
		}

		if (page == null) {
			throw new RuntimeException("Invalid page " + pageId);
		}
		return page;
	}

	private Page getPageAt(List<Page> pages, int position) {
		for (Page p : pages) {
			if (!p.getDeleted() && p.getPosition().equals(position)) {
				return p;
			}
		}
		return null;
	}

	private Page getPageById(Long pageId, List<Page> pages) {
		Page page = null;
		for (Page p : pages) {
			if (p.getId() != null && p.getId().equals(pageId)) {
				page = p;
				break;
			}
		}
		return page;
	}

	private void execEditElementCommand(Form form, Command cmd, User user) {
		ElementInstance instance = getInstance(form, cmd);
		logger.trace("edit element");
		editElement(form, instance, cmd, user);
	}

	private ElementInstance getInstance(final Form form, final Command cmd) {
		MFRef elementRef = cmd.getRef();
		MFRef pageRef = elementRef.getContainer();
		Page page = getPageAt(pages(form), pageRef.getPosition());
		List<ElementInstance> elements = elements(page);
		ElementInstance element = getElementAt(elements, elementRef.getPosition());
		return element;
	}

	private ElementInstance getElementAt(List<ElementInstance> elements, int position) {
		for (ElementInstance e : elements) {
			if (!e.getDeleted() && e.getPosition().equals(position)) {
				return e;
			}
		}
		throw new RuntimeException("No element at position " + position);
	}

	private void execDeleteCommand(Form form, Command cmd) {
		if (form == null) {
			throw new RuntimeException("Invalid form");
		}

		MFRef refToEdit = cmd.getRef();
		MFBaseModel.Type objType = refToEdit.getType();
		switch (objType) {
		case FORM:
			execDeleteFormCommand(form, cmd);
			break;
		case PAGE:
			execDeletePageCommand(form, cmd);
			break;
		case ELEMENT:
			execDeleteElementCommand(form, cmd);
			break;
		}
		em.flush();
	}

	private void execDeleteElementCommand(Form form, Command cmd) {
		MFRef elementRef = cmd.getRef();
		MFRef pageRef = elementRef.getContainer();
		Page page = getPageAt(pages(form), pageRef.getPosition());
		List<ElementInstance> elements = elements(page);
		ElementInstance element = getElementAt(elements, elementRef.getPosition());
		element.setDeleted(true);
		String instanceId = element.getInstanceId();
		for (ElementInstance e : elements) {
			if (e.getPosition() > element.getPosition()) {
				e.setPosition(e.getPosition() - 1);
			}
			// #2195
			// 3) If an element is deleted, all default value filters ...
			removeFilterReferences(instanceId, e);
		}
		// ... and navigation jumps that depend on it should be deleted
		removeJumpsThatDependOnElement(instanceId, page);
	}

	/**
	 * If there's any conditional jump that depends on the element with the
	 * given instanceId in the page, the jump is removed.
	 * 
	 * @param instanceId
	 * @param currentPage
	 */
	private void removeJumpsThatDependOnElement(String instanceId, Page currentPage) {
		Flow flow = currentPage.getFlow();
		if (flow != null) {
			List<ConditionalTarget> targets = flow.getTargets();
			if (targets != null) {
				Iterator<ConditionalTarget> iter = targets.iterator();
				while (iter.hasNext()) {
					ConditionalTarget t = iter.next();
					String elementId = t.getElementId();
					if (elementId.equals(instanceId)) {
						if (em.contains(t)) {
							em.remove(t);
						}
						iter.remove();
					}
				}
			}
		}
	}

	/**
	 * If the element e has any filter that references the element to be deleted
	 * (identified by its instanceId), the filter is removed.
	 * 
	 * @param deletedInstanceId
	 * @param e
	 */
	private void removeFilterReferences(String deletedInstanceId, ElementInstance e) {
		List<Filter> filters = e.getFilters();
		if (filters != null) {
			Iterator<Filter> iter = filters.iterator();
			while (iter.hasNext()) {
				Filter f = iter.next();
				ElementInstance elementInFilter = f.getElementInstance();
				if (elementInFilter.getInstanceId().equals(deletedInstanceId)) {
					if (em.contains(f)) {
						em.remove(f);
					}
					iter.remove();
				}
			}
		}
	}

	private void execDeletePageCommand(Form form, Command cmd) {
		Page page = getPage(form, cmd);
		page.setDeleted(true);
		List<Page> pages = pages(form);
		String instanceId = page.getInstanceId();
		for (Page p : pages) {
			if (p.getPosition() > page.getPosition()) {
				p.setPosition(p.getPosition() - 1);
			}

			// #2195
			// 2) If a page is deleted, all navigation jumps that refer to that
			// page should be deleted
			removeJumpsToDeletedPage(instanceId, p);
		}
	}

	/**
	 * If a page is deleted (the first argument is the instanceId of the deleted
	 * page) any jump from page p to that deleted page is removed
	 * 
	 * @param deletePageInstanceId
	 * @param currentPage
	 */
	private void removeJumpsToDeletedPage(String deletePageInstanceId, Page currentPage) {
		Flow flow = currentPage.getFlow();
		if (flow != null) {
			String defaultTarget = flow.getDefaultTarget();
			if (defaultTarget != null && defaultTarget.equals(deletePageInstanceId)) {
				flow.setDefaultTarget(null);
			}
			List<ConditionalTarget> targets = flow.getTargets();
			if (targets != null) {
				Iterator<ConditionalTarget> iter = targets.iterator();
				while (iter.hasNext()) {
					ConditionalTarget t = iter.next();
					String target = t.getTarget();
					if (target.equals(deletePageInstanceId)) {
						if (em.contains(t)) {
							em.remove(t);
						}
						iter.remove();
					}
				}
			}
		}
	}

	private void execDeleteFormCommand(Form form, Command cmd) {
		// TODO should this be implemented?, should it be possible?
		throw new RuntimeException("NOT IMPLEMENTED YET");
	}

	@Override
	public ExecResponse executeSave(User user, Long formId, Command[] commands) {
		return executeSave(user, formId, Arrays.asList(commands));
	}

	@Override
	public ExecResponse executeSaveAs(User user, Project project, Long formId, String label, Command[] commands) {
		return executeSaveAs(user, project, formId, label, Arrays.asList(commands));
	}
}
