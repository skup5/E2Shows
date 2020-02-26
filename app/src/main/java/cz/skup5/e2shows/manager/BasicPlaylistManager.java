package cz.skup5.e2shows.manager;

import cz.skup5.e2shows.playlist.PlaylistDataObservable;
import cz.skup5.e2shows.playlist.PlaylistDataObserver;
import cz.skup5.e2shows.playlist.Playlist;

/**
 * Basic implementation of {@link PlaylistManager} using Singleton pattern.
 * <p>
 * Created by Skup on 12.2.2017.
 *
 * @author Skup5
 */
public class BasicPlaylistManager implements PlaylistManager {
  private static BasicPlaylistManager ourInstance = new BasicPlaylistManager();

  public static BasicPlaylistManager getInstance() {
    return ourInstance;
  }

  private final PlaylistDataObservable playlistDataObservable = new PlaylistDataObservable();
  private Playlist actualPlaylist;

  private BasicPlaylistManager() {
  }

  private void notifyActualPlaylistChanged() {
    playlistDataObservable.notifyActualPlaylistChanged(actualPlaylist);
  }

  @Override
  public boolean add(Playlist newPlaylist) {
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public void remove(Playlist toRemove) {
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public Playlist getActualPlaylist() {
    return actualPlaylist;
  }

  @Override
  public void setActualPlaylist(Playlist actualPlaylist) {
    this.actualPlaylist = actualPlaylist;
    notifyActualPlaylistChanged();
  }

  @Override
  public void registerlPlaylistDataObserver(PlaylistDataObserver observer) {
    playlistDataObservable.registerObserver(observer);
  }

  @Override
  public void unregisterPlaylistDataObserver(PlaylistDataObserver observer) {
    playlistDataObservable.unregisterObserver(observer);
  }
}
