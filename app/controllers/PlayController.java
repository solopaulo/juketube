package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;

import lib.Song;
import lib.playback.Playback;
import lib.playback.PlaybackEvent;
import lib.playback.PlaybackStatus;
import lib.playback.mplayer.MplayerPlayback;
import lib.playlist.PlayList;
import lib.resolver.DownloadResolver;
import lib.resolver.ResolverException;
import play.Logger;
import play.cache.Cache;
import play.data.binding.As;
import play.libs.F.Promise;
import play.modules.spring.Spring;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.StatusCode;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static lib.playback.Playback.*;

public class PlayController extends Controller {
	
	public static void play(Song song) {
		if ( song == null ) {
			Logger.warn("No song provided for playback");
			response.status = new Integer( StatusCode.BAD_REQUEST );
			return;
		} else if ( isEmpty( song.getPlaybackUrl() ) ) {
			Promise<String>promiseUrl = new Promise<String>();
			try {
				promiseUrl.invoke( Spring.getBeanOfType(DownloadResolver.class).resolveUrl(song.getUrl()) );
				song.setPlaybackUrl( await(promiseUrl) );
			} catch (ResolverException rex) {
				Logger.warn("Unable to begin playback for song: %s (%s)",song.getUrl(),rex.getMessage());
				response.status = new Integer( StatusCode.BAD_REQUEST );
				return;
			}
		}
		PlayList playlist = Spring.getBeanOfType(PlayList.class);
		playlist.clear();
		playlist.add(song);
		getPlayback().play(song);
		renderText(song.getPlaybackUrl()); 
	}
	
	public static void playExisting() {
		Playback playback = getPlayback();
		Song song = playback.getCurrentSong();
		if ( song != null  ) {
			playback.stop();
			playback.play(song);
		}
		String songJson = new Gson().toJson( song );
		render("/Application/playback.html",song,songJson);
	}
	
	public static void stop() {		
		Song song = getPlayback().getCurrentSong();
		String songJson = new Gson().toJson( song );
		stopPlayback();
		render("/Application/playback.html",song, songJson);
	}
	
	@Before(only="play")
	private static void stopPlayback() {
		getPlayback().stop();
	}
	
	public static void pause() {
		getPlayback().pause();
		renderText("");
	}
	
	public static void getPlaybackStatus() {
		Song song = getPlayback().getCurrentSong();
		String songJson = new Gson().toJson(song);
		render("/Application/playback.html",song, songJson);
	}
	
	public static void getWSPlaybackStatus() {
		PlaybackStatus ps= getPlayback().getPlaybackStatus();
		renderText("status",ps ); 
	}
	
	public static void getPlaybackPosition() {
		renderText(getPlayback().getPlaybackPositionAsString());
	}
	
	public static void seek(int positionPercent) {
		getPlayback().seek(positionPercent);
		renderText("seek stream to "+positionPercent);
	}
	
	public static void silence() {
		Playback pb = getPlayback();
		MplayerPlayback mp = (MplayerPlayback) pb;
		mp.killAll();
	}
	public static void setVolume(int volume) {
		try {
			getPlayback().setPlaybackVolume(volume);
		} catch (Exception x) {
			Logger.error(x.getMessage());
		}
	}
	
	public static void setRepeatMode(String mode) {
		if ( ! ( REPEAT_NONE.equals(mode) ||
				 REPEAT_ALL.equals(mode) ||
				 REPEAT_TRACK.equals(mode) ) ) {
			return;
		}
		getPlayback().setRepeatMode(mode);
	}
	
	private static Playback getPlayback() {
		return Spring.getBeanOfType( Playback.class );
	}
}
