package py.com.sodep.mobileforms.impl.services.config;

import org.springframework.beans.factory.annotation.Autowired;

import py.com.sodep.mobileforms.api.services.config.IAcmeModuleService;

public class ApplicationStartupManager {

	@Autowired
	IAcmeModuleService acmeModule;
	
	public void postInitialized(){
		// since there is no interface to edit the menu tree we need to compute the lft and rgt values when the system is starting.
		// With this approach we can use the "parent_id" to build the tree manually on the database and the system will compute the lft,rgt values for itself
		acmeModule.assignPreOrderValues();
		acmeModule.checkTreeConsistency();
	}
}
