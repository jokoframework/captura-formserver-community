package py.com.sodep.mobileforms.impl.services.metadata.forms.elements;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementInstance;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementInstanceService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;

@Service("ElementInstanceService")
@Transactional
public class ElementInstanceService extends BaseService<ElementInstance> implements IElementInstanceService {
	
	protected ElementInstanceService() {
		super(ElementInstance.class);
	}
	
	@Override
	public List<ElementInstance> listAllDataInputElements(Form form) {
		String queryStr = " SELECT e FROM Page p JOIN p.elements e WHERE p.deleted = false AND p.active=true " +
				" AND e.deleted = false AND e.active = true  AND p.form.id = :formId AND (SELECT COUNT(h) FROM Headline h WHERE h.id= e.prototype.id) = 0";
		TypedQuery<ElementInstance> q = em.createQuery(queryStr, ElementInstance.class);
		q.setParameter("formId", form.getId());
		List<ElementInstance> elements = q.getResultList();
		return elements;
	}
	
	@Override
	public ElementInstance findById(Long id) {
		return super.findById(id);
	}

}
