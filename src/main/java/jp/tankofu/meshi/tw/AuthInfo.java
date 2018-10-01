package jp.tankofu.meshi.tw;

import java.util.Objects;

public class AuthInfo {
	private final String consumerKey;
	private final String consumerSecret;
	private final String accessToken;
	private final String accessSecret;

	public AuthInfo(String consumerKey, String consumerSecret, String accessToken, String accessSecret) {
		this.consumerKey = Objects.requireNonNull(consumerKey);
		this.consumerSecret = Objects.requireNonNull(consumerSecret);
		this.accessToken = Objects.requireNonNull(accessToken);
		this.accessSecret = Objects.requireNonNull(accessSecret);
	}

	public String getConsumerKey() {
		return this.consumerKey;
	}

	public String getAccessToken() {
		return this.accessToken;
	}

	public String getSigningKey() {
		return consumerSecret + "&" + accessSecret;
	}
}
