package cz.skup5.e2shows.playlist

/**
 * Created by Skup on 12.2.2017.
 */
interface PlaylistDataObserver {
    fun onActualPlaylistChanged(newActualPlaylist: Playlist<*>)
}