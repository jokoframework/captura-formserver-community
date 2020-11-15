package py.com.sodep.mobileforms.impl;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

public class AspectUtilities {

	public static Method getMethodDeclaration(JoinPoint p) throws SecurityException, NoSuchMethodException {
		MethodSignature signature = (MethodSignature) p.getSignature();
		Method method = signature.getMethod();
		if (method.getDeclaringClass().isInterface()) {
			method = p.getTarget().getClass().getMethod(p.getSignature().getName(), method.getParameterTypes());
		}
		return method;
	}

}
