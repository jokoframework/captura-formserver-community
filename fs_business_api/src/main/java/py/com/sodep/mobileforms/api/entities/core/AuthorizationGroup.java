package py.com.sodep.mobileforms.api.entities.core;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/*
 * This is a Entity System and is only used for Grouping purposes(grouping of Authorizations) In the Page creation of new Roles
 * 
 */

@Entity
@Table(schema = "sys", name = "authorizations_groups")
public class AuthorizationGroup implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;

	private Integer position;

	private List<Authorization> authorizations;

	@Id
	@Column(name = "i18n_name", unique = true, nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	@OneToMany
	@JoinColumn(name = "auth_group")
	public List<Authorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(List<Authorization> authorizations) {
		this.authorizations = authorizations;
	}

}
