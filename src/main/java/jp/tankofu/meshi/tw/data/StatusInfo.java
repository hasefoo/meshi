package jp.tankofu.meshi.tw.data;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusInfo {

	private String createdAt;

	private long id;

	private String idStr;

	private boolean retweeted;

	private boolean favorited;

	public String getCreatedAt() {
		return createdAt;
	}

	@JsonProperty("created_at")
	public void setCreateAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public long getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(long id) {
		this.id = id;
	}

	public String getIdStr() {
		return idStr;
	}

	@JsonProperty("id_str")
	public void setIdStr(String idStr) {
		this.idStr = idStr;
	}

	public boolean isRetweeted() {
		return retweeted;
	}

	@JsonProperty("retweeted")
	public void setRetweeted(boolean retweeted) {
		this.retweeted = retweeted;
	}

	public boolean isFavorited() {
		return favorited;
	}

	@JsonProperty("favorited")
	public void setFavorited(boolean favorited) {
		this.favorited = favorited;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
