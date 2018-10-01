package jp.tankofu.meshi;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.tankofu.meshi.tw.AuthInfo;
import jp.tankofu.meshi.tw.Client;
import jp.tankofu.meshi.tw.TwApiException;
import jp.tankofu.meshi.tw.data.CollectionAddResult;
import jp.tankofu.meshi.tw.data.StatusInfo;
import jp.tankofu.meshi.util.Utils;

public class Collector {
	private static final Logger logger = LoggerFactory.getLogger(Collector.class);
	private static final String PROPERTIES = "properties.xml";

	private final AuthInfo authInfo;
	private final String query;
	private final String collectionId;

	private Collector(AuthInfo authInfo, String query, String collectionId) {
		this.authInfo = authInfo;
		this.query = query;
		this.collectionId = collectionId;
	}

	private List<StatusInfo> collect() throws IOException, TwApiException {
		Client client = new Client(authInfo);
		List<StatusInfo> statuses = client.query(query);

		statuses.forEach(System.out::println);

		return statuses;
	}

	private void execute() {
		try {
			List<StatusInfo> statuses = collect();
			List<StatusInfo> targets = filterTarget(statuses);
			// List<StatusInfo> addSucceaddToCollection(targets);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (TwApiException e) {
			logger.error(e.getMessage() + ":" + e.getTwitterMessage(), e);
		} finally {

		}
	}

	private List<StatusInfo> filterTarget(List<StatusInfo> statuses) {
		// TODO
		return statuses;
	}

	public static void main(String[] args) throws Exception {

		Properties prop = Utils.loadProperties(PROPERTIES);
		logDebugProperties(logger, prop);

		AuthInfo authInfo = new AuthInfo(prop.getProperty("consumer-key"), prop.getProperty("consumer-secret"),
				prop.getProperty("access-token"), prop.getProperty("access-secret"));
		String query = prop.getProperty("query");
		String collectionId = prop.getProperty("collection-id");

		Collector collector = new Collector(authInfo, query, collectionId);
		// collector.execute();

		Client client = new Client(authInfo);
		CollectionAddResult result = client.addCollection(collectionId, "1046712502233456640");
		System.out.println(result.isSuccess());
		System.out.println(result.getErrors());
		System.out.println(result);
	}

	public static void logDebugProperties(Logger logger, Properties prop) {
		for (Object key : prop.keySet()) {
			logger.debug("key=" + key + ", value=" + prop.get(key));
		}
	}
}
