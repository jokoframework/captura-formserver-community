package py.com.sodep.mobileforms.web.controllers.cruds;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mf.exchange.objects.device.MFDevice;
import py.com.sodep.mf.exchange.objects.device.MFDeviceInfo;
import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.dtos.UserDTODevices;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.SodepServiceOriginatedException;
import py.com.sodep.mobileforms.api.services.auth.IAuthenticationService;
import py.com.sodep.mobileforms.api.services.license.MFLicenseManager;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IDeviceService;
import py.com.sodep.mobileforms.api.services.metadata.core.IGroupService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.license.MFApplicationLicense;
import py.com.sodep.mobileforms.utils.BeanUtils;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.json.jqgrid.ColModel;
import py.com.sodep.mobileforms.web.session.SessionManager;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Controller
public class UserCrudController extends SodepController {

	@Autowired
	private IUserService userService;

	@Autowired
	private IGroupService groupService;

	@Autowired
	private IDeviceService deviceService;

	@Autowired
	private MFLicenseManager licenseManager;

	@Autowired
	private IAuthenticationService authenticationService;
	
	@Autowired
	private IApplicationService applicationService;
	
	private static final int MIN_PASSWORD_LENGTH = 8;
	
	public static final String MAIL_REG_EXP = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	@RequestMapping("/cruds/users/paging/read.ajax")
	public @ResponseBody
	PagedData<List<UserDTODevices>> read(HttpServletRequest request,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false) String orderBy,
			@RequestParam(value = "sord", required = false) String order,
			@RequestParam(value = "_search", required = false, defaultValue = "") String _search,
			@RequestParam(value = "filters", required = false) String filters,
			@RequestParam(value = "searchOper", required = false) String searchOper,
			@RequestParam(value = "searchField", required = false) String searchField,
			@RequestParam(value = "searchString", required = false) String searchString) {

		SessionManager mgr = new SessionManager(request);
		Application app = mgr.getApplication();

		String groupParameter = request.getParameter("group");
		Group group = null;
		if (groupParameter != null && !groupParameter.isEmpty()) {
			Long groupId = Long.parseLong(request.getParameter("group"));
			group = groupService.findById(app, groupId);
		}

		boolean ascending = true;
		if (order != null) {
			ascending = order.equalsIgnoreCase("asc");
		}

		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = User.MAIL;
		}

		boolean search = _search.equals("true");

		PagedData<List<User>> serviceData = null;

		if (!search) {
			if (group == null) {
				serviceData = userService.findAll(app, orderBy, ascending, page, rows);
			} else {
				serviceData = userService.findAll(app, group, true, orderBy, ascending, page, rows);
			}
		} else {
			if (searchField.equals("id")) {
				Long val = Long.parseLong(searchString);
				if (group == null) {
					serviceData = userService.findByProperty(app, searchField, searchOper, val, orderBy, ascending,
							page, rows);
				} else {
					serviceData = userService.findByProperty(app, group, true, searchField, searchOper, val, orderBy,
							ascending, page, rows);
				}
			} else {
				if (group == null) {
					serviceData = userService.findByProperty(app, searchField, searchOper, searchString, orderBy,
							ascending, page, rows);
				} else {
					serviceData = userService.findByProperty(app, group, true, searchField, searchOper, searchString,
							orderBy, ascending, page, rows);
				}
			}
		}

		PagedData<List<UserDTODevices>> controllerData = new PagedData<List<UserDTODevices>>();
		List<UserDTODevices> data = new ArrayList<UserDTODevices>();

		MFApplicationLicense license = licenseManager.getLicense(app.getId());
		for (User u : serviceData.getData()) {
			UserDTODevices dto = new UserDTODevices(UserViewUtils.translate(u));
			dto.setMember(userService.isMember(app.getId(), u));
			List<MFDevice> devices = deviceService.getDevicesOfUser(app, u);
			dto.setDevices(devices);
			dto.setMaxDevices(license.getMaxDevices());
			data.add(dto);
		}

		BeanUtils.mapBean(serviceData, controllerData);
		controllerData.setData(data);

		return controllerData;
	}

	@RequestMapping("/cruds/users/columninfo.ajax")
	protected @ResponseBody
	JsonResponse<?> columnInfo(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		Map<String, Object> content = new HashMap<String, Object>();
		JsonResponse<Object> response = new JsonResponse<Object>();
		response.setSuccess(true);
		response.setContent(content);

		String[] cols = { User.ID, User.MAIL, User.FIRSTNAME, User.LASTNAME, SodepEntity.ACTIVE, "devices", "actions" };
		String[] colNames = { i18n.getMessage("admin.cruds.user.cols.id"),
				i18n.getMessage("admin.cruds.user.cols.mail")+"/"+i18n.getMessage("admin.cruds.user.cols.username"), i18n.getMessage("admin.cruds.user.cols.firstName"),
				i18n.getMessage("admin.cruds.user.cols.lastName"), i18n.getMessage("admin.cruds.user.cols.active"),
				i18n.getMessage("admin.cruds.user.cols.devices"), "" };

		content.put("cols", cols);
		content.put("colNames", colNames);

		List<ColModel> colModel = new ArrayList<ColModel>();
		colModel.add(new ColModel().name(cols[0]).index(cols[0]).hidden(true));
		colModel.add(new ColModel().name(cols[1]).index(cols[1]).formatter(ColModel.CUSTOM_FORMATTER).width("230px"));
		colModel.add(new ColModel().name(cols[2]).index(cols[2]).formatter(ColModel.CUSTOM_FORMATTER).width("150px"));
		colModel.add(new ColModel().name(cols[3]).index(cols[3]).formatter(ColModel.CUSTOM_FORMATTER).width("100px"));
		ColModel active = new ColModel().name(cols[4]).index(cols[4]).edittype("checkbox")
				.formatter(ColModel.CUSTOM_FORMATTER).width("30px").hidden(true);
		active.addEditoption("value", "true:false");
		colModel.add(active);

		colModel.add(new ColModel().name(cols[5]).index(cols[5]).formatter(ColModel.CUSTOM_FORMATTER).width("100px"));
		colModel.add(new ColModel().name(cols[6]).index(cols[6]).search(false).sortable(false).width("220")
				.align("center").formatter(ColModel.CUSTOM_FORMATTER).width("100px"));

		content.put("colModel", colModel);

		content.put("sortorder", "asc");
		content.put("sortname", User.MAIL);
		content.put("addCaption", i18n.getMessage("admin.cruds.user.form.addCaption"));
		content.put("editCaption", i18n.getMessage("admin.cruds.user.form.editCaption"));
		return response;
	}

	private User getUser(String id, String firstName, String lastName, String mail, String password, Boolean active) {
		User user = new User();
		if (id != null && !id.trim().isEmpty() && !id.equals("_empty")) {
			user.setId(Long.parseLong(id));
		}
		user.setActive(active);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setMail(mail);
		user.setPassword(password);
		return user;
	}

	@RequestMapping(value = "/cruds/users/preSave.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<UserStatus> preSave(HttpServletRequest request, @RequestParam(value = "mail") String mail, @RequestParam(value = "add")
									Boolean addDirectly) {
		SessionManager mgr = new SessionManager(request);
		Application app = mgr.getApplication();
		I18nManager i18n = mgr.getI18nManager();
		JsonResponse<UserStatus> response = new JsonResponse<UserStatus>();
		response.setSuccess(true);

		User user = userService.findByMail(mail.trim());

		UserStatus status = new UserStatus();
		response.setObj(status);
		if (user != null) {
			if (userService.isMember(app.getId(), user) || userService.isInvited(app.getId(), user)) {
				status.setStatus(UserStatus.MEMBER);
				response.setUnescapedMessage(i18n.getMessage("web.new-user.alreadyInApp", user.getMail()));
				response.setSuccess(false);

			} else {
				status.setStatus(UserStatus.EXISING);
				if (!addDirectly) {
					response.setUnescapedMessage(i18n.getMessage("web.new-user.alreadyExists", user.getMail()));
				} else {
					response.setUnescapedMessage(i18n.getMessage("web.new-user.addDirectly.alreadyExists", user.getMail()));
				}

			}
		} else {
			status.setStatus(UserStatus.NON_EXISTING);
			if (!addDirectly) {
				response.setUnescapedMessage(i18n.getMessage("web.new-user.addNewAccount", mail));
			} else {
				response.setUnescapedMessage(i18n.getMessage("web.new-user.addDirectly.addNewAccount", mail));
			}
		}
		return response;
	}

	@RequestMapping(value = "/cruds/users/get.ajax", method = RequestMethod.POST)
	@ResponseBody
	UserDTO getUser(HttpServletRequest request, @RequestParam(value = "userId") Long userId) {
		UserDTO userDTO = userService.getUserDTO(userId);
		userDTO.setPassword(null);
		if (!userDTO.getMail().matches(MAIL_REG_EXP)) {
			userDTO.setUsername(userDTO.getMail());
			userDTO.setMail(null);
		}
		return userDTO;
	}

	@RequestMapping(value = "/cruds/users/diassociateDevice.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> diassociateDevice(HttpServletRequest request, @RequestParam("userId") Long userId,
			@RequestParam("deviceId") Long deviceId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();
		// get device data
		// get user data
		User user = userService.findById(userId);
		MFDevice device = deviceService.findById(deviceId);
		if (user == null || device == null) {
			response.setSuccess(false);
			// this message is not i18n since it is a situation that shouldn't
			// happen
			response.setMessage("The user or device doesn't exists");
			return response;
		}

		MFDeviceInfo deviceInfo = device.getDeviceInfo();
		response.setTitle(i18n.getMessage("admin.cruds.user.diassociate.title"));
		response.setMessage(i18n.getMessage("admin.cruds.user.diassociate.message", deviceInfo.getBrand() + ","
				+ deviceInfo.getModel(), user.getLastName()));
		deviceService.disassociateDevice(userId, deviceId);
		response.setSuccess(true);
		return response;
	}

	// FIXME #738
	// A forged Request may allow a logged user to modify Users that don't
	// belong to the application
	// Any user will be able to modify another user
	@RequestMapping(value = "/cruds/users/save.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> saveUser(HttpServletRequest request, @RequestBody UserSaveRequest u) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		User currentUser = getUser(request);
		Application app = mgr.getApplication();
		JsonResponse<String> response = new JsonResponse<String>();
		try {
			// It may happen that initAppWithOwner
			// 1. the user is not in the system (save new user, send activation
			// mail)
			// 2. the user is in the system but not in this app (send invitation
			// mail)
			// 3. the user is in the system and in the app (edit the user?)
			User user = getUser(u.userId, u.firstName, u.lastName, u.mail, u.password, true);
			boolean userExistsInApp = userService.isMember(app.getId(), user);
			if (userExistsInApp) {
				// if the user "exists" in the App, we edit the User
				// (3)
				userService.updateUser(currentUser, user, app);
			} else {
				// if the user doesn't "exist" in the App, we invite the User
				// it may be (1) or (2)
				userService.inviteUserToApp(currentUser, app, user, u.add);
			}
			response.setObj(userService.findByMail(app, u.mail).getId().toString());
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("admin.cruds.user.saved.title"));
			response.setMessage(i18n.getMessage("admin.cruds.user.saved.message", u.mail));
		} catch (SodepServiceOriginatedException e) {
			sodepServiceExceptionToJsonResponse(request, i18n, response, e);
		}
		return response;
	}

	@RequestMapping(value = "/cruds/users/delete.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> deleteUser(HttpServletRequest request, @RequestParam(value = "id") Long userId) {
		User currentUser = getUser(request);
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Application app = mgr.getApplication();
		// TODO check authorization
		// Dani Cricco: I wrote this comment during the refactor of
		// authorizations. We need to reconsider this authorization
		// if (!authorizationControlService.has(app, currentUser,
		// AuthorizationNames.App.APP_CRUDS_EDIT)) {
		// return noAuthJsonResponse(i18n);
		// }

		User user = userService.findById(app, userId);
		User owner = app.getOwner();
		
		String title = null, message = null;
		JsonResponse<String> response = null;
		if (owner != null && owner.getId().equals(userId)) {
			response = new JsonResponse<String>();
			response.setSuccess(false);
			title = i18n.getMessage("admin.cruds.user.owner_cant_deleted.title");
			message = i18n.getMessage("admin.cruds.user.owner_cant_deleted.message", user.getMail());
		} else {
			response = deleteUserAppAssociation(currentUser, app, user);
			title =  i18n.getMessage("admin.cruds.user.deleted.title");
			message = i18n.getMessage("admin.cruds.user.deleted.message", user.getMail());
		}

		response.setTitle(title);
		response.setMessage(message);
		return response;
	}

	// TODO Refactor, it's a copy paste of deleteUser
	@RequestMapping(value = "/cruds/users/cancelInvitation.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> cancelInvitation(HttpServletRequest request, @RequestParam(value = "id") Long userId) {
		User currentUser = getUser(request);
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Application app = mgr.getApplication();
		// TODO check authorization
		// Dani Cricco: I wrote this comment during the refactor of
		// authorizations. We need to reconsider this authorization
		// if (!authorizationControlService.has(app, currentUser,
		// AuthorizationNames.App.APP_CRUDS_EDIT)) {
		// return noAuthJsonResponse(i18n);
		// }
		User owner = app.getOwner();
		String title = null, message = null;
		JsonResponse<String> response = null;
		User user = userService.findById(app, userId);
		if (owner != null && owner.getId().equals(userId)) {
			response = new JsonResponse<String>();
			response.setSuccess(false);
			title = i18n.getMessage("web.generic.error");
			message = i18n.getMessage("web.generic.error");
		} else {
			response = deleteUserAppAssociation(currentUser, app, user);
			title = i18n.getMessage("admin.cruds.user.invitation.cancel.title");
			message = i18n.getMessage("admin.cruds.user.invitation.cancel.message", user.getMail());
		}
		response.setTitle(title);
		response.setMessage(message);
		return response;
	}

	private JsonResponse<String> deleteUserAppAssociation(User currentUser, Application app, User user) {
		JsonResponse<String> response = new JsonResponse<String>();
		boolean success = userService.deleteUserAppAssociation(currentUser, app, user);
		response.setSuccess(success);
		return response;
	}

	@RequestMapping(value = "/cruds/users/addGroups.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> addGroups(HttpServletRequest request, @RequestBody GroupsUserRequest r) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();

		User user = this.userService.addGroups(r.userId, r.groupsId);
		response.setObj(user.getId().toString());
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.user.addGroups.title"));
		response.setMessage("");

		return response;
	}

	@RequestMapping(value = "/cruds/users/removeGroups.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> removeGroups(HttpServletRequest request, @RequestBody GroupsUserRequest r) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();

		List<Long> groupsId = new ArrayList<Long>();
		for (int i = 0; i < r.groupsId.size(); i++) {
			groupsId.add(new Long(r.groupsId.get(i)));
		}

		User user = this.userService.removeGroups(new Long(r.userId), groupsId);
		response.setObj(user.getId().toString());
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.user.removeGroups.title"));
		response.setMessage("");

		return response;
	}

	@RequestMapping(value = "/cruds/users/addRoles.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> addRoles(HttpServletRequest request, @RequestBody RolesUserRequest r) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Application app = mgr.getApplication();
		JsonResponse<String> response = new JsonResponse<String>();

		List<Long> rolesId = r.getRolesId();
		authorizationControlService.assignApplicationRoleToEntity(app.getId(), rolesId, r.userId);
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.user.addRoles.title"));
		response.setMessage("");

		return response;
	}

	@RequestMapping(value = "/cruds/users/removeRoles.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> removeRoles(HttpServletRequest request, @RequestBody RolesUserRequest r) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Application app = mgr.getApplication();
		JsonResponse<String> response = new JsonResponse<String>();

		List<Long> rolesId = r.rolesId;
		authorizationControlService.deleteRolesFromEntityOnApplication(app.getId(), rolesId, r.userId);
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.user.removeRoles.title"));
		response.setMessage("");

		return response;
	}

	@RequestMapping(value = "/cruds/users/changePassword.ajax", method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse<String> changePassword(HttpServletRequest request, @RequestParam String oldPass,
			@RequestParam String newPass) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		JsonResponse<String> response = new JsonResponse<String>();
		if (newPass.length() < MIN_PASSWORD_LENGTH) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.validation.user.password"));
		} else {
			User user = mgr.getUser();
			boolean checkCredentials = authenticationService.checkCredentials(user, oldPass);
			if (checkCredentials) {
				response.setSuccess(true);
				response.setTitle(i18n.getMessage("web.home.myaccount.password.change.success.title"));
				userService.changePassword(user.getId(), newPass);
			} else {
				response.setSuccess(false);
				response.setTitle(i18n.getMessage("web.home.myaccount.password.error.old"));
			}
		}
		return response;
	}

	@RequestMapping(value = "/cruds/users/downloadCsv.ajax", method = RequestMethod.POST)
	public void downloadCSV(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false) String orderBy,
			@RequestParam(value = "sord", required = false) String order,
			@RequestParam(value = "_search", required = false, defaultValue = "") String _search,
			@RequestParam(value = "searchOper", required = false) String searchOper,
			@RequestParam(value = "searchField", required = false) String searchField,
			@RequestParam(value = "searchString", required = false) String searchString) {
		SessionManager manager = new SessionManager(request);
		Application application = manager.getApplication();
		I18nManager i18n = manager.getI18nManager();
		PagedData<List<UserDTODevices>> usersPaged = read(request, page, rows, orderBy, order, _search, null, searchOper, searchField, searchString);
		List<UserDTODevices> users = usersPaged.getData();
		download(response, application, i18n, users);
	}
	
	private void download(HttpServletResponse response,
			Application application, I18nManager i18n,
			List<UserDTODevices> users) {
		String fileName = null;
		response.setContentType("text/csv");
		response.setHeader("Content-Description:", "File Transfer");
		fileName = "usuarios_" + application.getName() + ".csv";
		fileName = StringUtils.replace(fileName, " ", "_");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		List<String> headers = getHeadersByI18NValues(i18n);
		sendCsvToBrowser(response, i18n, headers, users);
	}

	private List<String> getHeadersByI18NValues(I18nManager i18n) {
		List<String> csvHeaders = new ArrayList<String>();
		String[] columnNames = {"mail", "first_name", "last_name", "devices", "last_login"};
		for (int i=0; i < columnNames.length; i++) {
			String i18NColumnName = i18n.getMessage(getI18NKeyByColumnName(columnNames[i]));
			csvHeaders.add(i18NColumnName);
		}
		return csvHeaders;
	}

	private String getI18NKeyByColumnName(String columnName) {
		if (columnName.equals("mail")) {
			return "admin.cruds.user.cols.mail";
		} else if (columnName.equals("first_name")) {
			return "admin.cruds.user.cols.firstName";
		} else if (columnName.equals("last_name")) {
			return "admin.cruds.user.cols.lastName";
		} else if (columnName.equals("devices")) {
			return "admin.cruds.user.cols.devices";
		} else if (columnName.equals("last_login")) {
			return "admin.cruds.user.cols.lastLogin";
		}
		return "";
	}
	
	private void sendCsvToBrowser(HttpServletResponse response,
			I18nManager i18n, List<String> headers, List<UserDTODevices> users) {
		try {
            CSVFormat csvFileFormat = CSVFormat.EXCEL;
            PrintWriter out = response.getWriter();
            CSVPrinter csvPrinter = new CSVPrinter(out, csvFileFormat);
            // Print the header of the CSV
            csvPrinter.printRecord(headers);
            
            // Print the data of the CSV
            loadLastLoginInfo(users);
            
            for (UserDTODevices user : users) {
            	//Map<String, String> userMap = userDTODevicesToMap(user);
                writeCsvRow(csvPrinter, user);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	private void loadLastLoginInfo(List<UserDTODevices> users) {
		Long[] userIds = new Long[users.size()];
		int i = 0;
		for (UserDTODevices user : users) {
			userIds[i] = user.getId();
			i++;
		}
		queryUserLastLogins(userIds, users);
	}

	private void queryUserLastLogins(Long[] userIds,
			List<UserDTODevices> users) {
		Map<Long, String> userLastLogins = userService.getLastLogins(users.get(0).getApplicationId(), userIds, users.size());
		for (UserDTODevices user : users) {
			String lastLogin = userLastLogins.get(user.getId()) != null ? userLastLogins.get(user.getId()) : "";
			user.setLastLogin(lastLogin);
		}
	}

	private Map<String, String> userDTODevicesToMap(UserDTODevices user) {
		Map<String, String> userMap = new HashMap<String, String>();
		String mail = user.getMail();
		userMap.put("mail", mail);
		userMap.put("firstName", user.getFirstName());
		userMap.put("lastName", user.getLastName());
		Integer devices = new Integer(user.getDevices().size());
		userMap.put("devices", devices.toString());
		String lastLogin = userService.getLastLogin(user.getApplicationId(), mail);
		userMap.put("lastLogin", lastLogin);
		
		return userMap;
	}

	private void writeCsvRow(CSVPrinter csvPrinter, UserDTODevices user) throws IOException {
		List<String> list = new ArrayList<String>();
		list.add(user.getMail());
		list.add(user.getFirstName());
		list.add(user.getLastName());
		Integer devices = new Integer(user.getDevices().size());
		list.add(devices.toString());
		list.add(user.getLastLogin());
		csvPrinter.printRecord(list);
	}

	public static class RolesUserRequest {

		private Long userId;

		private List<Long> rolesId;

		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		@JsonDeserialize(as = List.class, contentAs = Long.class)
		public List<Long> getRolesId() {
			return rolesId;
		}

		public void setRolesId(List<Long> rolesId) {
			this.rolesId = rolesId;
		}
	}

	public static class GroupsUserRequest {

		private Long userId;

		private List<Long> groupsId;

		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		@JsonDeserialize(as = List.class, contentAs = Long.class)
		public List<Long> getGroupsId() {
			return groupsId;
		}

		public void setGroupsId(List<Long> groupsId) {
			this.groupsId = groupsId;
		}
	}

	public static class UserSaveRequest {

		private String userId;

		private String lastName;

		private String firstName;

		private String mail;

		private String password;
		
		private Boolean add;

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getMail() {
			return mail;
		}

		public void setMail(String mail) {
			this.mail = mail;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public Boolean getAdd() {
			return add;
		}

		public void setAdd(Boolean add) {
			this.add = add;
		}
	}

}
