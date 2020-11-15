package py.com.sodep.mobileforms.api.entities.application;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.application.ApplicationUserAssociation.ApplicationUserAssociationPK;
import py.com.sodep.mobileforms.api.entities.core.User;

@Entity
@Table(name = "application_users", schema = "applications")
@IdClass(ApplicationUserAssociationPK.class)
public class ApplicationUserAssociation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum ASSOCIATION_STATUS {
		MEMBER, INVITED
	}

	public static class ApplicationUserAssociationPK implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private User user;

		private Application application;

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

		public Application getApplication() {
			return application;
		}

		public void setApplication(Application application) {
			this.application = application;
		}
	}

	private User user;

	private Application application;

	private ASSOCIATION_STATUS status;

	@Id
	@ManyToOne
	@JoinColumn(name = "user_id")
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Id
	@ManyToOne
	@JoinColumn(name = "application_id")
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	@Enumerated(EnumType.ORDINAL)
	public ASSOCIATION_STATUS getStatus() {
		return status;
	}

	public void setStatus(ASSOCIATION_STATUS status) {
		this.status = status;
	}

}
