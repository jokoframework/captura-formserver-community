package py.com.sodep.mobileforms.web.controllers;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.dtos.BasicSelectDTO;
import py.com.sodep.mobileforms.api.dtos.ProcessItemDTO;
import py.com.sodep.mobileforms.api.dtos.FormProcessItemInfoDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.forms.elements.Select;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.exceptions.ElementPrototypeInUseException;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.exceptions.PersistenceException;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormModificationService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
import py.com.sodep.mobileforms.api.services.metadata.pools.IPoolService;
import py.com.sodep.mobileforms.web.entitybuilders.ProcessItemBuilder;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class ProcessItemController extends SodepController {

	@Autowired
	private IElementPrototypeService elementPrototypeService;

	@Autowired
	private IPoolService poolService;

	@Autowired
	private IFormModificationService formModificationService;

	@Autowired
	private IFormService formService;

	@RequestMapping("/processItems/edit.ajax")
	public @ResponseBody
	JsonResponse<ProcessItemDTO> edit(HttpServletRequest request, @RequestBody ProcessItemDTO dto) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Application app = mgr.getApplication();

		User currentUser = getUser(request);
		Long rootId = dto.getRootId();

		// The default language of the process item can't be changed on newer
		// version. This is a workaround until we finally implement translation.
		// Otherwise, we will have several entries on element_labels for
		// different languages if the process item was created with a given
		// language and then updated with a different language preference
		String defaultLanguage = elementPrototypeService.getDefaultLanguageOfProcessItem(rootId);
		if (dto.getId() == null && rootId == null && dto.getVersion() == null) {
			JsonResponse<ProcessItemDTO> response = new JsonResponse<ProcessItemDTO>();
			response.setMessage(i18n.getMessage("admin.processitem.save.failure"));
			Map<String, Object> messages = new LinkedHashMap<String, Object>();

			messages.put("Error", "Process item ID not provided to the edit method");
			response.setContent(messages);
			return response;
		}

		ProcessItemBuilder builder = new ProcessItemBuilder(defaultLanguage);
		JsonResponse<ProcessItemDTO> response = validateProcessItemDTO(dto, i18n, builder);
		if (response != null) {
			return response;
		}
		response = new JsonResponse<ProcessItemDTO>();

		Pool pool = getPool(dto, app);

		ElementPrototype ep = builder.newElementPrototype(dto);
		try {
			if (ep instanceof Select) {
				ElementPrototype savedProcessItem = elementPrototypeService.updateSelect(app, pool, (Select) ep,
						rootId, dto.getOptions(), currentUser, dto.getLabel());
				String label = elementPrototypeService.getLabel(savedProcessItem.getId(), defaultLanguage);
				ProcessItemDTO obj = builder.buildDTO(savedProcessItem, label);
				List<Map<String, String>> options = elementPrototypeService.getSelectOptions(savedProcessItem.getRoot()
						.getId());
				obj.setOptions(options);
				response.setObj(obj);
				response.setSuccess(true);
				response.setMessage(i18n.getMessage("admin.processitem.save.success"));
			} else {
				ElementPrototype savedProcessItem = elementPrototypeService.update(ep, rootId, pool, dto.getLabel());
				String label = elementPrototypeService.getLabel(savedProcessItem.getId(), defaultLanguage);
				ProcessItemDTO obj = builder.buildDTO(savedProcessItem, label);
				response.setObj(obj);
				response.setSuccess(true);
				response.setMessage(i18n.getMessage("admin.processitem.save.success"));
			}
		} catch (InvalidEntityException e) {
			sodepServiceExceptionToJsonResponse(request, i18n, response, e);
		} catch (PersistenceException e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.processitem.save.failure"));
			response.setMessage(e.getMessage());
		}
		return response;
	}

	@RequestMapping(value = "/processItems/save.ajax", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse<ProcessItemDTO> save(HttpServletRequest request, @RequestBody ProcessItemDTO dto) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		String defaultLanguage = i18n.getSelectedLanguage();
		Application app = manager.getApplication();
		User currentUser = getUser(request);

		Pool pool = getPool(dto, app);

		ProcessItemBuilder builder = new ProcessItemBuilder(defaultLanguage);

		JsonResponse<ProcessItemDTO> response = validateProcessItemDTO(dto, i18n, builder);
		if (response != null) {
			return response;
		}

		response = new JsonResponse<ProcessItemDTO>();
		ElementPrototype ep = builder.newElementPrototype(dto);

		try {
			if (ep instanceof Select) {
				ElementPrototype savedProcessItem = elementPrototypeService.createSelect(app, pool, (Select) ep,
						dto.getOptions(), currentUser, defaultLanguage, dto.getLabel());
				String label = elementPrototypeService.getLabel(savedProcessItem.getId(), defaultLanguage);
				ProcessItemDTO obj = builder.buildDTO(savedProcessItem, label);
				List<Map<String, String>> options = elementPrototypeService.getSelectOptions(savedProcessItem.getRoot()
						.getId());
				obj.setOptions(options);
				response.setObj(obj);
				response.setSuccess(true);
				response.setMessage(i18n.getMessage("admin.processitem.save.success"));

			} else {
				ElementPrototype savedProcessItem = elementPrototypeService.create(ep, pool, defaultLanguage,
						dto.getLabel());
				String label = elementPrototypeService.getLabel(savedProcessItem.getId(), defaultLanguage);
				ProcessItemDTO obj = builder.buildDTO(savedProcessItem, label);
				response.setObj(obj);
				response.setSuccess(true);
				response.setMessage(i18n.getMessage("admin.processitem.save.success"));
			}
		} catch (InvalidEntityException e) {
			sodepServiceExceptionToJsonResponse(request, i18n, response, e);
		} catch (PersistenceException e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.processitem.save.failure"));
			response.setMessage(e.getMessage());
		}

		return response;
	}

	@RequestMapping("/processItems/delete.ajax")
	public @ResponseBody
	JsonResponse<String> deleteProcessItem(HttpServletRequest request, @RequestParam(value = "id") Long id) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();
		ElementPrototype elementPrototype = elementPrototypeService.findLastVersion(id);
		String language = i18n.getSelectedLanguage();
		String label = elementPrototypeService.getLabel(id, language);
		try {
			elementPrototypeService.logicalDelete(elementPrototype);
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("admin.cruds.processitem.deleted.title"));
			response.setUnescapedMessage(i18n.getMessage("admin.cruds.processitem.deleted.message", label));
		} catch (ElementPrototypeInUseException e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			StringBuilder sb = new StringBuilder(i18n.getMessage("admin.cruds.processitem.delete.form.depends.on"));
			sb.append("<br />");
			List<Form> forms = e.getForms();
			for (Form f : forms) {
				String formLabel = formService.getLabel(f.getId(), language);
				sb.append(formLabel);
				sb.append(",");
				sb.append(i18n.getMessage("web.generic.version"));
				sb.append(" : ");
				sb.append(f.getVersion());
				sb.append("<br/>");
			}
			response.setUnescapedMessage(sb.toString());
		} catch (Exception e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			response.setUnescapedMessage(i18n.getMessage("admin.cruds.processitem.deleted.error", label));
		}
		return response;
	}

	@RequestMapping(value = "/processItems/get.ajax")
	public @ResponseBody
	JsonResponse<ProcessItemDTO> getProcessItem(HttpServletRequest request, @RequestParam(value = "rootId") Long rootId) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		String defaultLanguage = i18n.getSelectedLanguage();

		JsonResponse<ProcessItemDTO> response = new JsonResponse<ProcessItemDTO>();

		ElementPrototype ep = elementPrototypeService.findLastVersion(rootId);

		if (ep == null) {
			response.setSuccess(false);
			response.setMessage("No process item with the provided version");
			return response;
		}

		ProcessItemBuilder builder = new ProcessItemBuilder(defaultLanguage);
		String label = elementPrototypeService.getLabel(ep.getId(), defaultLanguage);
		ProcessItemDTO obj = builder.buildDTO(ep, label);
		if (ep instanceof Select) {
			List<Map<String, String>> options = elementPrototypeService.getSelectOptions(rootId);
			obj.setOptions(options);
		}
		response.setSuccess(true);
		response.setObj(obj);
		return response;
	}

	@RequestMapping(value = "/processItems/upgradeInForm.ajax")
	public @ResponseBody
	JsonResponse<String> upgradeProcessItemInForm(HttpServletRequest request,
			@RequestParam(value = "processItemId", required = true) Long processItemId,
			@RequestParam(value = "formId", required = true) Long formId) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();
		formModificationService.upgradeElementPrototypeInForm(formId, processItemId);
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.processitem.upgrade.success"));
		return response;
	}

	@RequestMapping(value = "/processItems/upgradeInAllForms.ajax")
	public @ResponseBody
	JsonResponse<String> upgradeProcessItemInAllForms(HttpServletRequest request,
			@RequestParam(value = "processItemId", required = true) Long processItemId) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();
		formModificationService.upgradeElementPrototypeInAllForms(processItemId);
		response.setTitle(i18n.getMessage("admin.processitem.upgradeAll.success"));
		response.setSuccess(true);
		return response;
	}

	@RequestMapping("/processItems/typeMetadata.ajax")
	public @ResponseBody
	JsonResponse<BasicSelectDTO> getProcessItemTypeMetadata(HttpServletRequest request) {
		I18nManager i18n = I18nManager.getI18n(request);
		JsonResponse<BasicSelectDTO> response = new JsonResponse<BasicSelectDTO>();
		BasicSelectDTO dto = getProcessItemTypeMetadataDTO(i18n);
		response.setSuccess(true);
		response.setObj(dto);
		return response;
	}

	private BasicSelectDTO getProcessItemTypeMetadataDTO(I18nManager i18n) {

		BasicSelectDTO dto = new BasicSelectDTO();

		dto.addMapElement("text", i18n.getMessage("admin.form.processitem.type.text"), new String[] { "min", "max",
				"readonly", "defaultValue" });
		dto.addMapElement("date", i18n.getMessage("admin.form.processitem.type.date"), new String[] { "readonly",
				"defaultValue" });
		dto.addMapElement("time", i18n.getMessage("admin.form.processitem.type.time"), new String[] { "readonly",
				"defaultValue" });
// #2258 Hide process item of type password	
//		dto.addMapElement("password", i18n.getMessage("admin.form.processitem.type.password"), new String[] { "min",
//				"max", "readonly", "defaultValue" });
		dto.addMapElement("integer", i18n.getMessage("admin.form.processitem.type.integer"), new String[] { "min",
				"max", "readonly", "defaultValue" });
		dto.addMapElement("decimal", i18n.getMessage("admin.form.processitem.type.decimal"), new String[] { "min",
				"max", "readonly", "defaultValue" });
		dto.addMapElement("textarea", i18n.getMessage("admin.form.processitem.type.textarea"), new String[] { "min",
				"max", "readonly", "defaultValue" });
		dto.addMapElement("headline", i18n.getMessage("admin.form.processitem.type.headline"), new String[] {});

		dto.addMapElement("location", i18n.getMessage("admin.form.processitem.type.location"), new String[] {
				"defaultLatitude", "defaultLongitude" });

		dto.addMapElement("photo", i18n.getMessage("admin.form.processitem.type.photo"), new String[] {});

		HashMap<String, Object> selectTypes = new HashMap<String, Object>();
		selectTypes.put("manual",
				new String[] { "multiple", "source", "lookupIdentifier", "lookupLabel", "lookupValue" });
		selectTypes.put("dynamic", new String[] { "multiple", "source" });
		dto.addMapElement("select", i18n.getMessage("admin.form.processitem.type.select"), selectTypes);

		return dto;
	}

	@RequestMapping("/processItem/paging/forms.ajax")
	public @ResponseBody
	PagedData<List<FormProcessItemInfoDTO>> formsForProcessItem(HttpServletRequest request,
			@RequestParam(value = "rootId") Long rootId,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false) String orderBy,
			@RequestParam(value = "sord", required = false) String order,
			@RequestParam(value = "filters", required = false) String filters,
			@RequestParam(value = "searchOper", required = false) String searchOper,
			@RequestParam(value = "searchField", required = false) String searchField,
			@RequestParam(value = "searchString", required = false) String searchString) {

		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		String lang = i18n.getSelectedLanguage();

		boolean ascending = true;
		if (order != null) {
			ascending = order.equalsIgnoreCase("asc");
		}

		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = FormProcessItemInfoDTO.ID;
		}

		PagedData<List<FormProcessItemInfoDTO>> formData = elementPrototypeService.formsUsingProcessItem(
				rootId, orderBy, ascending, page, rows, lang);

		return formData;

	}

	private Pool getPool(ProcessItemDTO dto, Application app) {
		Long poolId = dto.getPool();
		Pool pool = null;
		if (poolId != null && poolId != 0) {
			pool = poolService.findById(poolId);
		}

		if (poolId != null && poolId != 0 && pool == null) {
			throw new RuntimeException("invalid pool");
		}
		return pool;
	}

	private JsonResponse<ProcessItemDTO> validateProcessItemDTO(ProcessItemDTO dto, I18nManager i18n,
			ProcessItemBuilder builder) {
		JsonResponse<ProcessItemDTO> response = new JsonResponse<ProcessItemDTO>();
		// Do the validation
		Map<String, String> errors = builder.validate(dto);
		if (errors.size() > 0) {
			response.setSuccess(false);
			response.setMessage(i18n.getMessage("admin.processitem.save.failure"));
			Map<String, Object> messages = new LinkedHashMap<String, Object>();
			for (Entry<String, String> entry : errors.entrySet()) {
				messages.put(i18n.getMessage(entry.getKey()), i18n.getMessage(entry.getValue()));
			}
			response.setContent(messages);
			return response;
		}
		return null;
	}

}
