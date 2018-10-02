package jp.tankofu.meshi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.tankofu.meshi.tw.AuthInfo;
import jp.tankofu.meshi.tw.Client;
import jp.tankofu.meshi.tw.TwApiException;
import jp.tankofu.meshi.tw.data.CollectionAddResult;
import jp.tankofu.meshi.tw.data.CollectionEntries;
import jp.tankofu.meshi.tw.data.StatusInfo;
import jp.tankofu.meshi.util.Utils;

/**
 * Meshi Collector.
 * 
 * <p>
 * This class ignore sensitive id order, and assumes that id are ordered time
 * series.
 * </p>
 * 
 * // Ignore sensitive cases.
 * 
 * @author hasegawa
 *
 *
 */
public class Collector {
	private static final Logger logger = LoggerFactory.getLogger(Collector.class);
	private static final String PROPERTIES = "properties.xml";

	private final String query;
	private final String collectionId;
	private final Client client;

	private Collector(AuthInfo authInfo, String query, String collectionId) {
		this.query = query;
		this.collectionId = collectionId;
		this.client = new Client(authInfo);
	}

	private void execute() {
		logger.info("Collector start.");
		int count = 0;
		try {
			List<StatusInfo> statuses = queryKeywordTweets();
			String collectionLastEntryId = getCollectionLastEntryId();
			List<StatusInfo> targets = filterTarget(statuses, collectionLastEntryId);
			for (StatusInfo target : targets) {
				boolean successAddition = addToCollection(target);
				if (successAddition) {
					count++;
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (TwApiException e) {
			logger.error(e.getMessage() + ":" + e.getTwitterMessage(), e);
		} finally {
			logger.info("Collector end, add " + count + " tweets.");
		}
	}

	private List<StatusInfo> queryKeywordTweets() throws IOException, TwApiException {
		List<StatusInfo> statuses = client.query(query);
		Collections.sort(statuses, Comparator.comparingLong(StatusInfo::getId));
		return statuses;
	}

	private String getCollectionLastEntryId() throws TwApiException, IOException {
		CollectionEntries entries = client.getCollectionEntries(collectionId, Map.of("count", "1"));
		List<CollectionEntries.Tweet> tweets = entries.getCollectionEntries();
		if (tweets.isEmpty()) {
			return null;
		}
		return tweets.get(0).getId();
	}

	private List<StatusInfo> filterTarget(List<StatusInfo> statuses, String collectionLastEntryId) {
		if (collectionLastEntryId == null) {
			return new ArrayList<>(statuses);
		}
		long lastEntryId = Long.parseLong(collectionLastEntryId);
		return statuses.stream().filter(info -> Long.compare(info.getId(), lastEntryId) > 0)
				.collect(Collectors.toList());
	}

	private boolean addToCollection(StatusInfo target) throws TwApiException, IOException {
		CollectionAddResult result = client.addCollection(collectionId, target.getIdStr());
		if (result.isSuccess()) {
			logger.info("AddToCollection Success:" + target.getIdStr());
			return true;
		}

		logger.warn("AddToCollection Error:" + target.getIdStr());
		logger.warn(result.getErrors().toString());
		return false;
	}

	public static void main(String[] args) throws Exception {
		AuthInfo authInfo;
		String query;
		String collectionId;
		try {
			String propertiesPath = PROPERTIES;
			if (args.length != 0) {
				propertiesPath = args[0];
			}
			Properties prop = Utils.loadProperties(propertiesPath);
			
			authInfo = new AuthInfo(
					prop.getProperty("consumer-key"),
					prop.getProperty("consumer-secret"),
					prop.getProperty("access-token"),
					prop.getProperty("access-secret"));
			query = Objects.requireNonNull(prop.getProperty("query"), "property query");
			collectionId = Objects.requireNonNull(prop.getProperty("collection-id"), "property collection-id");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			System.exit(1);
			return;
		}

		Collector collector = new Collector(authInfo, query, collectionId);
		collector.execute();
	}
}
