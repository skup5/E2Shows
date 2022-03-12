package cz.skup5.e2shows

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.widget.MediaController.MediaPlayerControl
import cz.skup5.e2shows.playlist.PlaylistItem

/**
 * This class allows playing of media sources. It's adapter for Android [MediaPlayer].
 */
abstract class APlayerAdapter(
        protected var mediaPlayer: MediaPlayer = createDefaultMediaPlayer()
) : MediaPlayerControl, MediaPlayer.OnPreparedListener {


    private val TAG = "APlayerAdapter"

    companion object {
        private fun createDefaultMediaPlayer(): MediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                    AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            )

        }
    }

    init {
        mediaPlayer.setOnPreparedListener { onPrepared(it) }
    }

    abstract fun next()

    //    /**
    //     * Invoked when user click "next".
    //     *
    //     * @param actualItem the last selected (played) item or null if none item wasn't selected yet
    //     * @param nextItem   the next item in actual {@link Playlist}
    //     */
    // abstract void onNext(Item actualItem, Item nextItem);
    //    /**
    //     * Invoked when user click "previous".
    //     *
    //     * @param actualItem   the last selected (played) item or null if none item wasn't selected yet
    //     * @param previousItem the previous item in actual {@link Playlist}
    //     */
    //abstract void onPrevious(Item actualItem, Item previousItem);
    abstract fun previous()

    override fun getAudioSessionId(): Int {
        return 0
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun start() {
        mediaPlayer.start()
    }

    fun stop() {
        if (isPlaying) {
            mediaPlayer.pause()
        }
        mediaPlayer.stop()
        mediaPlayer.reset()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun getDuration(): Int {
        return mediaPlayer.duration / 1000
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition / 1000
    }

    fun getCurrentTrack() = object : PlaylistItem {
        override val title: String
            get() {
                Log.e(TAG, "Not yet implemented")
                return "Mock single"
            }
        override val artist: String
            get() {
                Log.e(TAG, "Not yet implemented")
                return "Mock artist"
            }
    }

    override fun seekTo(pos: Int) {
        mediaPlayer.seekTo(pos * 1000)
    }

    fun release() {
        mediaPlayer.release()
    }

    fun prepareAsync(url: String) {
        mediaPlayer.apply {
            setDataSource(url)
            prepareAsync() // might take long! (for buffering, etc)
        }
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        start()
    }
}
