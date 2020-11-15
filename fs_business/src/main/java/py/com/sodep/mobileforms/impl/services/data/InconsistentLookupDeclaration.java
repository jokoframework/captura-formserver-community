package py.com.sodep.mobileforms.impl.services.data;


/***
 * This is an unexpected exception that can arrise if the PostgreSQL is
 * expecting data from the mongo db, but the {@link DataAccessService} can't
 * find them.
 * The following is a list of situation where this can happen
 * <ul>
 * 	<li> the method  {@link DataAccessService#getDataSetDefinition(String, Long)} can't find the DDL for the lookup table</li>
 * </u>
 * 
 * @author danicricco
 * 
 */
public class InconsistentLookupDeclaration extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
