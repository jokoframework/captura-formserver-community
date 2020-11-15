package py.com.sodep.mobileforms.impl.services.ui.multiselect;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import py.com.sodep.mobileforms.api.entities.ui.multiselect.MultiselectConf;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.ui.multiselect.IMultiselectManager;
import py.com.sodep.mobileforms.api.services.ui.multiselect.IMultiselectService;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectActionRequest;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectReadRequest;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectServiceResponse;

@Service("MultiselectManager")
@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
public class MultiselectManager implements IMultiselectManager, ApplicationContextAware {
	
	private static Logger logger = LoggerFactory.getLogger(MultiselectManager.class);

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;

	private ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	private IMultiselectService lookupService(String id) {
		MultiselectConf conf = em.find(MultiselectConf.class, id);
	
		try {
			if (conf != null) {
				String serviceName = conf.getServiceName();

				IMultiselectService service = (IMultiselectService) context.getBean(serviceName);
				return service;
			}
		} catch (NoSuchBeanDefinitionException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public MultiselectServiceResponse listItems(String id, MultiselectReadRequest request) {
		IMultiselectService service = lookupService(id);
		return service.listItems(request);
	}

	@Override
	public MultiselectServiceResponse doAction(String id, MultiselectActionRequest request) {
		IMultiselectService service = lookupService(id);
		return service.doAction(request);
	}

	@Override
	public MultiselectModel loadModel(String id, String language,Map<String,String> params) {
		IMultiselectService service = lookupService(id);
		if (service != null) {
			return service.loadModel(language,params);
		}
		return null;
	}

}
