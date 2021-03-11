package cz.skup5.e2shows.playlist

internal object EmptyPlaylist : Playlist<PlaylistItem> {
    override fun actual(): Nothing? = null

    override fun next(): Nothing? = null

    override fun previous(): Nothing? = null

    override fun first(): Nothing = throw NoSuchElementException()

    override fun last(): Nothing = throw NoSuchElementException()

    override fun indexOf(item: PlaylistItem): Int = -1

}

