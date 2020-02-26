package cz.skup5.e2shows.playlist

import android.database.Observable

/**
 * Created by Skup on 13.2.2017.
 *
 * @author Skup5
 */
class PlaylistDataObservable : Observable<PlaylistDataObserver>() {
    /**
     * Notify any registered observers that the currently reflected `actualPlaylist` was changed.
     *
     * @param actualPlaylist the new actual playlist
     */
    fun notifyActualPlaylistChanged(actualPlaylist: Playlist<*>) {
        for (observer in mObservers) {
            observer.onActualPlaylistChanged(actualPlaylist)
        }
    }
}