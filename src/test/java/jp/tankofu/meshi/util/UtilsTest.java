package jp.tankofu.meshi.util;

import static org.hamcrest.CoreMatchers.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import jp.tankofu.meshi.util.Utils;

public class UtilsTest {

	private void checkPercentEncode(String original, String expect) {
		Assert.assertThat(Utils.percentEncode(original), is(expect));
	}

	@Test
	public void testPercentEncode() {
		// https://developer.twitter.com/en/docs/basics/authentication/guides/percent-encoding-parameters.html
		checkPercentEncode("Ladies + Gentlemen", "Ladies%20%2B%20Gentlemen");
		checkPercentEncode("An encoded string!", "An%20encoded%20string%21");
		checkPercentEncode("Dogs, Cats & Mice", "Dogs%2C%20Cats%20%26%20Mice");
		checkPercentEncode("☃", "%E2%98%83");
		checkPercentEncode("+ *", "%2B%20%2A");
		checkPercentEncode("-._~", "-._~");
	}

}
