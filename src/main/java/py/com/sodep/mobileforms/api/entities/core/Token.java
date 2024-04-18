package py.com.sodep.mobileforms.api.entities.core;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;

@Entity
@Table(name = "tokens", schema = "core")
@SequenceGenerator(name = "seq_tokens", sequenceName = "core.seq_tokens")
public class Token extends SodepEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static int MAX_TOKEN_LENGTH = 200;

	private String token;

	private Date expires;

	private int purpose;

	private User grantee;

	private User granter;
	
	private Application application;

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_tokens")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	@Column(nullable = false)
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public int getPurpose() {
		return purpose;
	}

	public void setPurpose(int purpose) {
		this.purpose = purpose;
	}

	
	@ManyToOne
	@JoinColumn(name = "grantee_id", nullable = false)
	public User getGrantee() {
		return grantee;
	}

	public void setGrantee(User grantee) {
		this.grantee = grantee;
	}

	@ManyToOne
	@JoinColumn(name="granter_id", nullable = true)
	public User getGranter() {
		return granter;
	}

	public void setGranter(User granter) {
		this.granter = granter;
	}

	@ManyToOne
	@JoinColumn(name = "application_id", nullable = true)
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}
	
	

}
