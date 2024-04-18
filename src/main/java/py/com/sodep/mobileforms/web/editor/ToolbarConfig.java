package py.com.sodep.mobileforms.web.editor;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.DTO;
import py.com.sodep.mobileforms.api.dtos.PoolModelDTO;

public class ToolbarConfig implements DTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String title;

	private List<PoolModelDTO> sections;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<PoolModelDTO> getSections() {
		return sections;
	}

	public void setSections(List<PoolModelDTO> sections) {
		this.sections = sections;
	}

}
