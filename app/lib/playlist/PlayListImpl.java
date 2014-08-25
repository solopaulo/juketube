package lib.playlist;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.springframework.stereotype.Component;

import play.Logger;
import play.modules.spring.Spring;

import lib.Song;
import lib.playback.Playback;
import lib.playback.PlaybackEvent;
import lib.playback.PlaybackEventListener;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Component("playlist")
public class PlayListImpl implements PlayList,PlaybackEventListener {
	private static PlayList playListInstance;
	static {
		playListInstance = new PlayListImpl();
		getPlayback().addListener((PlaybackEventListener) playListInstance);
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
	
		String repeatMode = getPlayback().getRepeatMode();
		
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
			getPlayback().stop();
			getPlayback().play(plitem.getSong());
			Logger.info(String.format("Selected playlist item: %s (%s)",plitem.getSong().getTitle(),plitem.getPlaylistId()));
			return;
		}
		Logger.info("No playlist item was found for item: "+(item==null?"":item.getPlaylistId()));
	}

	private static Playback getPlayback() {
		return Spring.getBeanOfType( Playback.class );
	}
}