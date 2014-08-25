package lib.playback.mplayer;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

import play.test.UnitTest;

public class MplayerEventTest extends UnitTest {

	@Test
	public void testPlaybackPosition() {
		String line = "ANS_TIME_POSITION=382.0\r\n	";
		MplayerEvent event = MplayerEvent.createEvent(line);
		Assert.assertNotNull(event);
		Assert.assertEquals(event,MplayerEvent.PLAYBACK_POSITION);
		Assert.assertEquals("382.0",event.getValue());
	}
	
	@Test
	public void testVolumeChanged() {
		String line = "ANS_VOLUME=51.2\r\n	";
		MplayerEvent event = MplayerEvent.createEvent(line);
		Assert.assertNotNull(event);
		Assert.assertEquals(event,MplayerEvent.PLAYBACK_VOLUME_CHANGED);
		Assert.assertEquals("51.2",event.getValue());
	}

}
