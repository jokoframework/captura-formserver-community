package py.com.sodep.mobileforms.web.controllers.device;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mf.exchange.objects.device.MFDevice;
import py.com.sodep.mf.exchange.objects.device.MFDeviceInfo;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.core.IDeviceService;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class DeviceController extends SodepController {

	@Autowired
	private IDeviceService deviceService;

	@RequestMapping("/devices/blacklist.ajax")
	public @ResponseBody
	PagedData<List<MFDeviceInfo>> blacklist(HttpServletRequest request,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer pageSize,
			@RequestParam(value = "sidx", required = false, defaultValue = SodepEntity.ID) String orderBy,
			@RequestParam(value = "sord", required = false, defaultValue = "asc") String order) {
		SessionManager sm = new SessionManager(request);
		Application app = sm.getApplication();

		PagedData<List<MFDeviceInfo>> listBlacklistedDevices = deviceService.listBlacklistedDevices(app, orderBy,
				"asc".equals(order), pageNumber, pageSize);

		return listBlacklistedDevices;
	}

	@RequestMapping(value = "/devices/addToBlacklist.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> addToBlacklist(HttpServletRequest request, @RequestParam("deviceId") Long deviceId) {
		return addOrRemove(request, deviceId, false);
	}

	@RequestMapping(value = "/devices/removeFromBlacklist.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> removeFromBlacklist(HttpServletRequest request, @RequestParam("deviceId") Long deviceId) {
		return addOrRemove(request, deviceId, true);
	}

	JsonResponse<String> addOrRemove(HttpServletRequest request, Long deviceId, Boolean remove) {
		SessionManager mgr = new SessionManager(request);
		JsonResponse<String> response = new JsonResponse<String>();
		// get device data
		// get user data
		MFDevice device = deviceService.findById(deviceId);
		if (device == null) {
			response.setSuccess(false);
			// this message is not i18n since it is a situation that shouldn't
			// happen
			response.setMessage("The device doesn't exists"); // FIXME i18n
			return response;
		}
		I18nManager i18n = mgr.getI18nManager();
		boolean result;
		String title;
		String message;
		// FIXME i18n
		if (remove) {
			result = deviceService.removeFromBlacklist(mgr.getApplication(), deviceId);
			if (result) {
				title = i18n.getMessage("web.home.admin.devices.removeFromBlacklist.success.title");
				message = i18n.getMessage("web.home.admin.devices.removeFromBlacklist.success.msg");
			} else {
				title = i18n.getMessage("web.home.admin.devices.removeFromBlacklist.fail.title");
				message = i18n.getMessage("web.home.admin.devices.removeFromBlacklist.fail.msg");
			}
		} else {
			result = deviceService.addToBlacklist(mgr.getApplication(), deviceId);
			if (result) {
				title = i18n.getMessage("web.home.admin.devices.addToBlacklist.success.title");
				message = i18n.getMessage("web.home.admin.devices.addToBlacklist.success.msg");
			} else {
				title = i18n.getMessage("web.home.admin.devices.addToBlacklist.fail.title");
				message = i18n.getMessage("web.home.admin.devices.addToBlacklist.fail.msg");
			}
		}
		response.setSuccess(result);
		response.setTitle(title);
		response.setMessage(message);

		return response;
	}

}
