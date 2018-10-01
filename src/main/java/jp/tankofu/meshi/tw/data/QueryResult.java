package jp.tankofu.meshi.tw.data;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryResult {
	private List<StatusInfo> statuses;

	private Map<String, Object> searchMetadata;

	public List<StatusInfo> getStatuses() {
		return statuses;
	}

	@JsonProperty("statuses")
	public void setStatuses(List<StatusInfo> statuses) {
		this.statuses = statuses;
	}

	public Map<String, Object> getSearchMetadata() {
		return searchMetadata;
	}

	@JsonProperty("search_metadata")
	public void setSearchMetadata(Map<String, Object> searchMetadata) {
		this.searchMetadata = searchMetadata;
	}
}
