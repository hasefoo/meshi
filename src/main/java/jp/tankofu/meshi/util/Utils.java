package jp.tankofu.meshi.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

/**
 * tankofu meshi utilities.
 * 
 * @author hasegawa
 *
 */
public class Utils {
	private Utils() {
	}

	/**
	 * Load properties.
	 * 
	 * @param path path to properties.xml
	 * @return properties object.
	 * @throws IOException
	 */
	public static Properties loadProperties(String path) throws IOException {
		Objects.requireNonNull(path);
		try (InputStream in = Files.newInputStream(Paths.get(path))) {
			Properties prop = new Properties();
			prop.loadFromXML(in);
			return prop;
		}
	}

	public static String percentEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8)
				.replace("+", "%20")
				.replace("*", "%2A")
				.replace("%7E", "~");
	}
}
