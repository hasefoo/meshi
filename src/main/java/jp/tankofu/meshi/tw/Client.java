package jp.tankofu.meshi.tw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.tankofu.meshi.tw.data.CollectionAddResult;
import jp.tankofu.meshi.tw.data.QueryResult;
import jp.tankofu.meshi.tw.data.StatusInfo;
import jp.tankofu.meshi.util.Utils;

public class Client {
	@FunctionalInterface
	private interface ResponseHandler<T> {
		T consumeContent(HttpResponse response) throws IOException, TwApiException;
	}

	@FunctionalInterface
	private interface ErrorHandler {
		TwApiException createTwApiException(HttpResponse response) throws IOException;
	}

	public static final String USER_AGENT = "Meshi/1.0";

	public static final String SEARCH_URL = "https://api.twitter.com/1.1/search/tweets.json";
	public static final String COLLECTION_ADD_URL = "https://api.twitter.com/1.1/collections/entries/add.json";

	private final AuthInfo authInfo;

	public Client(AuthInfo authInfo) {
		this.authInfo = authInfo;
	}

	public List<StatusInfo> query(String queryStr) throws IOException, TwApiException {
		return query(queryStr, Collections.emptyMap());
	}

	public List<StatusInfo> query(String queryStr, Map<String, String> options) throws IOException, TwApiException {
		Map<String, String> defaultOption = Map.of("result_type", "recent");

		Map<String, String> queryParams = new HashMap<>(defaultOption);
		queryParams.putAll(options);
		queryParams.put("q", queryStr);

		return executeApiGet(SEARCH_URL, queryParams, this::listStatuses, this::constractTwApiException);
	}

	public CollectionAddResult addCollection(String collectionId, String tweetId) throws TwApiException, IOException {
		return addCollection(collectionId, tweetId, Collections.emptyMap());
	}

	public CollectionAddResult addCollection(String collectionId, String tweetId, Map<String, String> options)
			throws TwApiException, IOException {
		Map<String, String> queryParams = new HashMap<>(options);
		queryParams.put("id", collectionId);
		queryParams.put("tweet_id", tweetId);

		return executeApiSimplePost(COLLECTION_ADD_URL, queryParams, this::checkCollectionAddResult,
				this::constractTwApiException);
	}

	public <T> T executeApiGet(String url, Map<String, String> queryParams, ResponseHandler<T> contentHandler,
			ErrorHandler errorHandler) throws TwApiException, IOException {
		HttpGet httpGet = new HttpGet(joinUrlAndParams(url, queryParams));
		httpGet.addHeader(createAuthenticationHeader(url, "GET", queryParams));
		return executeSimpleRequest(httpGet, contentHandler, errorHandler);
	}

	public <T> T executeApiSimplePost(String url, Map<String, String> queryParams, ResponseHandler<T> contentHandler,
			ErrorHandler errorHandler) throws TwApiException, IOException {
		HttpPost httpPost = new HttpPost(joinUrlAndParams(url, queryParams));
		httpPost.addHeader(createAuthenticationHeader(url, "POST", queryParams));
		return executeSimpleRequest(httpPost, contentHandler, errorHandler);
	}

	public <T> T executeSimpleRequest(HttpRequestBase requestBase, ResponseHandler<T> contentHandler,
			ErrorHandler errorHandler) throws TwApiException, IOException {
		try (CloseableHttpClient client = createClient()) {
			try (CloseableHttpResponse response = client.execute(requestBase)) {
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					return contentHandler.consumeContent(response);
				}
				throw errorHandler.createTwApiException(response);
			}
		}
	}

	public List<StatusInfo> listStatuses(HttpResponse response) throws IOException {
		try (InputStream in = response.getEntity().getContent()) {
			return new ObjectMapper().readValue(in, QueryResult.class).getStatuses();
		}
	}

	public CollectionAddResult checkCollectionAddResult(HttpResponse response) throws IOException {
		try (InputStream in = response.getEntity().getContent()) {
			return new ObjectMapper().readValue(in, CollectionAddResult.class);
		}
	}

	public TwApiException constractTwApiException(HttpResponse response) throws IOException {
		String statusLine = response.getStatusLine().toString();
		StringBuilder message = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				message.append(line).append("\n");
			}
		}
		if (message.length() != 0) {
			message.deleteCharAt(message.length() - 1); // remove tail LF
		}

		return new TwApiException(statusLine, message.toString());
	}

	private Header createAuthenticationHeader(String url, String method, Map<String, String> queryParams) {
		AuthHeaderBuilder authHeaderBuilder = new AuthHeaderBuilder(authInfo);
		authHeaderBuilder.setMethod(method);
		authHeaderBuilder.setUrl(url);
		for (Map.Entry<String, String> param : queryParams.entrySet()) {
			authHeaderBuilder.putRequestParam(param.getKey(), param.getValue());
		}
		return authHeaderBuilder.build();
	}

	private String joinUrlAndParams(String url, Map<String, String> queryParams) {
		String paramsStr = queryParams.entrySet().stream()
				.map(entry -> Utils.percentEncode(entry.getKey()) + "=" + Utils.percentEncode(entry.getValue()))
				.collect(Collectors.joining("&"));
		return url + "?" + paramsStr;
	}

	public CloseableHttpClient createClient() {
		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		return HttpClients.custom().setDefaultRequestConfig(requestConfig).setUserAgent(USER_AGENT).build();
	}
}
