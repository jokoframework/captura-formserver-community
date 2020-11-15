package py.com.sodep.mobileforms.api.dtos;


import java.util.List;

import py.com.sodep.mf.form.model.prototype.MFPrototype;
import py.com.sodep.mobileforms.api.entities.pools.Pool;

public class PoolModelDTO extends PoolDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<MFPrototype> prototypes;
	
	public PoolModelDTO(){
		
	}
	
	public PoolModelDTO(PoolDTO pool){
		this.setActive(pool.isActive());
		this.setDescription(pool.getDescription());
		this.setId(pool.getId());
		this.setName(pool.getName());
	}
	
	public PoolModelDTO(Pool pool){
		this.setActive(pool.getActive());
		this.setDescription(pool.getDescription());
		this.setId(pool.getId());
		this.setName(pool.getName());
	}

	public List<MFPrototype> getPrototypes() {
		return prototypes;
	}

	public void setPrototypes(List<MFPrototype> items) {
		this.prototypes = items;
	}

}

