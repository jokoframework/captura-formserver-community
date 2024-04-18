package py.com.sodep.mobileforms.api.entities.core;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;

/**
 * This entity represents a mobile device that belongs to a user
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "core", name = "devices", uniqueConstraints = { @UniqueConstraint(columnNames = { "model", "brand",
		"os", "identifier", "version_number", "phone_number" }) })
@SequenceGenerator(name = "seq_devices", sequenceName = "core.seq_devices")
public class Device extends SodepEntity {

	public enum OS {
		ANDROID("Android"), IOS("iOS"), BLACKBERRY("BlackBerry");

		private String name;

		OS(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
		public static OS getOS(String os){
			return OS.valueOf(os.toUpperCase());
		}
	};

	/**
	 * 
	 */

	private static final long serialVersionUID = 1L;

	private String phoneNumber;

	private String brand;

	private String model;

	private OS os;

	private List<User> users;

	private String identifier;

	private String versionNumber;

	private Application application;

	private String manufacturer;
	
	private String product;
	
	private String release;
	
	private Boolean blacklisted;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_devices")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	@Column(name = "phone_number")
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String number) {
		this.phoneNumber = number;
	}

	@Column(name = "model")
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@ManyToMany
	@JoinTable(schema = "core", name = "users_devices", joinColumns = { @JoinColumn(name = "device_id") }, inverseJoinColumns = { @JoinColumn(name = "user_id") })
	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	@Column(name = "brand")
	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	@Enumerated(EnumType.STRING)
	public OS getOs() {
		return os;
	}

	public void setOs(OS os) {
		this.os = os;
	}

	@Column(name = "identifier")
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Column(name = "version_number")
	public String getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}

	@ManyToOne
	@JoinColumn(name = "application_id", nullable = false)
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	@Column(name = "manufacturer")
	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	@Column(name = "product")
	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	@Column(name = "release")
	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public Boolean getBlacklisted() {
		return blacklisted;
	}

	public void setBlacklisted(Boolean blacklisted) {
		this.blacklisted = blacklisted;
	}
}
