package py.com.sodep.mobileforms.impl.services.ui.multiselect;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.services.ui.multiselect.IMultiselectService;

@Transactional
public abstract class BaseMultiselectService implements IMultiselectService {

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;
}
