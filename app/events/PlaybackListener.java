package events;

import lib.Song;

public interface PlaybackListener {
	public Song playbackStopped();
	public Song playbackStarted();
	public Song skippedForward();
	public Song skippedBackward();
}
