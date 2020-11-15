package py.com.sodep.mobileforms.api.entities.core;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.interfaces.IAppAwareEntity;

//import org.hibernate.envers.Audited;
//import org.hibernate.envers.RelationTargetAuditMode;

/**
 * A group is a list of users.
 * 
 * A given groups has a list of authorizations over the application, projects
 * and forms. These authorizations are "transferred" to all members.
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "core", name = "groups")
// @Audited
public class Group extends AuthorizableEntity implements IAppAwareEntity {

	public static final String ID = "id";
	
	public static final String NAME = "name";
	
	public static final String DESCRIPTION = "description";

	private static final long serialVersionUID = 1L;

	private String description;

	private String name;

	private Set<User> users;

	private Application application;

	public Group() {
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(nullable = false, length = 255)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// @Audited(targetAuditMode=RelationTargetAuditMode.NOT_AUDITED)
	@ManyToMany
	@JoinTable(schema = "core", name = "groups_users", joinColumns = { @JoinColumn(name = "group_id", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "user_id", nullable = false) })
	public Set<User> getUsers() {
		return this.users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}
	
	@ManyToOne
	@JoinColumn(name = "application_id", nullable = false)
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}
	
}