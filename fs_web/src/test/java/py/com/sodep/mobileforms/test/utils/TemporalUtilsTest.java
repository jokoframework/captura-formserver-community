package py.com.sodep.mobileforms.test.utils;

import org.junit.Assert;
import org.junit.Test;

import py.com.sodep.mobileforms.utils.TemporalUtils;

/**
 * This class test {@link TemporalUtils} public methods
 */
public class TemporalUtilsTest {

	@Test
	public void testValidDates() {
		String[] strs = new String[] { "07/05/2012", "07/06/2012", "02/02/2002", "01/01/2000", "16/03/2002",
				"12/12/2012", "07/06/2012", "07/05/2004", "04/05/1902", "07/08/2012", "07/09/2010", "07/11/2011",
				"17/10/2000", "28/09/1981", "09/04/1811" };
		String format = "dd/MM/yy";
		for (String str : strs) {
			boolean valid = TemporalUtils.isLegalDateStringWithFormat(str, format);
			Assert.assertTrue(valid);
		}
	}
	
	@Test
	public void testValidTimes() {
		String[] strs = new String[] { "10:30 pm", "12:23 am" };
		String format = "hh:mm a";
		for (String str : strs) {
			boolean valid = TemporalUtils.isLegalDateStringWithFormat(str, format);
			Assert.assertTrue(valid);
		}
	}

	@Test
	public void testInvalidDates() {
		String[] strs = new String[] { "47/05/2012", "070/16/2012", "02//02//002", "01/021/2000", "16-03/2002",
				"12/12-2012", "07/15/2004", "04000/05/1902", "hola", "17/10/", "28//1981", "/04/1811", "23" };
		String format = "dd/MM/yy";
		for (String str : strs) {
			boolean valid = TemporalUtils.isLegalDateStringWithFormat(str, format);
			Assert.assertFalse(valid);
		}
	}

}
