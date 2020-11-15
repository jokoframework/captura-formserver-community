package py.com.sodep.mobileforms.api.services.config;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.SystemParameterDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.entities.sys.SystemParameter;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

//TODO consider adding a method to unserialize parameters stored as JSON objects
public interface IParametersService {

	public IParameter getParameter(Long id);

	public List<IParameter> listParameters();

	public PagedData<List<SystemParameter>> findAll(String orderBy, boolean ascending, Integer page,
			Integer rows);

	public PagedData<List<SystemParameter>> findByProperty(String searchField, String searchOper,
			Long val, String orderBy, boolean ascending, Integer page, Integer rows);

	public PagedData<List<SystemParameter>> findByProperty(String searchField, String searchOper,
			String searchString, String orderBy, boolean ascending, Integer page, Integer rows);

	public void createSystemParameter(User currentUser, SystemParameterDTO dto);

	  void editSystemParameter(User currentUser, SystemParameterDTO dto);

	public SystemParameter findById(Long id);

	public SystemParameter logicalDelete(SystemParameter parameter);

	public SystemParameterDTO getSystemParameter(Long parameterId);

}
