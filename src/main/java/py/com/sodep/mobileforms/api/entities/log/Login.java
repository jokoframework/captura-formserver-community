package py.com.sodep.mobileforms.api.entities.log;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(schema = "log", name = "logins")
@SequenceGenerator(name = "seq_logins", sequenceName = "log.seq_logins")
public class Login {

	private Long id;

	private Timestamp loginAt;

	private Long applicationId;

	private Long userId;

	private String loginTypeValue;
	
	
	public Login() {

	}

	public Login(Long applicationId, Long userId) {
		super();
		this.applicationId = applicationId;
		this.userId = userId;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(generator = "seq_logins")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "login_at", insertable = false, updatable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	public Timestamp getLoginAt() {
		return loginAt;
	}

	public void setLoginAt(Timestamp loginAt) {
		this.loginAt = loginAt;
	}

	@Column(name = "application_id", nullable = false)
	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	@Column(name = "user_id", nullable = false)
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "login_type_value")
	public String getLoginTypeValue() {
		return loginTypeValue;
	}

	public void setLoginTypeValue(
			String loginTypeValue) {
		this.loginTypeValue = loginTypeValue;
	}

	@Transient
	public LoginType getLoginType() {
		return LoginType.fromValue(loginTypeValue);
	}
	
	public void setLoginType(LoginType type) {
		this.loginTypeValue = type.toValue();
	}
}
