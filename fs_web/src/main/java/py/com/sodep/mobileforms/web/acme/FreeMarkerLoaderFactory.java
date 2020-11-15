package py.com.sodep.mobileforms.web.acme;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

import freemarker.cache.CacheStorage;
import freemarker.cache.FileTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class FreeMarkerLoaderFactory {

	private Configuration conf = null;

	private CacheStorage cache;

	private String templatePath;
	private String defaultEncoding;

	@Autowired
	private WebApplicationContext context;

	private static class CustomConfigurator extends Configuration {

	}

	public synchronized Configuration createInstance() {
		if (conf == null) {

			conf = new CustomConfigurator();

			FileTemplateLoader t;

			conf.setServletContextForTemplateLoading(context.getServletContext(), templatePath);

			conf.setDefaultEncoding(defaultEncoding);
			conf.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

			BeansWrapper wrapper = new ACMEObjectWrapper();
			conf.setObjectWrapper(wrapper);

			if (cache != null) {
				conf.setCacheStorage(cache);
			}
			return conf;

		}

		return conf;

	}

	public CacheStorage getCache() {
		return cache;
	}

	public void setCache(CacheStorage cache) {
		this.cache = cache;
	}

	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public static void main(String[] args) throws IOException, TemplateException {
		FreeMarkerLoaderFactory factory = new FreeMarkerLoaderFactory();
		Configuration c = factory.createInstance();
		Template temp = c.getTemplate("/testCommon.ftl");

		Writer out = new OutputStreamWriter(System.out);
		Map root = new HashMap();
		temp.process(root, out);
		out.flush();
		System.out.println("worked!");
	}

}
