package py.com.sodep.mobileforms.api.entities.scripting;

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

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.scripting.IScriptingService;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A saved Groovy/Java script.
 * 
 * A script is saved and/or executed by an implementation
 * {@link IScriptingService} for development/maintenance purposes.
 * 
 * @see {@link IScriptingService}
 * @author Miguel
 * 
 */
@Entity
@Table(name = "scripts", schema = "scripting")
@SequenceGenerator(name = "seq_script", sequenceName = "scripting.seq_script")
public class SodepScript implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private User user;

	private String name;

	private String script;

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_script")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Column(name = "script_name", nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "script_code", nullable = false, columnDefinition = "TEXT")
	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

}
