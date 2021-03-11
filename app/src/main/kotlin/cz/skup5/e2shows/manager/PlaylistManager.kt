package cz.skup5.e2shows.manager

import cz.skup5.e2shows.playlist.Playlist
import cz.skup5.e2shows.playlist.PlaylistDataObserver
import cz.skup5.e2shows.playlist.PlaylistItem

/**
 * Created by Skup on 12.2.2017.
 *
 * @author Skup5
 */
interface PlaylistManager {

    var actualPlaylist: Playlist<PlaylistItem>

    fun add(newPlaylist: Playlist<PlaylistItem>): Boolean

    fun remove(toRemove: Playlist<PlaylistItem>)

    /**
     * Register a new observer to listen for data changes.
     * The manager may publish a variety of events describing specific changes
     *
     * @param observer Observer to register
     */
    fun registerPlaylistDataObserver(observer: PlaylistDataObserver)

    /**
     * Unregister an observer currently listening for data changes.
     * The unregistered observer will no longer receive events about changes to the adapter.
     *
     * @param observer Observer to unregister
     */
    fun unregisterPlaylistDataObserver(observer: PlaylistDataObserver)
}