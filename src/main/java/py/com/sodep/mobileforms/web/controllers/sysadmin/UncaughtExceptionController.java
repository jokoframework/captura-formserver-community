package py.com.sodep.mobileforms.web.controllers.sysadmin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.entities.log.UncaughtException;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.sysadmin.IUncaughtExceptionService;
import py.com.sodep.mobileforms.web.json.JsonResponse;

@Controller
public class UncaughtExceptionController  {

	private static Logger logger = LoggerFactory.getLogger(UncaughtExceptionController.class);

	@Autowired
	private IUncaughtExceptionService service;

	@RequestMapping("/admin/uncaughtException/paging/read.ajax")
	public @ResponseBody
	PagedData<List<UncaughtException>> read(HttpServletRequest request,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false) String orderBy,
			@RequestParam(value = "sord", required = false) String order) {

		logger.trace("Loading unexpected exception report");
		boolean ascending = true;
		if (order != null) {
			ascending = order.equalsIgnoreCase("asc");
		}

		PagedData<List<UncaughtException>> response = service.findAll(orderBy, ascending, page, rows);

		return response;
	}

	@RequestMapping("/admin/uncaughtException/clean.ajax")
	public @ResponseBody
	JsonResponse<String> clean(HttpServletRequest request) {
		service.clean();
		JsonResponse<String> response = new JsonResponse<String>();
		response.setTitle("Unexpected exceptions cleaned up");
		response.setMessage("");
		response.setSuccess(true);
		return response;
	}
}
