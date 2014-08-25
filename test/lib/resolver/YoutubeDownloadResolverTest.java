package lib.resolver;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.test.UnitTest;

public class YoutubeDownloadResolverTest extends UnitTest {
	private static final String REAL_URL = "http://www.youtube.com/watch?v=llDikI2hTtk&feature=youtube_gdata";
	private static final String REAL_PLAYBACK_URL_ID = "id=9650e2908da14ed9";
	private DownloadResolver resolver;
	
	@Before
	public void setup() {
		resolver = new YoutubeDownloadResolver();
	}
	
	@After
	public void tearDown() {
		resolver = null;
	}
	
	@Test
	public void testEmptyUrl() {
		try {
			resolver.resolveUrl(null);
			Assert.fail("Exception must be thrown on empty parameter");
		} catch (Exception x) {
			Assert.assertTrue(x instanceof ResolverException);
		}
	}
	
	@Test
	public void testRealUrl() {
		try {
			String url = resolver.resolveUrl(REAL_URL);
			assertTrue(url.indexOf(REAL_PLAYBACK_URL_ID) >= 0);
		} catch (Exception x) {
			Assert.fail("Failed to resolve url");
		}
	}

}
