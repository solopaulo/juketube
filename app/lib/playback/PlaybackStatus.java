package lib.playback;

public class PlaybackStatus {
	private String 	playbackTime;
	private int 	playbackPosition;
	private String 	playbackTitle;
	private boolean	playbackPlaying;
	private int playbackDuration;
	private int playbackVolume;
	private String repeatMode;
	private boolean noSong;
	
	public String getPlaybackTime() {
		return playbackTime;
	}
	public void setPlaybackTime(String playbackTime) {
		this.playbackTime = playbackTime;
	}
	public int getPlaybackPosition() {
		return playbackPosition;
	}
	public void setPlaybackPosition(int playbackPosition) {
		this.playbackPosition = playbackPosition;
	}
	public String getPlaybackTitle() {
		return playbackTitle;
	}
	public void setPlaybackTitle(String playbackTitle) {
		this.playbackTitle = playbackTitle;
	}
	public boolean isPlaybackPlaying() {
		return playbackPlaying;
	}
	public void setPlaybackPlaying(boolean playbackPlaying) {
		this.playbackPlaying = playbackPlaying;
	}
	
	public String toString() {
		return "Time: "+getPlaybackTime()+"; Playing: "+isPlaybackPlaying()+"; Title: "+getPlaybackTitle();
	}
	public int getPlaybackDuration() {
		return playbackDuration;
	}
	public void setPlaybackDuration(int playbackDuration) {
		this.playbackDuration = playbackDuration;
	}
	public int getPlaybackVolume() {
		return playbackVolume;
	}
	public void setPlaybackVolume(int playbackVolume) {
		this.playbackVolume = playbackVolume;
	}
	public String getRepeatMode() {
		return repeatMode;
	}
	public void setRepeatMode(String repeatMode) {
		this.repeatMode = repeatMode;
	}
	public boolean isNoSong() {
		return noSong;
	}
	public void setNoSong(boolean noSong) {
		this.noSong = noSong;
	}
}
