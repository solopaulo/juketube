package lib.resolver;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.BufferedReader;
import java.io.InputStreamReader;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import play.Play;
import play.cache.Cache;

@Component("downloadResolverYoutube")
public class YoutubeDownloadResolver implements DownloadResolver {
	private static final String PYTHON_BIN		= "python";
	private static final String PYTHON_BIN_WINDOWS = "python.exe";
	private static final String RESOLVER_BIN;
	static {
		String fs = System.getProperty("file.separator");
		RESOLVER_BIN = Play.applicationPath+fs+"python"+fs+"youtube-dl";
	}
	private static final String RESOLVER_MODE	= "-g";
	private static final Logger log = Logger.getLogger(DownloadResolver.class);
	private static final String DOWNLOAD_FORMAT_OPTION	 = "-f";
	private static final String DOWNLOAD_FORMAT_MP4_MAIN = "18";
	private static final String DOWNLOAD_FORMAT_3GP		 = "17";
	private static final String DOWNLOAD_FORMAT_FLV_MAIN = "34";
	private static final String [] DOWNLOAD_PREFS = new String[] {
		DOWNLOAD_FORMAT_MP4_MAIN,
		DOWNLOAD_FORMAT_3GP,
		DOWNLOAD_FORMAT_FLV_MAIN
	};
	private static Logger logger = Logger.getLogger(DownloadResolver.class);
	
	
	public String resolveUrl(String url) throws ResolverException {
		if ( isEmpty(url) ) {
			throw new ResolverException("No URL provided for resolution");
		}
		String resolved = (String) Cache.get(url);
		if ( resolved != null ) {
			logger.info("++ USING CACHED URL for link "+url);
			return resolved;
		}
		
		logger.info("--> RESOLVING URL for "+url);
		ProcessBuilder pb = null;
		try {
			for ( String format : DOWNLOAD_PREFS ) {				 
				pb = new ProcessBuilder(getPythonBin(),RESOLVER_BIN,DOWNLOAD_FORMAT_OPTION,format,RESOLVER_MODE,url);
				pb.redirectErrorStream(true);
				Process p = pb.start();
				p.waitFor();
				String line = resolved = "";
				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));			
				while( (line = reader.readLine()) != null ) {
					resolved += line; 
				}
				reader.close();
				if ( resolved.length() > 0 && ! resolved.startsWith("ERROR")) {
					break;
				} 
			}
		} catch (Exception x) {
			log.error(x.getMessage());
			throw new ResolverException(x);
		}
		Cache.set(url, resolved,"1h");
		logger.info(" - set new url in cache");
		return resolved;
	}
	
	static String getPythonBin() {
		if ( ! osIsWindows() ) {
			return PYTHON_BIN;
		}
		String fs = System.getProperty("file.separator");
		String path = System.getProperty("application.path")+fs+"python"+fs+PYTHON_BIN_WINDOWS;
		return path;
	}
	
	static boolean osIsWindows() {
		return System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;
	}

}
