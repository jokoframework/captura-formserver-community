package py.com.sodep.mobileforms.web.controllers.cruds;

import java.util.ArrayList;
import java.util.List;

import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.core.Role;

/**
 * /** This class has some handy method to translate Role objects from the
 * domain entities to the DTOs used in JS
 * 
 * @author Miguel
 * 
 */
public class RoleViewUtils {

	public static List<RoleDTO> translate(List<Role> roles) {
		ArrayList<RoleDTO> dtos = new ArrayList<RoleDTO>();
		for (Role r : roles) {
			RoleDTO dto = translate(r);
			dtos.add(dto);
		}
		return dtos;
	}

	private static RoleDTO translate(Role r) {
		RoleDTO dto = new RoleDTO();
		dto.setActive(r.getActive());
		dto.setApplicationId(r.getApplication().getId());
		dto.setDescription(r.getDescription());
		// dto.setGrants(null); FIXME
		dto.setId(r.getId());
		// dto.setLevel(r.getAuthLevel()); FIXME
		dto.setName(r.getName());
		return dto;
	}
}
