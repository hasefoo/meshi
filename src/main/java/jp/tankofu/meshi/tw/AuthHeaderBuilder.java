package jp.tankofu.meshi.tw;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import jp.tankofu.meshi.util.Utils;

public class AuthHeaderBuilder {
	public static final String SIGNATURE_METHOD = "HMAC-SHA1";

	private RandomGenerator random;
	private final AuthInfo info;
	private final SortedMap<String, String> requestParams = new TreeMap<>();

	private String nonce;
	private String method;
	private String url;
	private String timestampSec;

	public AuthHeaderBuilder(AuthInfo info, RandomGenerator random) {
		this.info = info;
		this.random = random;
	}

	public AuthHeaderBuilder(AuthInfo info) {
		this(info, null);
	}

	public void putRequestParam(String name, String value) {
		requestParams.put(name, value);
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setTimestampSec(String timestampSec) {
		this.timestampSec = timestampSec;
	}

	public Header build() {
		String timestampSec = takeTimestampSec();
		SortedMap<String, String> authParams = generateNoSignatureAuthParameters(timestampSec);

		String signature = generateSignature(method, url, authParams, requestParams, info.getSigningKey());
		authParams.put("oauth_signature", signature);

		String authValue = generateAuthValue(authParams);
		return new BasicHeader("Authorization", authValue);
	}

	public String generateAuthValue(SortedMap<String, String> authParams) {
		// see
		// https://developer.twitter.com/en/docs/basics/authentication/guides/authorizing-a-request
		String paramString = authParams.entrySet().stream()
				// format key="value"
				.map(entry -> Utils.percentEncode(entry.getKey())
						+ "=\"" + Utils.percentEncode(entry.getValue())
						+ "\"")
				// delimiter and space ", "
				.collect(Collectors.joining(", "));
		return "OAuth " + paramString;
	}

	public static String generateSignature(String method, String url, SortedMap<String, String> authParams,
			SortedMap<String, String> requestParams, String signingKey) {
		String parameterString = createParameterString(authParams, requestParams);
		String signatureBase = new StringBuilder(method)
				.append('&').append(Utils.percentEncode(url))
				.append('&').append(Utils.percentEncode(parameterString))
				.toString();
		String signature = Base64.getEncoder().encodeToString(sign(signingKey, signatureBase));

		return signature;
	}

	private SortedMap<String, String> generateNoSignatureAuthParameters(String timestampSec) {
		return new TreeMap<>(Map.of(
				"oauth_consumer_key", info.getConsumerKey(),
				"oauth_nonce", takeNonce(),
				"oauth_signature_method", SIGNATURE_METHOD,
				"oauth_timestamp", timestampSec,
				"oauth_token", info.getAccessToken(),
				"oauth_version", "1.0"));
	}

	public String takeTimestampSec() {
		if (timestampSec != null) {
			return timestampSec;
		}
		return String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
	}

	public String takeNonce() {
		if (nonce != null) {
			return nonce;
		}
		if (random == null) {
			random = new MersenneTwister();
		}
		byte[] rawNonce = new byte[32];
		random.nextBytes(rawNonce);
		return Base64.getEncoder().encodeToString(rawNonce);
	}

	public static byte[] sign(String key, String signatureBase) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(signatureBase);
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(keySpec);
			return mac.doFinal(signatureBase.getBytes());
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}

	public static String createParameterString(Map<String, String> authParams, Map<String, String> reqParams) {
		SortedMap<String, String> params = new TreeMap<>();
		for (Map.Entry<String, String> entry : authParams.entrySet()) {
			params.put(Utils.percentEncode(entry.getKey()), Utils.percentEncode(entry.getValue()));
		}
		for (Map.Entry<String, String> entry : reqParams.entrySet()) {
			params.put(Utils.percentEncode(entry.getKey()), Utils.percentEncode(entry.getValue()));
		}

		return params.entrySet().stream()
				// format key=value
				.map(entry -> entry.getKey() + "=" + entry.getValue())
				// delimiter ","
				.collect(Collectors.joining("&"));
	}
}
