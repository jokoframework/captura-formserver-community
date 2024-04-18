package py.com.sodep.mobileforms.api.dtos;


public class CVSImportOptionsDTO implements DTO {

	private static final long serialVersionUID = 1L;

	private String lookupTableName;
	private Boolean useTab;
	private Boolean useColon;
	private Boolean useSemicolon;
	private Boolean useComma;
	private Boolean useSpace;

	private String textQualifier;
	private Boolean useFirstRowAsHeader;
	private String filename;

	public String getLookupTableName() {
		return lookupTableName;
	}

	public void setLookupTableName(String lookupTableName) {
		this.lookupTableName = lookupTableName;
	}

	public Boolean getUseTab() {
		return useTab;
	}

	public void setUseTab(Boolean useTab) {
		this.useTab = useTab;
	}

	public Boolean getUseColon() {
		return useColon;
	}

	public void setUseColon(Boolean useColon) {
		this.useColon = useColon;
	}

	public Boolean getUseSemicolon() {
		return useSemicolon;
	}

	public void setUseSemicolon(Boolean useSemicolon) {
		this.useSemicolon = useSemicolon;
	}

	public Boolean getUseComma() {
		return useComma;
	}

	public void setUseComma(Boolean useComma) {
		this.useComma = useComma;
	}

	public Boolean getUseSpace() {
		return useSpace;
	}

	public void setUseSpace(Boolean useSpace) {
		this.useSpace = useSpace;
	}

	public String getTextQualifier() {
		return textQualifier;
	}

	public void setTextQualifier(String textQualifier) {
		this.textQualifier = textQualifier;
	}

	public Boolean getUseFirstRowAsHeader() {
		return useFirstRowAsHeader;
	}

	public void setUseFirstRowAsHeader(Boolean useFirstRowAsHeader) {
		this.useFirstRowAsHeader = useFirstRowAsHeader;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}