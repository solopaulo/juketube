package lib.resolver;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import play.Play;
import play.cache.Cache;

public interface DownloadResolver {
	public String resolveUrl(String url) throws ResolverException;
}
