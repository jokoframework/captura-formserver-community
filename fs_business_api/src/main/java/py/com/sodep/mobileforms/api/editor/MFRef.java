package py.com.sodep.mobileforms.api.editor;

import py.com.sodep.mf.form.model.MFBaseModel;

/**
 * In a command a reference to an Element.
 * 
 * The element can be a Form, Page or any other Item (Input - Text, Decimal ...
 * - Photo, Location ...) See py.com.sodep.mf.form.model.MFBaseModel.Type
 * 
 * It will have an id if it has been saved.
 * 
 * It will have a reference to a container if it's not a FORM. E.g. an Item, a
 * Photo Item for example, will point to the page which contains the item
 * 
 * The position is very important. Most commands are executed based on the
 * position of items. Since an item may not be saved, the id may not be usable,
 * so when executing a command, the position of the item is used to identify it.
 * *
 * 
 * @author jmpr
 * 
 */
public class MFRef {

	private MFBaseModel.Type type;

	private Long id;

	private Integer position;

	private MFRef container;

	public MFBaseModel.Type getType() {
		return type;
	}

	public void setType(MFBaseModel.Type type) {
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public MFRef getContainer() {
		return container;
	}

	public void setContainer(MFRef container) {
		this.container = container;
	}
}
