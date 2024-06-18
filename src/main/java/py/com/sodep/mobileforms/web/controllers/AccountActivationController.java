package py.com.sodep.mobileforms.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.captcha.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import py.com.sodep.mf.exchange.objects.device.MFDevice;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.metadata.core.IDeviceService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class AccountActivationController {

	@Autowired
	private IUserService userService;

	@Autowired
	private IDeviceService deviceService;

	private MFDevice mfDevice;


	@RequestMapping("/account/activation.mob")
	public ModelAndView index(HttpServletRequest request, @RequestParam("device") String device) {
		ModelAndView mav = new ModelAndView("/account/activation.ftl");

		if (device != null) {
			try {
				// Decodificar el objeto base64
				String decodedJson = new String(Base64.getDecoder().decode(device), StandardCharsets.UTF_8);

				// Convertir el JSON decodificado a un objeto MFDevice
				ObjectMapper objectMapper = new ObjectMapper();
				mfDevice = objectMapper.readValue(decodedJson, MFDevice.class);
			} catch (IOException e) {
				// Manejo de errores
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

		User user = userService.findByMail("chake@feltesq.com");
		deviceService.associate(user, mfDevice);

		response.setSuccess(true);
		response.setObj("OK");
		response.setUnescapedMessage(i18n.getMessage("web.account.activation.success"));

		return response;
	}
}
