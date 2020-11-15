package py.com.sodep.mobileforms.api.entities.pools;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.LogicalDelete;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.core.interfaces.IAppAwareEntity;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;


//FIXME Pool should be i18n!
@Entity
@Table(schema = "pools", name = "pools")
@SequenceGenerator(name = "seq_pools", sequenceName = "pools.seq_pools")
public class Pool extends SodepEntity implements IAppAwareEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "name";
	
	public static final String DESCRIPTION = "description";

	private String name;

	private String description;

	private List<ElementPrototype> prototypes;

	private Application application;
	
	private User owner;
	
	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_pools")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	@Column(nullable = false)
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@LogicalDelete
	@OneToMany(mappedBy = "pool", cascade = { CascadeType.PERSIST })
	public List<ElementPrototype> getPrototypes() {
		return prototypes;
	}

	public void setPrototypes(List<ElementPrototype> prototypes) {
		this.prototypes = prototypes;
	}

	@ManyToOne
	@JoinColumn(name = "application_id", nullable = true)
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	@ManyToOne
	@JoinColumn(nullable = false, name = "owner_id")
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}
	
}
