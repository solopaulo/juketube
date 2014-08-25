package lib.playback;

import lib.Song;

public interface Playback {
	public static final String REPEAT_NONE			= "";
	public static final String REPEAT_ALL			= "all";
	public static final String REPEAT_TRACK			= "1-track";

	public void play(Song song);
	public void stop();
	public boolean pause();
	public boolean isPlaying();
	public Song getCurrentSong();
	public int getPlaybackPosition() throws Exception;
	public String getPlaybackPositionAsString();
	public PlaybackStatus getPlaybackStatus();
	public void setPlaybackVolume(int volumePercent) throws Exception;
	public void setRepeatMode(String repeatMode);
	public String getRepeatMode();
	public int getPlaybackVolume();
	public void seek(int positionPercent);
	public void addListener(PlaybackEventListener listener);
	public void removeListener(PlaybackEventListener listener);
	public void dispatchEvent(PlaybackEvent event);
}
