package lib.playback.mplayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import play.Logger;

import lib.Song;
import lib.playback.Playback;
import lib.playback.PlaybackEvent;
import lib.playback.PlaybackEventListener;
import lib.playback.PlaybackStatus;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static lib.playback.Playback.*; 

@Component("mplayer")
public class MplayerPlayback implements Playback {
	private static final String EOL = "\n\n";
	private static final int 	WAIT_FOR_RESULT		= 100;
	private static final String PLAYBACK_STOP 		= "stop";
	private static final String PLAYBACK_PAUSE 		= "PAUSE";
	private static final String PLAYBACK_SEEK		= "seek";
	private static final String PLAYBACK_POSITION 	= "get_time_pos";
	private static final String PLAYBACK_VOLUME_SET	= "volume";
	private static final String PLAYBACK_VOLUME_GET	= "get_property VOLUME";
	private static final int	SEEK_MODE_PERCENT	= 1;
	
	private List<PlaybackEventListener> listeners = new Vector<PlaybackEventListener>();
	private static Process mplayer = null;
	private static Thread processor = null;
	private static PrintWriter output = null;
	private static Song stateSong = null;
	private static int statePlaybackPosition = 0;
	private static int statePlaybackVolume = 0;
	private static String stateRepeatMode = REPEAT_NONE;
	private static boolean statePaused = false;
	
	private static final String MPLAYER_BIN = "mplayer";
	private static final String MPLAYER_THREAD = "mplayer";
	
	private static final String[] MPLAYER_OPTIONS = new String [] {
		"-slave",	// slave mode to accept command
		"-vo",		// set video mode ...
		"null",		// ... to null output
		"-quiet",	// less verbose output
		"-cache",
		"2048"
	};
	
	public MplayerPlayback() {
	}

	public void send(String command) throws MplayerException {
		send(command,false);
	}
	private void send(String command,boolean override) throws MplayerException {
		if ( mplayer == null ) {
			throw new MplayerException("No mplayer instance to send to");
		}
		
		if ( statePaused && !override) {
			Logger.warn("Ignoring command because mplayer paused: %s", command);
			return;
		}
		
		try {
			output.write(command+EOL);
			output.flush();
		} catch (Exception e) {
			throw new MplayerException("-- Failed to send command: "+command+" - "+e.getMessage());
		}
	}
	
	
	@Override
	public void play(Song song) {
		this.stateSong = song;
		if ( mplayer != null ) {
			return;
		}
		try {
			mplayer = createMplayerInstance(song.getPlaybackUrl());
		} catch (Exception x) {
			Logger.error("Unable to create mplayer instance: "+x.getMessage());
		}
		createOutputReader(mplayer);
		createInputWriter(mplayer);
		dispatchEvent(PlaybackEvent.PLAYBACK_STARTED);
		try {
			send( PLAYBACK_VOLUME_GET );
		} catch (Exception x) {
			Logger.error(x.getMessage());
		}
	}

	@Override
	public void stop() {		
		if ( mplayer == null ) {
			return;
		}
		try {
			if ( output != null ) {
				send(PLAYBACK_STOP);
				try {
					output.close();
					output = null;
				} catch (Exception e) { 
					Logger.error(e.getMessage());
				}
			}
			Logger.info("<-- Exiting mplayer");
			mplayer.waitFor();
			destroyOutputReader();
			statePlaybackPosition = 0;			
		} catch (Exception itsex) {
			itsex.printStackTrace();
		}
		mplayer = null;	
		Logger.info("!-- Stopped mplayer");
	}

	@Override
	public boolean pause() {
		try {
			statePaused = ! statePaused;
			send(PLAYBACK_PAUSE,true);
		} catch (Exception x) {
			Logger.error(x.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public Song getCurrentSong() {
		return stateSong;
	}

	@Override
	public int getPlaybackPosition() throws Exception {
		if ( ! isPlaying() ) {
			return statePlaybackPosition;
		}
		
		try {
			send( PLAYBACK_POSITION );
			Thread.sleep(WAIT_FOR_RESULT);
		} catch (MplayerException mx) {
			throw mx;
		} catch (Exception x) {
			Logger.error(x.getMessage());
			return -1;
		}
		return statePlaybackPosition;
	}

	@Override
	public String getPlaybackPositionAsString() {
		int pp = -1;
		try {
			pp = getPlaybackPosition();
		} catch (Exception x) { }
		
		if ( pp < 0 ) {
			return null;
		}
		int mins = (pp/60);
		int secs = (pp%60);
		return mins+":"+(secs<10?"0":"")+secs;
	}

	private Process createMplayerInstance(String url) throws Exception {
		if ( url == null ) {
			throw new Exception("No url was provided for MPlayer");
		}
		ProcessBuilder pb = new ProcessBuilder();
		ArrayList<String> params = new ArrayList<String>();
		params.add(MPLAYER_BIN);
		CollectionUtils.addAll(params,MPLAYER_OPTIONS);
		params.add(url);
		pb.command(params);
		try {
			Logger.info("url is %s",url);
			pb.redirectErrorStream(true);
			final Process p = pb.start();
			return p;
		} catch (Exception x) {
			x.printStackTrace();
		}
		throw  new RuntimeException("Shit went down");
	}

	private void createOutputReader(final Process p) {
		processor = new Thread(new Runnable() {
			public void run() {
				Logger.info("  --> Starting mplayer reader");
				readOutputFromMplayer(p);
				Logger.info("  <-- Processor thread has ended");
			}
		});
		processor.setName(MPLAYER_THREAD);
		processor.start();		
	}

	private void createInputWriter(final Process p) {
		try {
			output = new PrintWriter(new OutputStreamWriter(p.getOutputStream()));
		} catch (Exception x) {
			Logger.error(x.getMessage());
		}		
	}
	
	private void destroyOutputReader() {
		if ( processor != null ) {
			processor.interrupt();
			processor = null;
		}		
	}
	
	private void readOutputFromMplayer(Process mplayer) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(mplayer.getInputStream()));
			MplayerEvent event = null;
			while ( true ) {
				String line = reader.readLine();
				if ( line == null ) {
					throw new Exception("<-- MPlayer Output thread appears to be finished");
				}
				event = MplayerEvent.createEvent(line);
				if( event != null ) {
					dispatchEvent(event);
				}
				Thread.yield();
			}
		} catch (Exception x) {			
			Logger.error(x.getMessage());
		} finally {
			try {
				reader.close();
				Logger.info("<--  Closed mplayer output reader");
			} catch (Exception x) { 
				Logger.error(x.getMessage());
			}
		}
		dispatchEvent(PlaybackEvent.PLAYBACK_STOPPED);
	}
	
	private void dispatchEvent(MplayerEvent event) {
		if ( event == MplayerEvent.PLAYBACK_POSITION && ! isEmpty(event.getValue()) ) {
			statePlaybackPosition = (int) Float.parseFloat(event.getValue());			
		} else if ( event == MplayerEvent.PLAYBACK_STARTED ) {
			statePaused = false;
		} else if ( event == MplayerEvent.PLAYBACK_STOPPED ) {
			statePaused = true;
		} else if ( event == MplayerEvent.PLAYBACK_VOLUME_CHANGED ) {
			statePlaybackVolume = Math.round( Float.parseFloat(event.getValue() ) );
		}
	}

	@Override
	public boolean isPlaying() {
		return mplayer != null && ! statePaused;
	}

	@Override
	public PlaybackStatus getPlaybackStatus() {
		PlaybackStatus status = new PlaybackStatus();
		Song song = getCurrentSong();
		if ( song == null ) {
			status.setNoSong(true);
			return status;
		}
		status.setPlaybackPlaying( isPlaying() );
		status.setPlaybackPosition(statePlaybackPosition);
		status.setPlaybackDuration(getCurrentSong().getLength());
		status.setPlaybackTime( getPlaybackPositionAsString() );
		status.setPlaybackTitle( getCurrentSong().getTitle() );
		status.setPlaybackVolume( statePlaybackVolume );
		status.setRepeatMode( stateRepeatMode );
		return status;
	}

	@Override
	public void seek(int positionPercent) {
		try {
			String cmd = String.format("%s %d %d",PLAYBACK_SEEK,positionPercent,SEEK_MODE_PERCENT);
			send( cmd );
		} catch (Exception x) {
			Logger.error(x.getMessage());
		}
	}

	@Override
	public void addListener(PlaybackEventListener listener) {
		if ( listeners.contains(listener) ) {
			Logger.warn("Listener already added");
			return;
		}
		listeners.add(listener);
	}

	@Override
	public void removeListener(PlaybackEventListener listener) {
		if ( ! listeners.contains(listener) ) {
			return;
		}
		listeners.remove(listener);
	}

	@Override
	public void dispatchEvent(PlaybackEvent event) {
		for (PlaybackEventListener listener : listeners) {
			listener.eventReceived(event);
		}
	}
	
	public int getPlaybackVolume() {
		return statePlaybackVolume;
	}
	
	public void setPlaybackVolume(int volume) throws Exception {
		if ( ! isPlaying() ) {
			return;
		}
		
		try {
			send( PLAYBACK_VOLUME_SET +" "+volume);
			send( PLAYBACK_VOLUME_GET );
		} catch (MplayerException mx) {
			throw mx;
		} catch (Exception x) {
			Logger.error(x.getMessage());

		}
		return;
	}

	public void killAll() {
		Thread  [] tarray = new Thread[] { };
		Thread.currentThread().enumerate(tarray);
		for (Thread t : tarray) {
			if ( t == null || ! MPLAYER_THREAD.equals( t.getName() ) ) {
				continue;
			}
			t.interrupt();
		}
	}

	@Override
	public void setRepeatMode(String repeatMode) {
		if ( ! ( REPEAT_NONE.equals(repeatMode) ||
				 REPEAT_ALL.equals(repeatMode) ||
				 REPEAT_TRACK.equals(repeatMode) ) ) {
			return;
		}
		
		stateRepeatMode = repeatMode;
	}
	
	@Override
	public String getRepeatMode() {
		return stateRepeatMode;
	}
}
