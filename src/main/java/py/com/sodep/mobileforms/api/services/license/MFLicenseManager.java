package py.com.sodep.mobileforms.api.services.license;

import java.io.InputStream;
import java.util.Date;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.license.MFApplicationLicense;
import py.com.sodep.mobileforms.license.MFFormServerLicense;

public interface MFLicenseManager {

	/**
	 * There's a unique license for the server.
	 * 
	 * The license constraints the number of applications that this Server can
	 * have
	 * 
	 * @return
	 */
	MFFormServerLicense getFormServerLicense();

	/**
	 * An application may be free of license.
	 * 
	 * In that case, no constraints apply to the application.
	 * 
	 * @param applicationId
	 * @return
	 */
	boolean doesLicenseApply(Long applicationId);

	/**
	 * Returns the license for the application with the given id
	 * 
	 * @param applicationId
	 * @return
	 */
	MFApplicationLicense getLicense(Long applicationId);

	void reloadLicenses();

	MFApplicationLicense parseApplicationLicense(InputStream is);

	MFApplicationLicense parseEncrypted(String str);

	void setLicense(Long applicationId, String encryptedLicense);

	//FIXME The reason why I decided to pass the application's ID
	// in other methods, instead of an instance of Application, is to have a low
	// coupling with the "core". So, if needed and suitable, a license
	// module could be easily created as a separated/independent project. 
	// The core would depend on the license module but the module 
	// would stand alone.
	// jmpr - 31-10-2013
	MFAppLicenseStatus getLicenseStatus(Application app);
	
	Date caculateExpirationDate(MFApplicationLicense license);

}
