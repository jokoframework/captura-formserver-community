package py.com.sodep.mobileforms.impl.services.scripting;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.scripting.SodepScript;
import py.com.sodep.mobileforms.api.services.scripting.IScriptingService;

@Service("ScriptingService")
@Transactional
class ScriptingService implements IScriptingService, ApplicationContextAware {

	private final String UTF8 = "UTF-8";

	private ApplicationContext context;

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	@Override
	public String executeScript(User user, String script) {
		return executeScript(user, script, null);
	}

	@Override
	public String executeScript(User user, String script, Map<String, ? extends Object> params) {
		ByteArrayOutputStream bos = null;
		PrintStream out = null;
		String retValue = null;
		try {
			bos = new ByteArrayOutputStream();
			out = new PrintStream(bos, true, UTF8);

			Binding binding = new Binding();
			if (params != null) {
				binding.setVariable("params", params);
			}

			binding.setVariable("out", out);
			binding.setVariable("context", context);
			binding.setVariable("em", em);

			GroovyShell shell = new GroovyShell(binding);
			shell.evaluate(script);

			retValue = new String(bos.toByteArray(), UTF8);
		} catch (Exception t) {
			retValue = handleThrowable(bos, out, t);
		} finally {
			if (out != null) {
				out.close();
			}
		}
		return retValue;
	}

	private String handleThrowable(ByteArrayOutputStream bos, PrintStream out, Throwable t) {
		String retValue;
		if (out != null) {
			out.println(t.getMessage());
			out.println();
			
			if (bos != null) {
				try {
					retValue = new String(bos.toByteArray(), UTF8);
				} catch (UnsupportedEncodingException e) {
					retValue = e.getMessage();
				}
			} else {
				retValue = t.getMessage();
			}
		} else {
			retValue = t.getMessage();
		}
		return retValue;
	}

	@Override
	public SodepScript saveScript(User user, String script, String name) {
		SodepScript sodepScript = new SodepScript();
		sodepScript.setName(name);
		sodepScript.setScript(script);
		sodepScript.setUser(user);
		em.persist(sodepScript);
		return sodepScript;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SodepScript> listScripts(User user) {
		Query q = em.createQuery(" FROM " + SodepScript.class.getSimpleName() + " s ORDER BY s.id");
		return q.getResultList();
	}

	@Override
	public SodepScript getScript(User user, Long id) {
		SodepScript s = em.find(SodepScript.class, id);
		return s;
	}

	@Override
	public SodepScript getScript(User user, String name) {
		Query q = em.createQuery("FROM " + SodepScript.class.getSimpleName() + " s where s.name=:name");
		q.setParameter("name", name);
		return (SodepScript) q.getSingleResult();
	}

}
