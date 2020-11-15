package py.com.sodep.mobileforms.impl.authorization;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.ProjectDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationConfigurationException;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.core.IAuthorizationService;
import py.com.sodep.mobileforms.impl.AspectUtilities;

@Aspect
public class AuthorizationAspect {

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationAspect.class);

	private static final ThreadLocal<User> requestUser = new ThreadLocal<User>();
	private static final ThreadLocal<Boolean> avoidAuthorizationControl = new ThreadLocal<Boolean>();
	private static final ThreadLocal<Integer> methodLevel = new ThreadLocal<Integer>();

	@Autowired
	private IAuthorizationControlService authControlService;

	@Autowired
	private IAuthorizationService authorizationService;

	/**
	 * If this is set to true the system will throw an
	 * {@link AuthorizationException} if the user doesn't have enough rights. If
	 * it set to false a warning will be logged
	 * 
	 */
	private boolean strictAuthorization = false;

	private boolean failIfUnableToObtainMethod = true;

	public static final void setUserInRequest(User u) {
		requestUser.set(u);
	}

	private Integer increaseMethodLevel() {
		Integer level = methodLevel.get();
		level++;
		methodLevel.set(level);
		return level;
	}

	private Integer decreaseMethodLevel() {
		Integer level = methodLevel.get();
		level--;
		methodLevel.set(level);
		return level;
	}

	private Integer getMethodLevel() {
		Integer level = methodLevel.get();
		return level;
	}

	public static final User getUserInRequest() {
		// This seems to be a cleaner to solution to obtain the information from
		// the session
		// ServletRequestAttributes attr = (ServletRequestAttributes)
		// RequestContextHolder.currentRequestAttributes();
		// attr.getRequest().getSession(true);
		return requestUser.get();
	}

	/**
	 * Call this method to avoid checking the authorization.It will only work
	 * once for a given call
	 * 
	 * @param b
	 */
	public static final void shouldCheckAuthorization(boolean b) {
		avoidAuthorizationControl.set(b);
	}

	public static final boolean shouldCheckAuthorization() {
		Boolean b = avoidAuthorizationControl.get();
		if (b != null) {
			return b;
		}
		return true;
	}

	private void reportAuthorizationException(JoinPoint p, User u, String authorization) {
		Method m = getMethodDeclaration(p);

		logger.error("The user #" + u.getId() + " (" + u.getMail() + ") tried to executed " + m.toString()
				+ " but it doesn't have the authorization " + authorization);
		// we are logging the details of the authorization to help the developer
		// fix the problem (if any).
		// However, the AuthorizationException will just present a generic
		// method to avoid given too much information to a potential hacker
		if (strictAuthorization) {
			throw new AuthorizationException("Can't execute method. Not enough authorization");
		}
	}

	

	private Method getMethodDeclaration(JoinPoint p) {

		try {

			Method method = AspectUtilities.getMethodDeclaration(p);
			return method;
		} catch (SecurityException e) {
			if (failIfUnableToObtainMethod) {
				throw new ApplicationException("Unable to obtain method declaration to inspect it", e);
			} else {
				logger.error("Unable to obtain method declaration to inspect it", e);
			}
			return null;

		} catch (NoSuchMethodException e) {
			if (failIfUnableToObtainMethod) {
				throw new ApplicationException("Unable to obtain method declaration to inspect it", e);
			} else {
				logger.error("Unable to obtain method declaration to inspect it", e);
			}
			return null;
		}

	}

	/**
	 * This method analyze the invoked method and return the annotation of the
	 * {@link Authorizable} annotation of the method. If the method doesn't have
	 * any annotation it will return the {@link Authorizable} from the class
	 * 
	 * @param p
	 * @return an {@link Authorizable} annotation or null if couldn't find one
	 */
	public Authorizable getAuthorizableAnnotation(JoinPoint p) {
		// Method method = getMethodDeclaration(p);
		Method method = getMethodDeclaration(p);
		Authorizable annotation = null;
		if (method != null) {
			// check if the method has an annotation
			annotation = method.getAnnotation(Authorizable.class);
			if (annotation != null) {
				logger.trace("Analyzing " + p.toShortString() + " with method annotation");
			}
		}
		if (annotation == null) {
			// if the method doesn't have an annotation, then check if the
			// class has any
			annotation = p.getTarget().getClass().getAnnotation(Authorizable.class);
			if (annotation != null) {
				logger.trace("Analyzing " + p.toShortString() + " with class annotation");
			}
		}
		return annotation;

	}

	/**
	 * Search for an object of instance of T and return the first found instance
	 * (if any)
	 * 
	 * @param t
	 * @param args
	 * @return an object instance of T or null
	 */
	@SuppressWarnings("unchecked")
	private <T> T getFirstInterestedObject(Class<T> t, Object[] args) {
		for (Object o : args) {
			if (t.isInstance(o)) {
				return (T) o;
			}
		}
		return null;
	}

	boolean hasProjectAccess(Authorizable annotation, Authorization authorization, User user, JoinPoint p) {
		Object[] args = p.getArgs();
		if (annotation.projectParam() >= 0) {
			// the user has provided the parameter to check with
			if (annotation.projectParam() < args.length) {
				Object projObj = args[annotation.projectParam()];
				if ((projObj instanceof Project) || (projObj instanceof ProjectDTO) || (projObj instanceof Long)) {
					if (projObj instanceof Project) {
						Project project = (Project) projObj;
						return authControlService.has(project, user, authorization.getName());
					} else {
						Long projectId;
						if (projObj instanceof ProjectDTO) {
							ProjectDTO dto = (ProjectDTO) projObj;
							projectId = dto.getId();
						} else {
							projectId = (Long) projObj;
						}
						return authControlService.hasProjectLevelAccess(projectId, user, authorization.getName());
					}
				} else {
					throw new AuthorizationConfigurationException("The parameter " + annotation.projectParam()
							+ " of the method " + p.toShortString() + " is neither a " + Project.class.getName() + ", "
							+ ProjectDTO.class.getName() + " nor a Long ");
				}
			} else {
				throw new AuthorizationConfigurationException("The method " + p.toLongString()
						+ " claimed that the project is on parameter " + annotation.projectParam()
						+ " but the method only has " + args.length + " parameters.");
			}
		} else {
			// the user didn't provide explicitly the Project. We will look
			// for one
			Project project = getFirstInterestedObject(Project.class, args);
			if (project != null) {
				return authControlService.has(project, user, authorization.getName());
			} else {
				logger.error("Couldn't find a Project object on the method " + p.toLongString()
						+ ". Consider using projectParam. For example: Authorizable(value=\"xxx\",projectParam=2) ");
				return false;
			}
		}
	}

	private boolean hasFormAccess(Authorizable annotation, Authorization auth, User user, ProceedingJoinPoint p) {
		Object[] args = p.getArgs();
		if (annotation.formParam() >= 0) {
			// the user has provided the parameter to check with
			if (annotation.formParam() < args.length) {
				Object formObj = args[annotation.formParam()];
				if ((formObj instanceof Form) || (formObj instanceof FormDTO) || (formObj instanceof Long)) {
					if (formObj instanceof Form) {
						Form form = (Form) formObj;
						return authControlService.has(form, user, auth.getName());
					} else {
						Long formId = null;
						if (formObj instanceof FormDTO) {
							formId = ((FormDTO) formObj).getId();
						} else {
							formId = (Long) formObj;
						}
						return authControlService.hasFormLevelAccess(formId, user, auth.getName());
					}
				} else {
					throw new AuthorizationConfigurationException("The parameter " + annotation.formParam()
							+ " of the method " + p.toShortString() + " is neither a " + Form.class.getName() + ", "
							+ FormDTO.class.getName() + " nor a Long ");
				}
			} else {
				throw new AuthorizationConfigurationException("The method " + p.toLongString()
						+ " claimed that the form is on parameter " + annotation.formParam()
						+ " but the method only has " + args.length + " parameters.");
			}
		} else {
			// the user didn't provide explicitly the Form. We will look
			// for one
			Form form = getFirstInterestedObject(Form.class, args);
			if (form != null) {
				return authControlService.has(form, user, auth.getName());
			} else {
				logger.error("Couldn't find a Form object on the method " + p.toLongString()
						+ ". Consider using formParam. For example: Authorizable(value=\"xxx\",formParam=2) ");
				return false;
			}
		}

	}

	boolean hasAppAccess(Authorizable annotation, Authorization authorization, User user, JoinPoint p) {
		Object[] args = p.getArgs();
		if (annotation.applicationParam() >= 0) {
			// the user has provided the parameter to check with
			if (annotation.applicationParam() < args.length) {
				Object appObj = args[annotation.applicationParam()];
				if ((appObj instanceof Application) || (appObj instanceof Long)) {
					if ((appObj instanceof Application)) {
						Application app = (Application) appObj;
						return authControlService.has(app, user, authorization.getName());
					} else {
						return authControlService.hasAppLevelAccess((Long) appObj, user, authorization.getName());
					}
				} else {
					throw new AuthorizationConfigurationException("The parameter " + annotation.applicationParam()
							+ " of the method " + p.toShortString() + " is neither an " + Application.class.getName()
							+ " nor a Long ");
				}
			} else {
				throw new AuthorizationConfigurationException("The method " + p.toLongString()
						+ " claimed that the application is on parameter " + annotation.applicationParam()
						+ " but the method only has " + args.length + " parameters.");
			}
		} else {
			// the user didn't provide explicitly the application. We will look
			// for one
			Application app = getFirstInterestedObject(Application.class, args);
			if (app != null) {
				return authControlService.has(app, user, authorization.getName());
			} else {
				logger.trace("Couldn't find an application object on the method "
						+ p.toLongString()
						+ ". Consider using applicationParam. For example: Authorizable(value=\"xxx\",applicationParam=2) ");
				return false;
			}

		}
	}

	@Around(value = "execution(* py.com.sodep.mobileforms.api..*.*(..)) ")
	public Object authorizationWrapper(ProceedingJoinPoint p) throws Throwable {
		// TODO throw exception if the target is baseService or a method of
		// baseservice that has not been overwritten
		Integer level = getMethodLevel();
		if (getMethodLevel() == null) {
			methodLevel.set(0);
			level = 0;
		} else {
			level = increaseMethodLevel();
		}
		if (shouldCheckAuthorization()) {
			checkAuthorization(p);
		}
		Object o = p.proceed();
		// the next time the authorization should be checked
		if (level == 0) {
			// Only the first call will put the "check authorization" back to
			// true. This way a call to shouldCheckAuthorization(false) will
			// have effect in the inner server calls
			shouldCheckAuthorization(true);
		}
		level = decreaseMethodLevel();

		return o;

	}

	private void checkAuthorization(ProceedingJoinPoint p) {
		long startAnalyzing = System.currentTimeMillis();
		User user = getUserInRequest();

		Authorizable annotation = getAuthorizableAnnotation(p);
		boolean hasAccess = false;
		if (annotation != null) {
			logger.trace("Analyzing access to " + p.toShortString() + " for " + user + " with annotation");
			String authName = annotation.value();
			if (annotation.checkType().equals(Authorizable.CHECK_TYPE.CHECK_AUTH)) {
				Authorization auth = null;
				if (authName != null) {
					logger.trace("Checking simple authorization access " + authName);
					auth = authorizationService.get(authName);
					hasAccess = hasAccess(p, user, annotation, auth);
				} else if (annotation.authorizations().length > 0) {
					logger.trace("Checking multiple authorization with " + annotation.condition().toString());

					Authorizable.CONDITION condition = annotation.condition();

					for (int i = 0; i < annotation.authorizations().length; i++) {
						auth = authorizationService.get(annotation.authorizations()[i]);
						boolean innerHasAccess = hasAccess(p, user, annotation, auth);
						if (condition.equals(Authorizable.CONDITION.AND)) {
							// needs to test until someone is false or all
							// required authorization are true
							if (innerHasAccess) {
								hasAccess = true;
							} else {
								hasAccess = false;
								i = annotation.authorizations().length;
							}

						} else {
							if (innerHasAccess) {
								hasAccess = true;
								i = annotation.authorizations().length;
							}
						}
					}

				}

			} else {
				// grant access to annotation that are marked with
				// uthorizable.CHECK_TYPE.NONE
				hasAccess = true;
			}
			if (!hasAccess) {
				reportAuthorizationException(p, user, authName);
			} else {
				startAnalyzing = System.currentTimeMillis() - startAnalyzing;
				logger.trace("Authrorization granted to " + p.toShortString() + " for user " + user + " in "
						+ startAnalyzing + " ms. ");
			}
		} else {
			if (strictAuthorization) {
				throw new AuthorizationConfigurationException("Unable to determine authorization for "
						+ p.toLongString());
			} else {
				logger.warn("Unable to determine authorization for " + p.toShortString() + " on target "
						+ p.getTarget().getClass());
			}
		}
	}

	private boolean hasAccess(ProceedingJoinPoint p, User user, Authorizable annotation, Authorization auth) {
		boolean hasAccess = false;
		if (auth != null) {
			switch (auth.getLevel()) {

			case Authorization.LEVEL_APP:
				hasAccess = hasAppAccess(annotation, auth, user, p);
				break;
			case Authorization.LEVEL_PROJECT:
				hasAccess = hasProjectAccess(annotation, auth, user, p);
				break;
			case Authorization.LEVEL_FORM:
				hasAccess = hasFormAccess(annotation, auth, user, p);
				break;
			case Authorization.LEVEL_POOL:

			}
			if (!hasAccess) {
				// if it couldn't obtain access at a lower level will
				// try at system level
				hasAccess = authControlService.has(user, auth.getName());
			}
		}
		return hasAccess;
	}

}
