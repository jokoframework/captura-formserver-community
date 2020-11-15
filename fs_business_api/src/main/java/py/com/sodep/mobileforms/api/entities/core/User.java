package py.com.sodep.mobileforms.api.entities.core;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.interfaces.IAppAwareEntity;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;

/**
 * This entity represents a user
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "core", name = "users", uniqueConstraints = { @UniqueConstraint(columnNames = "mail") })
// @EntityListeners({UserEntityListener.class })
public class User extends AuthorizableEntity implements IAppAwareEntity {

	public static final String FIRSTNAME = "firstName";

	public static final String LASTNAME = "lastName";

	public static final String MAIL = "mail";

	public static final String PASSWORD = "password";

	public static final String PASSWORD_SECURED = "<SECURED>";

	private static final long serialVersionUID = 1L;

	private String firstName;

	private String lastName;

	private String password;

	private String mail;

	private String formerMail;

	private Date expirationDate;

	private String language;

    private String securePassword;

    private String salt;

	private Set<Group> groups;

	private List<Device> devices;

	private List<Application> applicationsOwned;

	private List<Application> applications;

	private Application defaultApplication;

	private boolean rootUser;

	@PrePersist
	public void prePersist() {
		if (!getDeleted() && mail == null) {
			throw new InvalidEntityException("User's mail cannot be null"); // FIXME
																			// i18n
																			// ?
		}
	}

	@PreUpdate
	public void preUpdate() {
		if (getDeleted()) {
			if (formerMail == null && mail != null) {
				// #747
				formerMail = mail;
				mail = null;
			}
		} else {
			if (mail == null) {
				throw new InvalidEntityException("User's mail cannot be null");
			}
		}
	}

	@OneToMany(cascade = { CascadeType.PERSIST }, mappedBy = "owner")
	public List<Application> getApplicationsOwned() {
		return applicationsOwned;
	}

	@ManyToMany(mappedBy = "appUsers")
	@OrderBy("id")
	public List<Application> getApplications() {
		return applications;
	}

	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}

	public void setApplicationsOwned(List<Application> applicationsOwned) {
		this.applicationsOwned = applicationsOwned;
	}

	public User() {
	}

	@Column(name = "first_name", nullable = false, length = STR_LENGTH_LONG)
	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "last_name", nullable = false, length = STR_LENGTH_LONG)
	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Column(nullable = false, length = STR_LENGTH_LONG)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@ManyToMany(mappedBy = "users")
	public Set<Group> getGroups() {
		return this.groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Column(length = 250, unique = true)
	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	@ManyToMany(mappedBy = "users")
	public List<Device> getDevices() {
		return this.devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	// The default application can be null when the user
	// completes the registration on the public web page
	@ManyToOne
	@JoinColumn(nullable = true, name = "default_application")
	public Application getApplication() {
		return defaultApplication;
	}

	public void setApplication(Application defaultApplication) {
		this.defaultApplication = defaultApplication;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			// Check the equals method of the SodepEntity. If the ID are
			// different just return false. Otherwise compare the properties
			return false;
		}
		// This shouldn't be necessary, is already checked in SodepEntity's
		// equals method
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		// -----------------------------//
		if (expirationDate == null) {
			if (other.expirationDate != null)
				return false;
		} else if (!expirationDate.equals(other.expirationDate))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (mail == null) {
			if (other.mail != null)
				return false;
		} else if (!mail.equals(other.mail))
			return false;
		return true;
	}

	// #747
	@Column(name = "former_mail")
	public String getFormerMail() {
		return formerMail;
	}

	public void setFormerMail(String formerMail) {
		this.formerMail = formerMail;
	}

	@Column(name = "system_administrator")
	public boolean isRootUser() {
		return rootUser;
	}

	public void setRootUser(boolean rootUser) {
		this.rootUser = rootUser;
	}

	@Override
	public String toString() {
		return "#" + getId() + " ( " + mail + " )";
	}

    @Column(name = "secure_password")
    public String getSecurePassword() {
        return securePassword;
    }

    public void setSecurePassword(String p_securePassword) {
        securePassword = p_securePassword;
    }

    @Column(name = "salt")
    public String getSalt() {
        return salt;
    }

    public void setSalt(String p_salt) {
        salt = p_salt;
    }
}
