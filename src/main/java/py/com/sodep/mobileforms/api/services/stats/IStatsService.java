package py.com.sodep.mobileforms.api.services.stats;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.stats.DataUsage;
import py.com.sodep.mobileforms.api.dtos.stats.FailedDocument;
import py.com.sodep.mobileforms.api.dtos.stats.UsageStats;

public interface IStatsService {
	
	UsageStats getAppUsageStats(Long appId);

	List<DataUsage> getAllAppsDataUsage(Boolean _search, String searchValue, Integer page, Integer rows, String orderBy, String order);
	
	List<FailedDocument> getFailedDocuments(int lastNDays);
	
}
