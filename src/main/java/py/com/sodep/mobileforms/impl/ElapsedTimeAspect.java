package py.com.sodep.mobileforms.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

@Aspect
public class ElapsedTimeAspect {

	private static final Logger logger = LoggerFactory.getLogger(ElapsedTimeAspect.class);

	private static final ThreadLocal<Integer> indentation = new ThreadLocal<Integer>();

	private static final String indent(int indent) {
		String s = "";
		for (int i = 0; i < indent; i++) {
			s += "-";
		}
		return s;

	}

	@Around(value = "execution(* py.com.sodep.mobileforms.api..*.*(..)) ")
	public Object authorizationWrapper(ProceedingJoinPoint p) throws Throwable {
		StopWatch watch = new StopWatch();

		// Method method = AspectUtilities.getMethodDeclaration(p);
		Integer i = indentation.get();
		if (i == null) {
			i = 0;
		}
		i++;
		indentation.set(i);
		watch.start();
		logger.debug(indent(i) + "Executing: " + p.toShortString());
		Object o = p.proceed();
		watch.stop();
		logger.debug(indent(i) + p.toShortString() + " took " + watch.getTotalTimeMillis() + " ms. ");
		i--;
		indentation.set(i);
		return o;
	}
}
