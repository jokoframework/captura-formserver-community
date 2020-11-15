package py.com.sodep.mobileforms.impl.services.data;

import java.util.Map;

import py.com.sodep.mf.exchange.MFDataSetDefinition;

public class DuplicateDocumentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1l;
	private final MFDataSetDefinition dataDef;
	private final Map<String, Object> offendingRow;

	public DuplicateDocumentException(MFDataSetDefinition dataDef, Map<String, Object> offendingRow) {

		this.dataDef = dataDef;
		this.offendingRow = offendingRow;
	}

	public DuplicateDocumentException(MFDataSetDefinition dataDef, Map<String, Object> offendingRow, Throwable pThrowable) {
		super(pThrowable);
		this.dataDef = dataDef;
		this.offendingRow = offendingRow;
	}

	public MFDataSetDefinition getDataDef() {
		return dataDef;
	}

	public Map<String, Object> getOffendingRow() {
		return offendingRow;
	}

}
