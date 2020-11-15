package py.com.sodep.mobileforms.web.controllers.cruds;

import java.util.ArrayList;
import java.util.List;

import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.entities.core.User;

/**
 * This class has some handy method to translate objects from the domain
 * entities to the DTOs used in JS
 * 
 * @author danicricco
 * 
 */
public class UserViewUtils {

	public static List<UserDTO> translate(List<User> users) {
		ArrayList<UserDTO> dtos = new ArrayList<UserDTO>();
		for (User u : users) {
			UserDTO dto = translate(u);
			dtos.add(dto);
		}
		return dtos;
	}

	public static UserDTO translate(User u) {
		UserDTO dto = new UserDTO();
		dto.setActive(u.getActive());
		if (u.getApplication() != null) {
			// the application can be null if the user has registered but didn't
			// activate his application yet
			dto.setApplicationId(u.getApplication().getId());
		}

		dto.setFirstName(u.getFirstName());
		dto.setLastName(u.getLastName());
		dto.setMail(u.getMail());
		dto.setId(u.getId());
		return dto;
	}
}
