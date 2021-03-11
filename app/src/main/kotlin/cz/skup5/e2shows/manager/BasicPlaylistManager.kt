package cz.skup5.e2shows.manager

import cz.skup5.e2shows.playlist.*

/**
 * Basic implementation of [PlaylistManager] using Singleton pattern.
 *
 *
 * Created by Skup on 12.2.2017.
 *
 * @author Skup5
 */
object BasicPlaylistManager : PlaylistManager {

    private val playlistDataObservable = PlaylistDataObservable()

    override var actualPlaylist: Playlist<PlaylistItem> = emptyPlaylist()
        set(value) {
            field = value
            notifyActualPlaylistChanged()
        }

    private fun notifyActualPlaylistChanged() {
        playlistDataObservable.notifyActualPlaylistChanged(actualPlaylist)
    }

    override fun add(newPlaylist: Playlist<PlaylistItem>): Boolean {
        throw IllegalStateException("Not implemented yet")
    }

    override fun remove(toRemove: Playlist<PlaylistItem>) {
        throw IllegalStateException("Not implemented yet")
    }

    override fun registerPlaylistDataObserver(observer: PlaylistDataObserver) {
        playlistDataObservable.registerObserver(observer)
    }

    override fun unregisterPlaylistDataObserver(observer: PlaylistDataObserver) {
        playlistDataObservable.unregisterObserver(observer)
    }

}
