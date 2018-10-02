package jp.tankofu.meshi.tw.data;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// see https://developer.twitter.com/en/docs/tweets/curate-a-collection/api-reference/get-collections-entries
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionEntries {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Tweet {
		private String id;

		@JsonProperty("id")
		public void setId(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TimelineEntry {
		private Tweet tweet;

		public void setTweet(Tweet tweet) {
			this.tweet = tweet;
		}

		public Tweet getTweet() {
			return this.tweet;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Response {

		private List<TimelineEntry> timeline;

		@JsonProperty("timeline")
		public void setTweets(List<TimelineEntry> timeline) {
			this.timeline = timeline;
		}
	}

	private Response response;

	@JsonProperty("response")
	public void setResponse(Response response) {
		this.response = response;
	}

	public List<Tweet> getCollectionEntries() {
		return response.timeline.stream().map(TimelineEntry::getTweet).collect(Collectors.toList());
	}
}
