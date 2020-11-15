package py.com.sodep.mobileforms.api.entities.workflow;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.SodepEntity;

@Entity
@Table(schema = "workflow", name = "states")
@SequenceGenerator(name = "seq_states", sequenceName = "workflow.seq_states")
public class State extends SodepEntity {

	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private String description;
	
	private Boolean initial;
	
	private Long formId;
	
	private List<StateRole> stateRoles;
	

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_states")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Column(nullable = false)
	public Boolean getInitial() {
		return initial;
	}

	public void setInitial(Boolean initial) {
		this.initial = initial;
	}

	@Column(name = "form_id", nullable = false)
	public Long getFormId() {
		return formId;
	}

	public void setFormId(Long formId) {
		this.formId = formId;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "state")
	public List<StateRole> getStateRoles() {
		return stateRoles;
	}

	public void setStateRoles(List<StateRole> stateRoles) {
		this.stateRoles = stateRoles;
	}

}
