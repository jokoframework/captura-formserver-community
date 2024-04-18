package py.com.sodep.mobileforms.api.entities.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.SodepEntity;

/**
 * An AuthorizableEntity is an entity that has a list of Authorizations
 * (Permissions, privileges, roles).
 * 
 * Its concrete subclasses are {@link User} and {@link Group}
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "core", name = "authorizable_entities")
@SequenceGenerator(name = "seq_authorizable_entities", sequenceName = "core.seq_authorizable_entities")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AuthorizableEntity extends SodepEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// private List<Authorization> authorizations;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_authorizable_entities")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	
}
