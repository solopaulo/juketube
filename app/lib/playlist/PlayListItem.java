package lib.playlist;

import static org.apache.commons.lang.StringUtils.isEmpty;
import lib.Song;
import play.libs.Codec;
import play.libs.Crypto;
public class PlayListItem {
	
	private Song song;
	public Song getSong() {
		return song;
	}
	public void setSong(Song song) {
		this.song = song;
		setPlaylistId( getPlaylistId() );
	}
	
	private String playlistId;
	public String getPlaylistId() {
		if ( isEmpty(playlistId) && song != null ) {
			playlistId = createPlayListId(getSong());
		}
		return playlistId;
	}
	public void setPlaylistId(String playlistId) {
		this.playlistId = playlistId;
	}
	
	private static String createPlayListId(Song song) {
		return Crypto.passwordHash(song.getId()+Codec.UUID());
	}
	
	@Override
	public boolean equals(Object o) {
		if ( !( o instanceof PlayListItem ) ) {
			return false;
		}
		PlayListItem item = (PlayListItem) o;
		if ( isEmpty(getPlaylistId()) || item == null || isEmpty(item.getPlaylistId())) {
			return false;
		}
		
		return getPlaylistId().equals( item.getPlaylistId() );
	}
	
}
