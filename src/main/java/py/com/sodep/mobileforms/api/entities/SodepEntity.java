package py.com.sodep.mobileforms.api.entities;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

/**
 * It is strongly encouraged that all entities that have an Id of type Long be a
 * subclass of SodepEntity.
 * 
 * The Id should be the id property.
 * 
 * @author Miguel
 * 
 */
@MappedSuperclass
public abstract class SodepEntity implements Serializable, Cloneable {

	public static final String ID = "id";
	
	public static final String ACTIVE = "active";
	
	public static final String DELETED = "deleted";

	protected Long id;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static final int STR_LENGTH_LONG = 512;

	protected static final int STR_MEDIUM_LONG = 512;

	private boolean deleted = false;

	private boolean active = true;

	private Timestamp created;

	private Timestamp modified;

	@PreUpdate
	public void preUpdate() {
		modified = new Timestamp(System.currentTimeMillis());
	}
	
	@PrePersist
	public void prePersist(){
		//xFIXME is this necessary?
		// Column is marked as insertable = false, updatable = false, nullable = false
		//created = new Timestamp(System.currentTimeMillis());
	}
	
	@PreRemove
	public void preRemove(){
		
	}

	/**
	 * This method should be implemented and annotated with Id by the
	 * subclassing Entity
	 * 
	 * @return
	 */
	@Transient
	public abstract Long getId();

	public void setId(Long id) {
		this.id = id;
	}

	@Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@Column(insertable = false, updatable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	public Timestamp getCreated() {
		return this.created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	public Timestamp getModified() {
		return modified;
	}

	public void setModified(Timestamp modified) {
		this.modified = modified;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SodepEntity other = (SodepEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
