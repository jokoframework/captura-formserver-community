package py.com.sodep.mobileforms.api.services.metadata.core;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.GroupDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

/**
 * To save, update, retrieve and search Groups
 * 
 * @author Miguel
 * 
 */
public interface IGroupService {

	GroupDTO getGroup(Long groupId);

	Group addUsers(Long groupId, List<Long> usersId);

	Group removeUsers(Long groupId, List<Long> usersId);

	Group findById(Long groupId);

	Group findById(Application app, Long groupId);

	Group save(User user, Group group);

	Group logicalDelete(Group group);

	/**
	 * Returns the groups - that belong to the application - which the user is
	 * member of, if isMember is true, or the groups which the user isn't part
	 * of if isMember is false.
	 * 
	 * The groups are filtered by name if name is not null. The filter applied
	 * is like %name%
	 * 
	 * @param app
	 *            The application in which to search
	 * @param user
	 *            The user that has to belong or not to the group. If null then
	 *            the groups are not filtered by membership of any user
	 * @param isMember
	 *            Indicates wheter the user must be a member or not of the
	 *            groups. If user is null, it has no effect.
	 * @param name
	 *            To filter the name by "like %name%"
	 * @param pageNumber
	 *            Start at 1
	 * @param pageSize
	 *            The size of the page, i.e. the maximum number of Groups in the
	 *            page
	 * @return
	 */
	PagedData<List<Group>> findByMemberAndName(Application app, User user, boolean isMember, String name,
			int pageNumber, int pageSize);

	PagedData<List<Group>> findAll(Application app, String orderBy, boolean ascending, int pageNumber, int pageSize);

	PagedData<List<Group>> findByProperty(Application app, String propertyName, String oper, Object value,
			String orderBy, boolean ascending, int pageNumber, int pageSize);

	/**
	 * Lists the members of the group
	 * 
	 * @param group
	 * @return
	 */
	List<User> listUsers(Group group);

}
