package py.com.sodep.mobileforms.api.entities.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mf.exchange.objects.lookup.LookupTableDTO;
import py.com.sodep.mf.form.model.prototype.MFSelect.OptionSource;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;

/**
 * This is the representation of Lookup table. The lookup tables is a collection
 * of homegenous data imported into the Mobileforms. A lookup table might have
 * different versions and the number and types of columns might vary. However, a
 * lookup table on a given version has a fixed number of columns that cann't be
 * altered. The class is not thread safe.
 * 
 * IMHO it shares a lot of common with the Forms and it might be merged in the
 * future.
 * 
 * @author danicricco
 * 
 */
@Entity
@Table(schema = "mf_data", name = "lookuptables")
@SequenceGenerator(name = "seq_lookupTables", sequenceName = "mf_data.seq_lookuptable_id")
public class DBLookupTable extends SodepEntity {

	private static final long serialVersionUID = 1L;

	private String defaultLanguage;
	private String dataSetDefinition;

	private Long datasetVersion;
	// indicates if data was entered directly on the form editor (embeded)
	// or if its coming form an external source or was manually entered
	// EMBEDDED means coming directly from the form editor
	// LOOKUP_TABLE means data was manually entered or is coming from a REST
	// source
	private OptionSource source;
	private Application application;
	private Boolean acceptsRESTDML;
	private User owner;// The user who created the lookupTable
	private String lastDMLRemoteIP;// The IP of the last user that performs a
									// DML on this
									// lookuptable. This only only counts for
									// rest created lookuptables

	private String identifier;
	private String name;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_lookupTables")
	@Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	@ManyToOne
	@JoinColumn(name = "application_id", nullable = false)
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	@Column(nullable = false, length = 250, name = "dataset_definition")
	public String getDataSetDefinition() {
		return dataSetDefinition;
	}

	public void setDataSetDefinition(String dataSetDefinition) {
		this.dataSetDefinition = dataSetDefinition;
	}

	@Column(nullable = false, length = 16, name = "default_language")
	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	@Column(name = "dataset_version")
	public Long getDatasetVersion() {
		return datasetVersion;
	}

	public void setDatasetVersion(Long datasetVersion) {
		this.datasetVersion = datasetVersion;
	}

	@Column(name = "option_source")
	public OptionSource getSource() {
		return source;
	}

	public void setSource(OptionSource source) {
		this.source = source;
	}

	@Column(name = "is_rest")
	public Boolean getAcceptsRESTDML() {
		return acceptsRESTDML;
	}

	public void setAcceptsRESTDML(Boolean isRest) {
		this.acceptsRESTDML = isRest;
	}

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@Column(name = "last_ddl_ip")
	public String getLastDMLRemoteIP() {
		return lastDMLRemoteIP;
	}

	public void setLastDMLRemoteIP(String lastDMLRemoteIP) {
		this.lastDMLRemoteIP = lastDMLRemoteIP;
	}

	@Column(name = "identifier")
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LookupTableDTO toDTO() {
		LookupTableDTO dto = new LookupTableDTO();
		dto.setAcceptRESTDMLs(getAcceptsRESTDML() != null ? getAcceptsRESTDML() : false);
		dto.setApplicationId(getApplication().getId());
		dto.setIdentifier(getIdentifier());
		dto.setName(getName());
		dto.setPk(getId());
		return dto;
	}
}
