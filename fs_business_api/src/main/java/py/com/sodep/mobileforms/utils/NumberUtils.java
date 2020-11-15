package py.com.sodep.mobileforms.utils;

public class NumberUtils {
	/**
	 * Parse a string to create an Integer
	 * 
	 * @param number
	 * @return null if not an integer or an integer
	 */
	public static Integer getInteger(String number) {
		if (number == null) {
			return null;
		}
		try {
			return Integer.parseInt(number);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Float getFloat(String number) {
		if (number == null) {
			return null;
		}
		try {
			return Float.parseFloat(number);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Long getLong(String number) {
		if (number == null) {
			return null;
		}
		try {
			return Long.parseLong(number);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Double getDouble(String number) {
		if (number == null) {
			return null;
		}
		try {
			return Double.parseDouble(number);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Object getBoolean(String strValue) {
		if (strValue == null) {
			return null;
		}
		return Boolean.parseBoolean(strValue);
	}

}
