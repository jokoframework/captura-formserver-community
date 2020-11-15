package py.com.sodep.mobileforms.api.services.auth;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Authorizable {
	public static enum CHECK_TYPE {
		NONE, CHECK_AUTH
	};

	public static enum CONDITION {
		AND, OR
	};

	public String value() default "";

	public int applicationParam() default -1;

	public int projectParam() default -1;

	public int formParam() default -1;

	public int poolParam() default -1;

	public String[] authorizations() default {};

	public CHECK_TYPE checkType() default CHECK_TYPE.CHECK_AUTH;
	public CONDITION condition() default CONDITION.AND;
}
