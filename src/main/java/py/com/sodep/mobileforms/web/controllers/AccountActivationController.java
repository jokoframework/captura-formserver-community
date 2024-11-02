package py.com.sodep.mobileforms.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.captcha.Captcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import py.com.sodep.mf.exchange.objects.device.MFDevice;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IDeviceService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.config.ChakeConfig;
import py.com.sodep.mobileforms.web.activation.ActivationRequest;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class AccountActivationController {

	private static final Logger LOG = LoggerFactory.getLogger(AccountActivationController.class);

	@Autowired
	private IUserService userService;

	@Autowired
	private IDeviceService deviceService;

	@Autowired
	private IApplicationService applicationService;

	private ActivationRequest activationRequest;

	@Autowired
	private ChakeConfig chakeConfig;

	@RequestMapping("/account/activation.mob")
	public ModelAndView index(HttpServletRequest request, @RequestParam("device") String device) {
		ModelAndView mav = new ModelAndView("/account/activation.ftl");

		if (device != null) {
			try {
				// Decodificar el objeto base64
				String decodedJson = new String(Base64.getDecoder().decode(device), StandardCharsets.UTF_8);

				// Convertir el JSON decodificado a un objeto MFDevice
				ObjectMapper objectMapper = new ObjectMapper();
				activationRequest = objectMapper.readValue(decodedJson, ActivationRequest.class);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return mav;
	}

	@RequestMapping(value = "/account/activation.ajax", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse<String> activation(HttpServletRequest request,
			@RequestParam("captcha") String captchaStr) {
		SessionManager mgr = new SessionManager(request);
		JsonResponse<String> response = new JsonResponse<String>();
		I18nManager i18n = I18nManager.getI18n(request);

		// Captcha challenge must be the first control on the request.
		// Otherwise, a bot can detect by brute force the registered users
		Captcha captcha = mgr.getCaptcha();
		if (!captcha.isCorrect(captchaStr)) {

			response.setSuccess(false);
			response.setUnescapedMessage(i18n.getMessage("web.account.activation.error"));
			return response;
		}

		MFDevice mfDevice = activationRequest.getDevice();
		Application app = applicationService.findById(mfDevice.getApplicationId());
		User user = userService.findByMail(chakeConfig.getEmail());
		String identifier = mfDevice.getDeviceInfo().getIdentifier();

		if (deviceService.isDeviceAssociated(user, app, identifier)) {
			response.setSuccess(true);
			response.setObj("OK");
			response.setUnescapedMessage("Tu dispositivo ya está activado. Continúa usando la app para realizar tus denuncias.");
			return response;
		}

		deviceService.associate(user, activationRequest);
		response.setSuccess(true);
		response.setObj("OK");
		response.setUnescapedMessage(i18n.getMessage("web.account.activation.success"));

		return response;
	}
}
