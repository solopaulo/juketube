package lib;

import java.util.List;

public interface PlayList {

	public List<PlayListItem> get();
	public PlayListItem add(Song song);
	public void remove(PlayListItem item);
	public void nextItem();
	public void clear();
	public void play(PlayListItem item);
}
