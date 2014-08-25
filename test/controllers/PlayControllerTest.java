package controllers;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import lib.JsonResponse;
import lib.Song;
import lib.playback.PlaybackStatus;

import org.junit.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import play.Logger;
import play.mvc.Http.Response;
import play.mvc.Http.StatusCode;
import play.test.*;

public class PlayControllerTest extends FunctionalTest {
	private static final String URL_PLAYBACK_PLAY 		= "/play";
	private static final String URL_PLAYBACK_STOP 		= "/stop";
	private static final String URL_PLAYBACK_PAUSE 		= "/pause";
	private static final String URL_PLAYBACK_POSITION	= "/playback/position";
	private static final String URL_PLAYBACK_STATUS		= "/playback/status";
	private static final String PARAM_PLAY_SONG = "song";
	
	
	@Test
	public void testPostPlayWithNoSong() {
		// calling static void play()
		Response response = POST(URL_PLAYBACK_PLAY);
		Object o = renderArgs(PARAM_PLAY_SONG);
		assertNull(o);
		assertStatus(StatusCode.BAD_REQUEST,response);		
	}

	@Test
	public void testGetPlay() {
		Response response = GET(URL_PLAYBACK_PLAY);
		assertIsOk(response);
	}
	
	@Test
	public void testGetStop() {
		Response response = GET(URL_PLAYBACK_STOP);
		assertIsOk(response);
	}
	
	@Test
	public void testGetPause() {
		
	}
	
	@Test
	public void testGetSeek() {
		
	}
	
	@Test
	public void testPostStatus() {
		Response response = POST(URL_PLAYBACK_STATUS);
		assertIsOk(response);
		Type jsonType = new TypeToken<JsonResponse>(){}.getType();
		JsonResponse status = new Gson().fromJson(String.format("[%s]",getContent(response)),jsonType );
		Logger.info("status is %s", status.toJson());
		assertNotNull(status);
		assertTrue( status instanceof JsonResponse );
	}
	
}
