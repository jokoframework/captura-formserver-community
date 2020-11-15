package py.com.sodep.mobileforms.api.services.auth;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation that can be use to logically group the authorizations
 * 
 * @author danicricco
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorizationGroup {
	public String value();
	public int position() default 0;
}
