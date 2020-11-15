package py.com.sodep.mobileforms.impl.services.metadata.core;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.GroupDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.DuplicateEntityException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.Authorizable.CHECK_TYPE;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.core.IGroupService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.impl.services.metadata.AppAwareBaseService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.PagingCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.PropertyCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.SortCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.groups.FindGroupCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.groups.FindGroupCriteriaBuilder;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.groups.UserCriteria;

@Service("GroupService")
@Transactional
public class GroupService extends AppAwareBaseService<Group> implements IGroupService {

	@Autowired
	private IUserService userService;
	
	// To solve #1449, but I think this might be wrong. There's a cicular dependency between GroupService and AuthorizationControlService
	@Autowired
	private IAuthorizationControlService authorizationControlService;

	protected GroupService() {
		super(Group.class);
	}

	private String fieldNameWithPrefix(final String field) {
		String f = "g.id";
		if (field.matches("name|description")) {
			f = "g." + field;
		}
		return f;
	}

	@Override
	public Group save(User user, Group group) {
		validateGroupUniqueness(group);
		return super.save(user, group);
	}

	private void validateGroupUniqueness(Group group) {
		String queryStr = "SELECT COUNT(g) FROM " + Group.class.getSimpleName() + " g"
				+ " WHERE g.deleted = false AND g.application = :app AND g.name = :name";

		Long id = group.getId();
		if (id != null) {
			queryStr += " AND g.id != :gid";
		}

		Query query = em.createQuery(queryStr);
		query.setParameter("app", group.getApplication());
		query.setParameter("name", group.getName());
		if (id != null) {
			query.setParameter("gid", group.getId());
		}

		Long singleResult = (Long) query.getSingleResult();

		if (singleResult.longValue() > 0) {
			DuplicateEntityException ex = new DuplicateEntityException("admin.cruds.group.invalid.duplicated");
			ex.addMessage("form", "admin.cruds.group.invalid.duplicated");
			throw ex;
		}

	}

	private PagedData<List<Group>> find(Application app, FindGroupCriteria criteria) {
		String propertyWhereClause = "";
		String propertyValue = null;

		if (criteria.getPropertyCriteria() != null) {
			PropertyCriteria property = criteria.getPropertyCriteria();
			propertyWhereClause = "AND "
					+ whereClause(fieldNameWithPrefix(property.getName()), property.getOper(), property.getValue());
			String oper = property.getOper();
			if (oper.equals(OPER_CONTAINS) || oper.equals(OPER_NOT_CONTAINS)) {
				propertyValue = "%" + property.getValue() + "%";
			}
		}

		String orderBy = "id";
		boolean ascending = true;
		SortCriteria c = criteria.getSortCriteria();
		if (c != null) {
			orderBy = c.getOrderBy();
			ascending = c.isAscending();
		}

		User user = null;
		String userWhereClause = "";
		String userJoin = "";
		if (criteria.getUserCriteria() != null) {
			UserCriteria userCriteria = criteria.getUserCriteria();
			user = userCriteria.getUser();
			boolean isMember = userCriteria.isMember();
			if (isMember) {
				userJoin = "JOIN g.users u";
				userWhereClause = "AND u.deleted = false AND u = :user ";
			} else {
				userWhereClause = "AND :user NOT IN elements(g.users) ";
			}
		}

		PagingCriteria pagingCriteria = criteria.getPagingCriteria();
		int pageNumber = pagingCriteria.getPageNumber();
		int pageSize = pagingCriteria.getPageSize();

		String selectQueryStr = "SELECT DISTINCT(g) FROM Group g JOIN g.application app " + userJoin
				+ " WHERE g.deleted = false AND app = :app " + userWhereClause + propertyWhereClause
				+ " ORDER BY $1 $2 ";
		selectQueryStr = selectQueryStr.replace("$1", fieldNameWithPrefix(orderBy));
		selectQueryStr = selectQueryStr.replace("$2", ascending ? " ASC " : " DESC ");
		TypedQuery<Group> query = em.createQuery(selectQueryStr, Group.class);
		setFindQueryParameters(query, app, propertyValue, user, pageNumber, pageSize);
		query.setMaxResults(pageSize);
		query.setFirstResult((pageNumber - 1) * pageSize);
		List<Group> data = query.getResultList();

		String countQueryStr = "SELECT COUNT(DISTINCT g) FROM Group g JOIN g.application app " + userJoin
				+ " WHERE g.deleted = false AND app = :app " + userWhereClause + propertyWhereClause;
		Query countQuery = em.createQuery(countQueryStr);
		setFindQueryParameters(countQuery, app, propertyValue, user, pageNumber, pageSize);
		
		Long count = (Long) countQuery.getSingleResult();

		return new PagedData<List<Group>>(data, count, pageNumber, pageSize, data.size());
	}

	private void setFindQueryParameters(Query query, Application app, String propertyValue, User user, int pageNumber,
			int pageSize) {
		query.setParameter("app", app);
		if (propertyValue != null) {
			query.setParameter("value", propertyValue);
		}
		if (user != null) {
			query.setParameter("user", user);
		}
	}

	@Override
	@Authorizable(checkType = CHECK_TYPE.NONE)
	public GroupDTO getGroup(Long groupId) {
		Group g = this.findById(groupId);
		GroupDTO gdto = new GroupDTO();
		gdto.setId(g.getId());
		gdto.setName(g.getName());
		gdto.setDescription(g.getDescription());
		gdto.setActive(g.getActive());
		return gdto;
	}

	@Override
	public Group addUsers(Long groupId, List<Long> usersId) {
		Group group = this.findById(groupId);
		for (Long userId : usersId) {
			User user = this.userService.findById(userId);
			group.getUsers().add(user);
			user.getGroups().add(group);
			// #1449
			authorizationControlService.computeUserAccess(user);
		}
		return group;
	}

	@Override
	public Group removeUsers(Long groupId, List<Long> usersId) {
		Group group = this.findById(groupId);
		for (Long userId : usersId) {
			User user = this.userService.findById(userId);
			group.getUsers().remove(user);
			user.getGroups().remove(group);
			// #1449
			authorizationControlService.computeUserAccess(user);
		}
		return group;
	}

	@Override
	@Authorizable(checkType = CHECK_TYPE.NONE)
	public Group findById(Long groupId) {
		return super.findById(groupId);
	}

	@Override
	@Authorizable(AuthorizationNames.App.GROUP_LIST)
	public Group findById(Application app, Long groupId) {
		return super.findById(app, groupId);
	}

	@Override
	@Authorizable(AuthorizationNames.App.GROUP_EDIT)
	public Group logicalDelete(Group group) {
		return super.logicalDelete(group);
	}

	@Override
	@Authorizable(AuthorizationNames.App.GROUP_LIST)
	public PagedData<List<Group>> findByMemberAndName(Application app, User user, boolean isMember, String name,
			int pageNumber, int pageSize) {
		FindGroupCriteriaBuilder builder = new FindGroupCriteriaBuilder();
		builder.property(Group.NAME, BaseService.OPER_CONTAINS, name);
		builder.pageNumber(pageNumber).pageSize(pageSize).asc(Group.NAME);
		if (user != null && isMember) {
			builder.isMember(user);
		} else if (user != null) {
			builder.notAMember(user);
		}
		return find(app, builder.newInstance());
	}

	@Override
	@Authorizable(AuthorizationNames.App.GROUP_LIST)
	public PagedData<List<Group>> findAll(Application app, String orderBy, boolean ascending, int pageNumber,
			int pageSize) {
		FindGroupCriteriaBuilder builder = new FindGroupCriteriaBuilder();
		builder.pageNumber(pageNumber).pageSize(pageSize);
		if (ascending) {
			builder.asc(orderBy);
		} else {
			builder.desc(orderBy);
		}

		return find(app, builder.newInstance());
	}

	@Override
	@Authorizable(AuthorizationNames.App.GROUP_LIST)
	public PagedData<List<Group>> findByProperty(Application app, String propertyName, String oper, Object value,
			String orderBy, boolean ascending, int pageNumber, int pageSize) {
		FindGroupCriteriaBuilder builder = new FindGroupCriteriaBuilder();
		builder.property(Group.NAME, oper, value);
		builder.pageNumber(pageNumber).pageSize(pageSize);
		if (ascending) {
			builder.asc(orderBy);
		} else {
			builder.desc(orderBy);
		}

		return find(app, builder.newInstance());
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	// (Dani Cricco): This method shouldn't be invoked directly by the web interface. It is
	// meant for use initially on the AuthorizationControlServie
	public List<User> listUsers(Group group) {
		TypedQuery<User> q = em.createQuery("SELECT u from " + Group.class.getName() + " g JOIN g.users u"
				+ " WHERE g.deleted = false AND u.deleted = false AND g = :group", User.class);
		q.setParameter("group", group);
		List<User> users = q.getResultList();
		return users;

	}

}
