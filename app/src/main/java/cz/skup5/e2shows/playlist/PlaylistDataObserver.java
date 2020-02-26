package cz.skup5.e2shows.playlist;

/**
 * Created by Skup on 12.2.2017.
 */
public interface PlaylistDataObserver {

  void onActualPlaylistChanged(Playlist newActualPlaylist);
}
