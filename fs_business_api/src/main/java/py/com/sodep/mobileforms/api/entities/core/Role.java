package py.com.sodep.mobileforms.api.entities.core;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.interfaces.IAppAwareEntity;

@Entity
@Table(schema = "core", name = "roles")
@SequenceGenerator(name = "sequenceCoreRoles", sequenceName = "core.seq_core_roles")
public class Role extends SodepEntity implements IAppAwareEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String ID = "id";

	public static final String NAME = "name";

	public static final String DESCRIPTION = "description";
	
	public static final String LEVEL = "authLevel";

	private String name;

	private String description;
	
	private Application application;

	private boolean editable;

	private int authLevel;

	private List<Authorization> grants;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "sequenceCoreRoles")
	@Column(unique = true, nullable = false)
	@Override
	public Long getId() {
		return this.id;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setName(String name) {
		this.name = name;

	}

	public void setDescription(String description) {
		this.description = description;

	}

	@Column(name = "auth_level")
	public int getAuthLevel() {
		return authLevel;
	}

	public void setAuthLevel(int authLevel) {
		this.authLevel = authLevel;
	}
	
	@ManyToMany(cascade = { CascadeType.PERSIST })
	@JoinTable(schema = "core", name = "role_grants_authorization", joinColumns = { @JoinColumn(name = "role_id", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "authorization_name", nullable = false) })
	public List<Authorization> getGrants() {
		return grants;
	}

	public void setGrants(List<Authorization> grants) {
		this.grants = grants;
	}

	@ManyToOne
	@JoinColumn(name = "application_id", nullable = true)
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

}
