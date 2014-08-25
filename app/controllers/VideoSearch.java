package controllers;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lib.Song;
import lib.search.SearchProvider;
import lib.search.YouTubeSearchProvider;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.Controller;
import static org.apache.commons.lang.StringUtils.isEmpty;
public class VideoSearch extends Controller {

	private static final int MAX_RESULTS = 15;
	private static final String CACHE_SEARCH = "searchterm_";
	private static final String LAST_SEARCH = "_lastSearchResult";
	
	public static void search() {
		String search = request.params.get("search");
		String maxresults = request.params.get("results");

		List<Song>songs = null;
		// don't care if no results
		if ( isEmpty(search) ) {
			songs = getLastSearchResult();
			if ( songs == null ) {
				songs = new ArrayList<Song>();
			}
		} else {
			songs = doSearch(search);
		}
		
		int truncate = MAX_RESULTS;
		if ( ! isEmpty( maxresults) ) {
			try {
				truncate = Integer.parseInt(maxresults);
			} catch (Exception x) { }
		}
		setLastSearchResult(songs);
		// truncate results
		if ( songs.size() >= truncate ) {
			songs = songs.subList(0,  truncate);
		}
		
		String songsJson = new Gson().toJson(songs.toArray(new Song[] { }));
		render("/Application/results.html",songs,songsJson);
	}
		
	public static void index() {
		render("/Application/search.html");		
	}
	
	public static List<Song> doSearch(String search) {
		// try to decode if decoded
		try {
			search = URLDecoder.decode(search,"UTF-8");
		} catch (Exception x) { }

		String key = CACHE_SEARCH + Codec.encodeBASE64(search);
		List<Song>songs = (List<Song>) Cache.get(key);
		
		if ( songs == null || songs.size() == 0 ) {
			// get a search provider
			SearchProvider provider = new YouTubeSearchProvider();
			songs = provider.search(search);
		}
		return songs == null ? new ArrayList<Song>() : songs;
	}
	
	private static String getLSRKey() {
		return session.getId()+LAST_SEARCH;
	}
	private static List<Song> getLastSearchResult() {
		return (List<Song>) Cache.get(getLSRKey());
	}
	
	private static void setLastSearchResult(List<Song>songs) {
		Cache.set(getLSRKey(),songs);
	}
}
