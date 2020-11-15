package py.com.sodep.mobileforms.impl.services.config;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters.PARAMETER_TYPE;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.config.IParametersService;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;

// TODO consider extending this For application parameters
@Service("SystemParametersBundle")
@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
class SystemParametersBundle implements ISystemParametersBundle {

	private static final Logger logger = LoggerFactory.getLogger(SystemParametersBundle.class);

	@Autowired
	private IParametersService parametersService;

	private Object lockReload = new Object();
	// We use a ConcurrentHashMap to have a lock free access to the parameters
	private final ConcurrentHashMap<Long, IParameter> parameters = new ConcurrentHashMap<Long, IParameter>();

	@PostConstruct
	public void init() {
		reload();
	}

	private static IParameter findParameter(List<IParameter> param, long id) {
		for (IParameter iParameter : param) {
			if (iParameter.getParameterId() == id) {
				return iParameter;
			}
		}
		return null;
	}

	public void reload() {
		synchronized (lockReload) {
			logger.debug("Loading System Parameters");
			// Although we are using a ConcurrentHashMap we use a synchronize
			// reload to avoid two "reloads" happening at the same time.
			// Reloading parameters in runtime is something that will happen
			// very seldom, so this is not a problem
			List<IParameter> params = parametersService.listParameters();

			// Clean parameters that are not present
			// We don't make a full clean of the map because this might affect
			// other thread that is reading a parameter.
			// However, we need to clean the parameters because the
			// administrator might have decided to remove the overloading from
			// the DB and the system should use the default parameter
			Enumeration<Long> loadedKeys = parameters.keys();
			ArrayList<Long> keysToDelete = new ArrayList<Long>();
			while (loadedKeys.hasMoreElements()) {
				Long paramId = loadedKeys.nextElement();
				IParameter param = findParameter(params, paramId);
				if (param == null) {
					keysToDelete.add(paramId);
				}
			}
			for (Long paramId : keysToDelete) {
				logger.debug("Removing parameter " + paramId);
				parameters.remove(paramId);
			}
			for (IParameter p : params) {
				logger.debug("Adding parameter #" + p.getParameterId() + " - " + p.getValue());
				// The idea is to fast fail if a parameter is wrongly set and
				// possible prevent side effects.
				checkParameterType(p);
				parameters.put(p.getParameterId(), p);

			}
			logger.info("System Parameters Loaded");
		}
	}

	/**
	 * This method checks that the value of the parameter can be casted to the
	 * specified type. If this is not possible an {@link IllegalStateException}
	 * will be thrown.
	 * 
	 * @param p
	 */
	private void checkParameterType(IParameter p) {
		String value = p.getValue();
		if (p.getType().equals(PARAMETER_TYPE.INTEGER)) {
			try {
				Integer.valueOf(value);
			} catch (NumberFormatException e) {
				throw new IllegalStateException("The system parameter  #" + p.getParameterId() + " was declared as "
						+ p.getType() + " but the value was \"" + p.getValue() + "\"");
			}
		} else if (p.getType().equals(PARAMETER_TYPE.BOOLEAN)) {
			if (!value.equals("true") && !value.equals("false")) {
				throw new IllegalStateException("The system parameter  #" + p.getParameterId() + " was declared as "
						+ p.getType() + " but the value was \"" + p.getValue()
						+ "\". It should be either \"true\" or \"false\" ");
			}
		} else if (p.getType().equals(PARAMETER_TYPE.LONG)) {
			try {
				Integer.valueOf(value);
			} catch (NumberFormatException e) {
				throw new IllegalStateException("The system parameter  #" + p.getParameterId() + " was declared as "
						+ p.getType() + " but the value was \"" + p.getValue() + "\"");
			}
		} else if (p.getType().equals(PARAMETER_TYPE.STRING)) {
			// do nothing, a string is always value
		} else if (p.getType().equals(PARAMETER_TYPE.LIST)) {
			throw new IllegalStateException("The system parameter #" + p.getParameterId()
					+ " was declared of type List but it is currently not supported");
		}
	}

	@Override
	public IParameter getParameter(Long id) {

		return parameters.get(id);
	}

	@Override
	public Integer getIntValue(Long id) {
		IParameter param = parameters.get(id);
		if (param != null) {
			String strValue = param.getValue();
			return new Integer(strValue);
		}
		return null;
	}

	@Override
	public String getStrValue(Long id) {
		IParameter param = parameters.get(id);
		if (param != null) {
			return param.getValue();
		}
		return null;
	}

	@Override
	public Long getLongValue(Long id) {
		IParameter param = parameters.get(id);
		if (param != null) {
			String strValue = param.getValue();
			return new Long(strValue);
		}
		return null;
	}

	@Override
	public List<String> getListValues(Long id) {
		throw new RuntimeException("NOT YET IMPLEMENTED");
	}

	@Override
	public Boolean getBoolean(Long id) {
		IParameter param = parameters.get(id);
		if (param != null) {
			Boolean bool = new Boolean(param.getValue());
			return bool;
		}
		return null;
	}

}
