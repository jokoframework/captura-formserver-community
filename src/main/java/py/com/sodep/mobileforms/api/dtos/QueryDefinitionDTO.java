package py.com.sodep.mobileforms.api.dtos;

import java.util.List;
import java.util.Map;

public class QueryDefinitionDTO extends QueryDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> selectedTableColumns;
	private List<String> selectedCSVColumns;
	private List<String> selectedSortingColumns;
	private List<ReportFilterOptionDTO> filterOptions;
	private Boolean downloadLocationsAsLinks;
    private Map<String, String> elementsFileNames;

    public List<String> getSelectedTableColumns() {
		return selectedTableColumns;
	}

	public void setSelectedTableColumns(List<String> selectedTableColumns) {
		this.selectedTableColumns = selectedTableColumns;
	}

	public List<String> getSelectedCSVColumns() {
		return selectedCSVColumns;
	}

	public void setSelectedCSVColumns(List<String> selectedCSVColumns) {
		this.selectedCSVColumns = selectedCSVColumns;
	}

	public List<ReportFilterOptionDTO> getFilterOptions() {
		return filterOptions;
	}

	public void setFilterOptions(List<ReportFilterOptionDTO> filterOptions) {
		this.filterOptions = filterOptions;
	}

	public Boolean getDownloadLocationsAsLinks() {
		return downloadLocationsAsLinks;
	}

	public void setDownloadLocationsAsLinks(Boolean downloadLocationsAsLinks) {
		this.downloadLocationsAsLinks = downloadLocationsAsLinks;
	}

	public List<String> getSelectedSortingColumns() {
		return selectedSortingColumns;
	}

	public void setSelectedSortingColumns(List<String> selectedSortingColumns) {
		this.selectedSortingColumns = selectedSortingColumns;
	}

    public Map<String, String> getElementsFileNames() {
        return this.elementsFileNames;
    }

    public void setElementsFileNames(Map<String, String> elementsFileNames) {
        this.elementsFileNames = elementsFileNames;
    }

}
