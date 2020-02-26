package cz.skup5.e2shows.manager;

import cz.skup5.e2shows.playlist.Playlist;
import cz.skup5.e2shows.playlist.PlaylistDataObserver;

/**
 * Created by Skup on 12.2.2017.
 *
 * @author Skup5
 */
public interface PlaylistManager {

  boolean add(Playlist newPlaylist);

  void remove(Playlist toRemove);

  Playlist getActualPlaylist();

  void setActualPlaylist(Playlist actualPlaylist);

  /**
   * Register a new observer to listen for data changes.
   * The manager may publish a variety of events describing specific changes
   *
   * @param observer Observer to register
   */
  void registerlPlaylistDataObserver(PlaylistDataObserver observer);

  /**
   * Unregister an observer currently listening for data changes.
   * The unregistered observer will no longer receive events about changes to the adapter.
   *
   * @param observer Observer to unregister
   */
  void unregisterPlaylistDataObserver(PlaylistDataObserver observer);
}
