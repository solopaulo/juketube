package controllers;

import java.util.List;

import lib.Song;
import lib.playlist.PlayList;
import lib.playlist.PlayListItem;
import lib.resolver.DownloadResolver;
import lib.resolver.ResolverException;
import play.Logger;
import play.cache.Cache;
import play.libs.F.Promise;
import play.modules.spring.Spring;
import play.mvc.Controller;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class PlayListController extends Controller {

		public static void getPlaylist() {
			List<PlayListItem>list = getPlayList().get();
			String playlistItemsJson = new com.google.gson.Gson().toJson( list.toArray( new PlayListItem[] { }));
			render("/Application/playlist.html",list,playlistItemsJson);
		}
		
		public static void clearPlaylist() {
			getPlayList().clear();
		}
		
		public static void addToPlaylist(Song song) {
			if ( isEmpty(song.getPlaybackUrl()) && !isEmpty(song.getUrl()) ) {
				Promise<String>urlPromise = new Promise<String>();
				try {
					urlPromise.invoke( Spring.getBeanOfType(DownloadResolver.class).resolveUrl(song.getUrl()) );
					song.setPlaybackUrl( await(urlPromise) );
				} catch (ResolverException rex) {
					Logger.warn("Unable to add song to playlist: %s (%s)",song.getUrl(),rex.getMessage());
				}
				
			}
			PlayListItem item = getPlayList().add(song);
			if ( item != null ) {
				renderJSON(item);	
			}
		}
		
		public static void removeFromPlaylist(PlayListItem playlistItem) {
			if ( playlistItem != null ) {
				getPlayList().remove(playlistItem);
			}
		}
		
		public static void play(PlayListItem playlistItem) {
			if ( playlistItem != null ) {
				getPlayList().play(playlistItem);
			}
		}
		
		private static PlayList getPlayList() {
			return Spring.getBeanOfType(PlayList.class);
		}
}
