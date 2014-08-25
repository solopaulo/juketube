package lib;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonArray;
import com.google.gson.JsonSerializer;

import play.libs.Codec;
import play.libs.Crypto;

public class Song implements Serializable {

	private String id;

	public String getId() {
		if ( id == null ) {
			id = generateId();
		}
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	private String url;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
		generateId();
	}
	
	private String playbackUrl;
	public String getPlaybackUrl() {
		return playbackUrl;
	}

	public void setPlaybackUrl(String playbackUrl) {
		this.playbackUrl = playbackUrl;
	}

	private String title;
	
	public String getTitle() {
		return title;
	}

	private String artist;
	
	public String getArtist() {
		return artist;
	}

	private int length;
	
	public int getLength() {
		return length;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public void setLength(int length) {
		this.length = length;
	}
	
	private String time;
	public void setTime(String tim) {
		this.time = tim;
	}
	
	
	public String getTime() {
		return time;
	}
	
	private String generateId() {
		return Codec.encodeBASE64(getUrl()+searchterms);
	}
	
	private String searchterms;
	public void setSearchTerm(String searchterms) {
		this.searchterms = searchterms;
	}
	
	public String getSearchTerm() {
		return searchterms;
	}
}
