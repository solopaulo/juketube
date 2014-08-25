package lib.search;

import lib.Song;
import java.util.List;

public interface SearchProvider {
	public List<Song> search(String searchterm);
}
