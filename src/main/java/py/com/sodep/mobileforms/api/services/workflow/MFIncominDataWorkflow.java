package py.com.sodep.mobileforms.api.services.workflow;

import java.util.HashMap;
import java.util.Map;

import py.com.sodep.mf.exchange.MFIncomingDataI;

public class MFIncominDataWorkflow implements MFIncomingDataI {

	public static final String META_FIELD_STATE_ID = "stateId";

	public static final String META_FIELD_COMMENT = "comment";
	
	private Integer handle;
	
	private Map<String, Object> data;
	
	private Map<String, Object> meta;

	public MFIncominDataWorkflow(Integer handle, Map<String, Object> data) {
		super();
		this.handle = handle;
		this.data = data;
	}
	
	public MFIncominDataWorkflow(Integer handle, Map<String, Object> data, Map<String, Object> meta) {
		this(handle, data);
		this.meta = meta;
	}

	public MFIncominDataWorkflow() {
		super();
		data = new HashMap<>();
		meta = new HashMap<>();
	}

	public MFIncominDataWorkflow(Long stateId, String comment) {
		this();
		stateId(stateId);
		comment(comment);
	}

	public MFIncominDataWorkflow(Map<String, Object> data, Map<String, Object> meta, Long stateId, String comment) {
		this();
		this.data = data;
		this.meta = meta;
		stateId(stateId);
		comment(comment);
	}

	@Override
	public Object getHandle() {
		return handle;
	}

	@Override
	public Map<String, Object> getData() {
		return data;
	}
	
	public Map<String, Object> getMeta(){
		return meta;
	}
	
	public MFIncominDataWorkflow stateId(Long stateId) {
		this.getMeta().put(META_FIELD_STATE_ID, stateId);
		return this;
	}
	
	public MFIncominDataWorkflow comment(String comment) {
		this.getMeta().put(META_FIELD_COMMENT, comment);
		return this;
	}
	
}
