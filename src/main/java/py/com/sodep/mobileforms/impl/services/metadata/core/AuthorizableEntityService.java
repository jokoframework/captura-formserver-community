package py.com.sodep.mobileforms.impl.services.metadata.core;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.core.AuthorizableEntity;
import py.com.sodep.mobileforms.impl.services.metadata.AppAwareBaseService;

@Service("AuthorizableEntityService")
@Transactional
public class AuthorizableEntityService extends AppAwareBaseService<AuthorizableEntity> {

	public AuthorizableEntityService() {
		super(AuthorizableEntity.class);
	}

	
}
