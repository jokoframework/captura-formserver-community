package py.com.sodep.mobileforms.test.authorization.unit;

import java.util.Set;

import org.junit.Test;

import junit.framework.Assert;
import py.com.sodep.mobileforms.api.services.auth.ComputedAuthorizations;

public class ComputedAuthorizationTest {

	/**
	 * Add an authorization and check that the computed authorization returns
	 * true when we ask if there are enough access rights.
	 */
	@Test
	public void testHasAccess() {
		ComputedAuthorizations ca = new ComputedAuthorizations(2);
		int level = 0;
		long objId = 1;
		String auth = "auth.test";

		// add an authorization
		ca.addAuthorization(level, objId, auth);
		// check that the authorization has been granted
		boolean access = ca.hasAccess(level, objId, auth);
		Assert.assertTrue(access);
		// check that the object "objId" is returned when we asked for the
		// granted objects
		Set<Long> objs = ca.getGrantedObjects(level, auth);
		Assert.assertTrue(objs.contains(objId));

		// check that different permission returns false
		access = ca.hasAccess(level, objId, auth + "x");
		Assert.assertFalse(access);

		// check that the object is not returned with the given permission
		objs = ca.getGrantedObjects(level, auth + "x");
		Assert.assertFalse(objs.contains(objId));

		// check that the same authorization has not been granted to a different
		// level
		access = ca.hasAccess(level + 1, objId, auth);
		Assert.assertFalse(access);

		// check that the object is not returned if it asked on a different
		// level
		objs = ca.getGrantedObjects(level + 1, auth);
		Assert.assertFalse(objs.contains(objId));

		// check that the authorization has not been granted over a different
		// object
		access = ca.hasAccess(level, objId + 1, auth);
		Assert.assertFalse(access);

	}
	/**
	 * Try add authorization to inexistent level, should fail
	 * with an exception
	 */
	@Test
	public void testLevelOverflow() {
		int level = 2;
		ComputedAuthorizations ca = new ComputedAuthorizations(level);
		try {
			ca.addAuthorization(level + 1, 0l, "auth.test");
		} catch (IndexOutOfBoundsException e) {
			// do nothing, we are expecting this exception to happen
		}
	}

}
