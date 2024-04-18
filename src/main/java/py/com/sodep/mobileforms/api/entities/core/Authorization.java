package py.com.sodep.mobileforms.api.entities.core;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;

/**
 * An Authorization is identified by a unique name. Its semantics is defined
 * through documentation, taking into account requirements + design.
 * 
 * A developer is responsible of testing the authorizations of an
 * AuthorizableEntity using an implementation of
 * {@link IAuthorizationControlService} when needed.
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "core", name = "authorizations")
@SequenceGenerator(name = "seq_authorizations", sequenceName = "core.seq_authorizations")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Authorization implements Serializable {

	// DO NOT CHANGE THIS ORDER, because it is mapped in the DB
	public static final int LEVEL_SYSTEM = 0;
	public static final int LEVEL_APP = 1;
	public static final int LEVEL_PROJECT = 2;
	public static final int LEVEL_FORM = 3;
	public static final int LEVEL_POOL = 4;
	public static final int MAX_NUMBER_OF_LEVELS = 5;

	public static final String NAME = "name";

	private static final long serialVersionUID = 1L;

	private String name;

	private int level;

	private AuthorizationGroup authorizationGroup;

	private List<Authorization> dependentAuthorizations;

	private Boolean visible = true;

	@Id
	@Column(unique = true, nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {

	}

	@Column(name = "auth_level")
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@ManyToOne
	@JoinColumn(name = "auth_group", nullable = true)
	public AuthorizationGroup getAuthorizationGroup() {
		return authorizationGroup;
	}

	public void setAuthorizationGroup(AuthorizationGroup authorizationGroup) {
		this.authorizationGroup = authorizationGroup;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST })
	@JoinTable(schema = "core", name = "authorization_dependencies", joinColumns = { @JoinColumn(name = "base_auth", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "granted_auth", nullable = false) })
	public List<Authorization> getDependentAuthorizations() {
		return dependentAuthorizations;
	}

	public void setDependentAuthorizations(List<Authorization> dependentAuthorizations) {
		this.dependentAuthorizations = dependentAuthorizations;
	}

	@Column(name = "visible")
	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Authorization other = (Authorization) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Authorization [name=" + name + ", level=" + level + ", visible=" + visible + "]";
	}

}