package py.com.sodep.mobileforms.api.entities.sys;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.core.Authorization;

@Entity
@Table(schema = "sys", name = "acme_launchers")
@SequenceGenerator(name = "seq_acme_launchers", sequenceName = "sys.seq_acme_module")
public class AcmeLauncher {

	public static enum ACME_LAUNCH_TYPE {
		OPEN_VIEW, EXECUTE_JS
	};

	private Integer id;
	private ACME_LAUNCH_TYPE launchType;
	
	private String jsCode;
	
	private AcmeView acmeView;
	private String jsAMD;
	
	private Authorization authorization;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_acme_launchers")
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "launch_type")
	public ACME_LAUNCH_TYPE getLaunchType() {
		return launchType;
	}

	public void setLaunchType(ACME_LAUNCH_TYPE launchType) {
		this.launchType = launchType;
	}

	@Column(name = "js_code")
	public String getJsCode() {
		return jsCode;
	}

	public void setJsCode(String jsCode) {
		this.jsCode = jsCode;
	}

	@ManyToOne
	@JoinColumn(name = "view_id")
	public AcmeView getAcmeView() {
		return acmeView;
	}

	public void setAcmeView(AcmeView acmeView) {
		this.acmeView = acmeView;
	}

	@Column(name="js_amd")
	public String getJsAMD() {
		return jsAMD;
	}

	public void setJsAMD(String jsAMD) {
		this.jsAMD = jsAMD;
	}

	@ManyToOne
	@JoinColumn(name="authorization_name")
	public Authorization getAuthorization() {
		return authorization;
	}

	public void setAuthorization(Authorization authorization) {
		this.authorization = authorization;
	}
	
}
