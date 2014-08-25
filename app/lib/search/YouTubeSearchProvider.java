package lib.search;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import play.cache.Cache;
import play.libs.Codec;

import sun.util.calendar.LocalGregorianCalendar.Date;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.data.Feed;
import com.google.gdata.data.BaseFeed.FeedHandler;
import com.google.gdata.data.media.mediarss.MediaContent;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;

import lib.Song;
import lib.Song;

public class YouTubeSearchProvider implements SearchProvider {
	private static final String YOUTUBE_DATA_API_URL = "http://gdata.youtube.com/feeds/api/videos";
	private static final String YOUTUBE_APPLICATION_NAME = "juketube";
	private static final Logger log = Logger.getLogger(YouTubeSearchProvider.class);
	public YouTubeSearchProvider() {		
	}
	
	@Override
	public List<Song> search(String searchterm) {		
		List<Song> songs = new ArrayList<Song>();
		YouTubeQuery query = null;
		try {
			query = new YouTubeQuery(new URL(YOUTUBE_DATA_API_URL) );
		} catch (Exception x) {
			log.error(x.getMessage());
			x.printStackTrace();
			return songs;
		}
		query.setOrderBy( YouTubeQuery.OrderBy.RELEVANCE );
		query.setSafeSearch( YouTubeQuery.SafeSearch.NONE );
		query.setFullTextQuery(searchterm);
		
		YouTubeService service = new YouTubeService(YOUTUBE_APPLICATION_NAME);
		try {
			VideoFeed feed = service.query(query, VideoFeed.class);
			for (VideoEntry entry : feed.getEntries() ) {
				Song song = createSongFromEntry(entry,searchterm);				
				if ( song == null ) {
					continue;
				}				
				songs.add( song );
			}
		} catch (Exception x) {
			x.printStackTrace();
			log.error(x.getMessage());
		}
		return songs;
	}

	private Song createSongFromEntry(VideoEntry entry,String searchterm) {
		if ( entry == null ) {
			return null;
		}
		Song song = new Song();
		
		int duration = 0;
		try {
			List<MediaContent> contents = entry.getMediaGroup().getContents();
			duration = contents.size() > 0 ? contents.get(0).getDuration() : 0;
		} catch (Exception x) { 
			log.error(x.getMessage());			
		}

		String tim = String.format("%d:%02d", 
				TimeUnit.SECONDS.toMinutes(duration),
				duration - (TimeUnit.SECONDS.toMinutes(duration) * 60));
		song.setTitle( entry.getTitle().getPlainText());
		song.setLength( duration );
		song.setTime(tim);
		song.setUrl( entry.getHtmlLink().getHref());
		song.setSearchTerm( searchterm );
		song.setId( song.getId() );
		return song;
	}
}
