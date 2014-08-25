package lib;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import play.Logger;

import lib.playback.Playback;
import lib.playback.PlaybackEvent;
import lib.playback.PlaybackEventListener;
import lib.playback.PlaybackFactory;
import static org.apache.commons.lang.StringUtils.isEmpty;
public class PlayListImpl implements PlayList,PlaybackEventListener {
	private static PlayList playListInstance;
	static {
		playListInstance = new PlayListImpl();
		PlaybackFactory.createPlaybackInstance().addListener((PlaybackEventListener) playListInstance);
	}
	public static PlayList createInstance() {
		return playListInstance;
	}
	
	/* --- */
	
	private List<PlayListItem>playlistItems = new Vector<PlayListItem>();
	
	private String currentId;
	
	@Override
	public List<PlayListItem> get() {
		return playlistItems;
	}

	@Override
	public PlayListItem add(Song song) {
		if ( song == null ) {
			return null;
		}
		PlayListItem item = new PlayListItem();
		item.setSong(song);
		playlistItems.add( item );
		return item;
	}

	@Override
	public void remove(PlayListItem item) {
		if ( ! playlistItems.contains(item) ) {
			return;
		}
		
		playlistItems.remove(item);
	}

	@Override
	public void clear() {
		playlistItems.clear();
	}

	@Override
	public void eventReceived(PlaybackEvent event) {
		String message = event.toString(); 
		if ( event == PlaybackEvent.PLAYBACK_STOPPED ) {
			nextItem();
		}
		Logger.info(message);
	}

	@Override
	public void nextItem() {
		Logger.info("Calling next item; currentId = "+currentId);
		if ( playlistItems.size() == 0 ) {
			return;
		} else if ( isEmpty(currentId) ) {
			currentId = playlistItems.get(0).getPlaylistId();
		}
	
		String repeatMode = PlaybackFactory.createPlaybackInstance().getRepeatMode();
		
		boolean next = false;
		
		norepeat: {
			for (PlayListItem item : get()) {
				if ( next && item.getSong() != null ) {
					play(item);
					break norepeat;
				}
				if ( currentId.equals( item.getPlaylistId() )) {
					if ( Playback.REPEAT_TRACK.equals( repeatMode ) ) {
						play( item );
						break norepeat;
					}
					next = true;
				}
			}
			
			if ( Playback.REPEAT_ALL.equals( repeatMode ) ) {
				PlayListItem item = playlistItems.get(0);
				play(item);
			}
		}
	}

	@Override
	public void play(PlayListItem item) {
		for (PlayListItem plitem : get()) {
			if ( ! plitem.equals( item ) ) {
				continue;
			}
			currentId = plitem.getPlaylistId();
			PlaybackFactory.createPlaybackInstance().stop();
			PlaybackFactory.createPlaybackInstance().play(plitem.getSong());
			Logger.info(String.format("Selected playlist item: %s (%s)",plitem.getSong().getTitle(),plitem.getPlaylistId()));
			return;
		}
		Logger.info("No playlist item was found for item: "+(item==null?"":item.getPlaylistId()));
	}

}