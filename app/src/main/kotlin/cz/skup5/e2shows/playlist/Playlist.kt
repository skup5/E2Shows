package cz.skup5.e2shows.playlist

/**
 * @author Skup5
 */
interface Playlist<Item : PlaylistItem> {
    /**
     * Returns the last selected [PlaylistItem] or null if none item wasn't selected yet.
     *
     * @return playlist item or null
     */
    fun actual(): Item?

    /**
     * Returns [PlaylistItem] after [actual].
     * If `actual` is null, it is called [first].
     * If `actual` is `last`, returns null;
     *
     * @return playlist item after `actual`
     */
    operator fun next(): Item?

    /**
     * Returns [PlaylistItem] before [.actual].
     * If `actual` is null or `first`, returns null.
     *
     * @return playlist item before `actual`
     */
    fun previous(): Item?

    /**
     * @return
     */
    fun first(): Item

    /**
     * @return
     */
    fun last(): Item

    /**
     * Index of item in playlist.
     *
     * @param item the requested item
     * @return index of item or -1 if doesn't contain this `item`
     */
    fun indexOf(item: Item): Int
}

fun emptyPlaylist(): Playlist<PlaylistItem> = EmptyPlaylist