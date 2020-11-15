package py.com.sodep.mobileforms.web.controllers.cruds;

import java.util.ArrayList;
import java.util.List;

import py.com.sodep.mobileforms.api.dtos.GroupDTO;
import py.com.sodep.mobileforms.api.entities.core.Group;

public class GroupViewUtils {

	public static List<GroupDTO> translate(List<Group> groups) {
		ArrayList<GroupDTO> dtos = new ArrayList<GroupDTO>();
		for (Group g : groups) {
			GroupDTO dto = translate(g);
			dtos.add(dto);
		}
		return dtos;
	}

	public static GroupDTO translate(Group g) {
		GroupDTO dto = new GroupDTO();
		dto.setActive(g.getActive());
		dto.setApplicationId(g.getApplication().getId());
		dto.setDescription(g.getDescription());
		dto.setName(g.getName());
		dto.setId(g.getId());
		return dto;
	}
}
