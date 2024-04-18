package py.com.sodep.mobileforms.impl.services.config;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import py.com.sodep.mobileforms.api.dtos.SystemParameterDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.entities.sys.SystemParameter;
import py.com.sodep.mobileforms.api.services.config.IParametersService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;

@Service("ParametersService")
class ParametersServiceImpl extends BaseService<SystemParameter> implements IParametersService {

	public ParametersServiceImpl() {
		super(SystemParameter.class);
	}

	protected ParametersServiceImpl(Class<SystemParameter> clazz) {
		super(clazz);
	}

	private static Logger logger = LoggerFactory.getLogger(ParametersServiceImpl.class);

	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	@Override
	public IParameter getParameter(Long id) {
		SystemParameter p = em.find(SystemParameter.class, id);
		if (p == null || p.getDeleted()) {
			logger.warn("Unknown System Parameter id=" + id);
			return null;
		}
		return p;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IParameter> listParameters() {
		Query q = em.createQuery("FROM " + SystemParameter.class.getName());
		return new ArrayList<IParameter>(q.getResultList());
	}

	@Override
	public PagedData<List<SystemParameter>> findAll(String orderBy, boolean ascending,
			Integer pageNumber, Integer pageSize) {
		return super.findAll(orderBy, ascending, pageNumber, pageSize);
	}

	@Override
	public PagedData<List<SystemParameter>> findByProperty(String searchField, String searchOper,
			Long val, String orderBy, boolean ascending, Integer page, Integer rows) {
		return super.findByProperty(searchField, searchOper, val, orderBy, ascending, page, rows);
	}

	@Override
	public PagedData<List<SystemParameter>> findByProperty(String searchField, String searchOper,
			String searchString, String orderBy, boolean ascending, Integer page, Integer rows) {
		return super.findByProperty(searchField, searchOper, searchString, orderBy, ascending, page, rows);
	}

	@Override
	public void createSystemParameter(User currentUser, SystemParameterDTO dto) {
		logger.debug(" dto: " + dto);
		SystemParameter sysParamEntity = new SystemParameter();
		synchFromDTO(dto, sysParamEntity);
		em.persist(sysParamEntity);
	}

	private void synchFromDTO(SystemParameterDTO dto, SystemParameter sysParamEntity) {
		try {
			ConvertUtils.register(new SqlTimestampConverter(null), Timestamp.class);      
			BeanUtils.copyProperties(sysParamEntity, dto);
		} catch (IllegalAccessException e) {
			logger.error("", e);
		} catch (InvocationTargetException e) {
			logger.error("", e);
		}
	}

	@Override
	public void editSystemParameter(User currentUser, SystemParameterDTO dto) {
		logger.debug(" dto: " + dto);
		SystemParameter sysParamEntity = new SystemParameter();
		synchFromDTO(dto, sysParamEntity);
		em.merge(sysParamEntity);
	}

	@Override
	public SystemParameter findById(Long id) {
		logger.debug(" id: " + id);
		return em.find(SystemParameter.class, id);
	}

	@Override
	public SystemParameterDTO getSystemParameter(Long parameterId) {
		logger.debug("id: " + parameterId);
		SystemParameter entity = findById(parameterId);
		SystemParameterDTO dto = new SystemParameterDTO();
		try {
			ConvertUtils.register(new SqlTimestampConverter(null), Timestamp.class);      
			BeanUtils.copyProperties(dto, entity);
		} catch (IllegalAccessException e) {
			logger.error("", e);
		} catch (InvocationTargetException e) {
			logger.error("", e);
		}
		return dto;
	}

	@Override
	public SystemParameter logicalDelete(SystemParameter role) {
		return super.logicalDelete(role);
	}

}
