package py.com.sodep.mobileforms.api.entities.report;

import java.io.Serializable;

import javax.persistence.*;

import py.com.sodep.mobileforms.api.entities.forms.Form;

@Entity
@Table(schema = "reports", name = "queries")
@SequenceGenerator(name = "seq_querys", sequenceName = "reports.seq_querys")
public class Query implements Serializable {

	private static final long serialVersionUID = 2L;

	private Long id;

	private String name;

	private Form form;

	private Boolean defaultQuery = false;

	private String selectedTableColumns;

	private String selectedCSVColumns;
	
	private String selectedSortingColumns;

	private String filterOptions;
	
	private Boolean downloadLocationsAsLinks;

    private String elementsFileNames;

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_querys")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne
	@JoinColumn(name = "form_id", nullable = false)
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	@Column(name = "default_query", nullable = false)
	public Boolean getDefaultQuery() {
		return defaultQuery;
	}

	public void setDefaultQuery(Boolean defaultQuery) {
		this.defaultQuery = defaultQuery;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Column(name = "selected_table_columns")
	public String getSelectedTableColumns() {
		return selectedTableColumns;
	}

	public void setSelectedTableColumns(String selectedTableColumns) {
		this.selectedTableColumns = selectedTableColumns;
	}

	@Column(name = "selected_csv_columns")
	public String getSelectedCSVColumns() {
		return selectedCSVColumns;
	}

	public void setSelectedCSVColumns(String selectedCSVColumns) {
		this.selectedCSVColumns = selectedCSVColumns;
	}

	@Column(name = "filter_options")
	public String getFilterOptions() {
		return filterOptions;
	}

	public void setFilterOptions(String filterOptions) {
		this.filterOptions = filterOptions;
	}
	
	@Column(name = "download_locations_as_links", nullable = false)
	public Boolean getDownloadLocationsAsLinks() {
		return downloadLocationsAsLinks;
	}

	public void setDownloadLocationsAsLinks(Boolean downloadLocationsAsLinks) {
		this.downloadLocationsAsLinks = downloadLocationsAsLinks;
	}
	
	@Column(name = "selected_sorting_columns")
	public String getSelectedSortingColumns() {
		return selectedSortingColumns;
	}
	
	public void setSelectedSortingColumns(String selectedSortingColumns) {
		this.selectedSortingColumns = selectedSortingColumns;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Query other = (Query) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

    @Column(name = "elements_file_names")
    public String getElementsFileNames() {
        return this.elementsFileNames;
    }

    public void setElementsFileNames(String elementsFileNames) {
        this.elementsFileNames = elementsFileNames;
    }
}
