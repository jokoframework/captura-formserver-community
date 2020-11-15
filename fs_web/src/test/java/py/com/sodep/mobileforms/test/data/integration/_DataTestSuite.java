package py.com.sodep.mobileforms.test.data.integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	
	
BinaryDataIT.class,
DataAccessDefinitionAndInsertIT.class,
DataAccessCommitAndRollbackIT.class,
EditLookupTableIT.class,
LookupTableDataIT.class,
LookupTableIT.class,
TransactionGenerationIT.class,
TransactionSelfHealingIT.class
})
public class _DataTestSuite {

}


