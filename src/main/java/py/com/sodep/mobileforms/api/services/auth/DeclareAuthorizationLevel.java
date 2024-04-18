package py.com.sodep.mobileforms.api.services.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark the class that will contain all the
 * authorizations that belong to a given level
 * 
 * @author danicricco
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DeclareAuthorizationLevel {
	int level();
	String prefix();
	String column();
}
