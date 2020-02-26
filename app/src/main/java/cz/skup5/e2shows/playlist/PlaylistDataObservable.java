package cz.skup5.e2shows.playlist;

import android.database.Observable;

/**
 * Created by Skup on 13.2.2017.
 *
 * @author Skup5
 */
public class PlaylistDataObservable extends Observable<PlaylistDataObserver> {

  /**
   * Notify any registered observers that the currently reflected {@code actualPlaylist} was changed.
   *
   * @param actualPlaylist the new actual playlist
   */
  public void notifyActualPlaylistChanged(Playlist actualPlaylist) {
    for (PlaylistDataObserver observer : mObservers) {
      observer.onActualPlaylistChanged(actualPlaylist);
    }
  }
}
