package py.com.sodep.mobileforms.api.entities.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;

@Entity
@Table(schema = "mf_data", name = "connector")
@SequenceGenerator(name = "seq_connector_id", sequenceName = "mf_data.seq_connector_id")
public class Connector implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum CONNECTOR_DIRECTION {
		INPUT, OUTPUT, BIDIRECTIONAL
	};

	public static enum CONNECTOR_TYPE {
		REST, EMAIL, GOOGLE_SPREADSHEET
	};

	private Long id;
	private String name;

	private CONNECTOR_DIRECTION direction;
	private CONNECTOR_TYPE type;
	private Application application;
	private User user;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_connector_id")
	@Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "direction")
	public CONNECTOR_DIRECTION getDirection() {
		return direction;
	}

	public void setDirection(CONNECTOR_DIRECTION direction) {
		this.direction = direction;
	}

	@Column(name = "connector_type")
	public CONNECTOR_TYPE getType() {
		return type;
	}

	public void setType(CONNECTOR_TYPE type) {
		this.type = type;
	}

	@ManyToOne
	@JoinColumn(name = "application_id", nullable = false)
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
