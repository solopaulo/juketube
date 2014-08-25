package lib.playback.mplayer;

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.apache.commons.lang.math.NumberUtils;

import play.Logger;
public enum MplayerEvent {
	PLAYBACK_POSITION,	
	PLAYBACK_STOPPED,
	PLAYBACK_STARTED,
	PLAYBACK_VOLUME_CHANGED;
	
	
	public static final String YES = "yes";
	public static final String NO  = "no";
	
	public static final String RESPONSE_PLAYBACK_POSITION = "ANS_TIME_POSITION";
	public static final String RESPONSE_PLAYBACK_PAUSED	  = "ANS_PAUSE";
	public static final String RESPONSE_PLAYBACK_VOLUME	  = "ANS_VOLUME";
	
	public static String sep = "="; 
	
	private String value;
	public MplayerEvent setValue(String val) {
		this.value = val;
		return this;
	}
	public String getValue() {
		return value;
	}
	

	public static MplayerEvent createEvent(String text) {
		MplayerEvent event = null;
		
		if ( ! isEmpty(getResponse(text,RESPONSE_PLAYBACK_POSITION)) ) {
			return PLAYBACK_POSITION.setValue(getResponse(text,RESPONSE_PLAYBACK_POSITION));
		}
		
		if ( YES.equals( getResponse(text,RESPONSE_PLAYBACK_PAUSED)) ) {
			Logger.info("Event Received: paused 1 =  %s", getResponse(text,RESPONSE_PLAYBACK_PAUSED));
			return PLAYBACK_STARTED;
		} else if ( NO.equals( getResponse(text,RESPONSE_PLAYBACK_PAUSED) ) ) {
			Logger.info("Event Received: paused 2 =  %s", getResponse(text,RESPONSE_PLAYBACK_PAUSED));			
			return PLAYBACK_STOPPED;
		} else if ( NumberUtils.isNumber( getResponse(text,RESPONSE_PLAYBACK_VOLUME) ) ) {
			return PLAYBACK_VOLUME_CHANGED.setValue( getResponse(text,RESPONSE_PLAYBACK_VOLUME) );
		}
		return event;
	}
	
	private static String getResponse(String text,String marker) {
		if ( isEmpty(text) || isEmpty(marker) ) {
			return null;
		}
		
		if ( ! text.startsWith(marker+sep) ) {
			return null;
		}
		
		String val = null;
		try {
			val = text.substring( (marker+sep).length() ).trim();
 		} catch (Exception x) { }
		return val;
	}
}
