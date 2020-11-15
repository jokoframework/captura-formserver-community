package py.com.sodep.mobileforms.api.services.dynamicvalues;

import java.util.List;

public interface IDynamicValuesService {

	List<String> changesDefaultValue(Long elementId);

	List<String> defaultValueDependsOn(Long elementId);

	List<String> changesValueList(Long elementId);

	List<String> valuesDependOn(Long elementId);

}
