package controllers;

import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import static org.apache.commons.lang.StringUtils.isEmpty;
import lib.playback.Playback;
import lib.playback.PlaybackStatus;
import lib.playback.mplayer.MplayerException;
import play.Logger;
import play.modules.spring.Spring;
import play.mvc.WebSocketController;
import play.mvc.Http.*;
import play.libs.Codec;
import play.libs.F.*;
import static play.mvc.Http.WebSocketFrame.*;

public class WSPlayController extends WebSocketController {

	public static void getPlaybackPosition() {
		Promise<WebSocketEvent>promise = null;
		breaker:
		while(inbound.isOpen()) {
			promise = inbound.nextEvent();
			while ( outbound.isOpen() && !promise.isDone() ) {
				try {
					outbound.send( getPlaybackTime() );
				} catch (Exception x) {
					break breaker;
				} 
				await(750);						
			}
			WebSocketEvent event = await(promise);
			
			for (String quit: TextFrame.and( Matcher.Equals("quit") ).match(event))  {
				break breaker;
			}
		}		 
		inbound.close();
		outbound.close();
		Logger.info("Websocket thread ended");
	}
	
	public static void getPlaybackStatus() {
		Promise<WebSocketEvent>promise = null;
		try {
		breaker:
			while(inbound.isOpen()) {
				promise = inbound.nextEvent();
				while ( outbound.isOpen() && !promise.isDone() ) {
					try {
						PlaybackStatus status = getPlayback().getPlaybackStatus();
						outbound.send( new Gson().toJson(status) );
					} catch (Exception x) {
						break breaker;
					} 
					await(750);		
					
				}
				WebSocketEvent event = await(promise);
				for (String quit: TextFrame.and( Matcher.Equals("quit") ).match(event))  {
					break breaker;
				}
			}		 
			inbound.close();
			outbound.close();
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
	}
	
	private static String getPlaybackTime() {
		 return getPlayback().getPlaybackPositionAsString();
	}
	
	private static Playback getPlayback() {
		return Spring.getBeanOfType( Playback.class );
	}
}
