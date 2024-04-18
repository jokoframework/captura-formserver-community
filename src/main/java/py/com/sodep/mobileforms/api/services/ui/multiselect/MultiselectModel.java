package py.com.sodep.mobileforms.api.services.ui.multiselect;

import java.util.ArrayList;
import java.util.List;

import py.com.sodep.mobileforms.api.dtos.DTO;

public class MultiselectModel implements DTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class Column {

		public Column() {

		}

		public Column(String id, String label, String formatter) {
			this.id = id;
			this.label = label;
			this.formatter = formatter;
		}

		String id;

		String label;

		String formatter;
	}

	public static enum RIGHT_IMAGE {
		NONE, ARROW
	}

	public static enum LEFT_IMAGE {
		NONE, SIMPLE_CHECKBOX
	}

	private String label;

	private LEFT_IMAGE leftImage;

	private RIGHT_IMAGE rightImage;

	private boolean showColumnNames = false;

	private List<Column> columns;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public LEFT_IMAGE getLeftImage() {
		return leftImage;
	}

	public void setLeftImage(LEFT_IMAGE leftImage) {
		this.leftImage = leftImage;
	}

	public RIGHT_IMAGE getRightImage() {
		return rightImage;
	}

	public void setRightImage(RIGHT_IMAGE rightImage) {
		this.rightImage = rightImage;
	}

	public boolean isShowColumnNames() {
		return showColumnNames;
	}

	public void setShowColumnNames(boolean showColumnNames) {
		this.showColumnNames = showColumnNames;
	}

	public void addColumn(Column c) {
		if (columns == null) {
			columns = new ArrayList<MultiselectModel.Column>();
		}
		columns.add(c);
	}

	public int getSize() {
		if (columns == null) {
			return 0;
		}
		return columns.size();
	}

}
