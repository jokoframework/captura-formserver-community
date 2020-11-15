package py.com.sodep.mobileforms.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

public class BeanUtils {

	public static void mapBean(Object origin, Object destination) {
		try {
			PropertyUtils.copyProperties(destination, origin);
		} catch (Exception e) {
			throw new BeanMappingException(e);
		}
	}

	public static <T> T createAndMapBean(Object source, Class<T> clazz) {
		try {
			T dest = clazz.newInstance();
			mapBean(source, dest);
			return dest;
		} catch (Exception e) {
			throw new BeanMappingException(e);
		}
	}

	public static <T, Z> List<T> createAndMapList(List<Z> source, Class<T> clazz) {
		try {
			List<T> res = new ArrayList<T>();
			for (Z s : source) {
				T t = createAndMapBean(s, clazz);
				res.add(t);
			}
			return res;
		} catch (Exception e) {
			throw new BeanMappingException(e);
		}
	}

	/**
	 * This is a method that will search for a given method declaration on the
	 * whole class hierarchy and return the first matching (anyway there must be
	 * only one due to the overriding rules). If there is no method then it will
	 * return null
	 * 
	 * @param c
	 * @param methodName
	 * @param parameters
	 * @return
	 */
	public static Method getMethod(Class<?> c, String methodName, Class<?>[] parameterTypes) {
		Method m = null;
		try {
			m = c.getMethod(methodName, parameterTypes);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			// do nothing we will search on the parent class
		}
		if (m == null) {
			// if no method on this level search on the supper class
			if (c.getSuperclass() != null) {
				return getMethod(c.getSuperclass(), methodName, parameterTypes);
			}
			return null;
		}
		return m;
	}

}
