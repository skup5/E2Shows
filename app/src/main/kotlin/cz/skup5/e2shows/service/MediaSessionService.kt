package cz.skup5.e2shows.service

import android.app.AlertDialog
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import android.view.View
import cz.skup5.e2shows.AudioController
import cz.skup5.e2shows.AudioController.AudioPlayerControl
import cz.skup5.e2shows.MainActivity
import cz.skup5.e2shows.PrepareStream
import cz.skup5.e2shows.R
import cz.skup5.e2shows.manager.MediaNotificationManager
import cz.skup5.e2shows.playlist.PlaylistItem
import cz.skup5.e2shows.record.RecordItem
import cz.skup5.e2shows.utils.NetworkUtils

/**
 *
 * Source [https://stackoverflow.com/questions/63501425/java-android-media-player-notification](https://stackoverflow.com/a/63606999)
 */
class MediaSessionService : Service() {

    private var providers: Providers? = null
    private val mBinder: IBinder = LocalBinder()

    //    val audioController: AudioController<RecordItem?>? = null
    var mediaPlayer: MediaPlayer? = null
    var audioPlayerControl: AudioPlayerControl? = null
    private var mMediaNotificationManager: MediaNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null

    val metadata: MediaMetadataCompat
        get() {
            val currentTrack = providers?.getAudioController()?.playlist?.actual()
            return MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack?.artist ?: "")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack?.title ?: "")
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                            mediaPlayer?.duration?.toLong() ?: 0L)
                    .build()
        }

    private val state: PlaybackStateCompat
        get() {
            val actions = if (mediaPlayer?.isPlaying == true) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY
            val state = if (mediaPlayer?.isPlaying == true) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            return PlaybackStateCompat.Builder()
                    .setActions(actions)
                    .setState(state,
                            mediaPlayer?.currentPosition?.toLong() ?: 0L,
                            1.0f,
                            SystemClock.elapsedRealtime())
                    .build()
        }

    override fun onCreate() {
        super.onCreate()
        initMediaPlayer()
        initAudioController()
        mMediaNotificationManager = MediaNotificationManager(this)
        initMediaSession()
        val notification = mMediaNotificationManager!!.getNotification(
                metadata, state, mediaSession!!.sessionToken)
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Intent.ACTION_MEDIA_BUTTON == intent.action) {
            val keyEvent = intent.extras!![Intent.EXTRA_KEY_EVENT] as KeyEvent?
            if (keyEvent!!.keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                mediaPlayer?.pause()
            } else {
                mediaPlayer?.start()
            }
        } else if (ACTION_BIND == intent.action) {
            providers = intent.extras!![EXTRA_PROVIDERS] as Providers?

        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? = mBinder

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "SOME_TAG")
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                mediaPlayer!!.start()
            }

            override fun onPause() {
                mediaPlayer!!.pause()
            }
        })
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer!!.setOnPreparedListener(MediaPlayer.OnPreparedListener
        /**
         * Called when the media file is ready for playback.
         *
         * @param mp the MediaPlayer that is ready for playback
         */
        {
            providers?.getAudioController()?.apply {
                isEnabled = true
                setUpSeekBar()
                /* play mp3 */
                clickOnPlayPause()
            }
        })
        mediaPlayer!!.setOnCompletionListener { providers?.getAudioController()?.onCompletion() }
    }

    private fun initAudioController() {
        audioPlayerControl = object : AudioPlayerControl() {
            override fun start() {
                mediaPlayer!!.start()
            }

            override fun stop() {
                if (mediaPlayer != null) {
                    if (isPlaying) {
                        mediaPlayer!!.pause()
                    }
                    mediaPlayer!!.stop()
                    mediaPlayer!!.reset()
                }
            }

            override fun pause() {
                mediaPlayer!!.pause()
            }

            override fun getDuration(): Int {
                return mediaPlayer!!.duration / 1000
            }

            override fun getCurrentPosition(): Int {
                return mediaPlayer!!.currentPosition / 1000
            }

            override fun seekTo(pos: Int) {
                mediaPlayer!!.seekTo(pos * 1000)
            }

            override fun isPlaying(): Boolean {
                return if (mediaPlayer != null) {
                    mediaPlayer!!.isPlaying
                } else false
            }

            override fun next() {
                val audioController = providers?.getAudioController()?.let {
                    val nextItem = it.playlist.next()
                    if (nextItem != null) {
                        //TODO: onRecordItemClick(nextItem, it.playlist.indexOf(nextItem))
                    }
                }
            }

            override fun previous() {
                if (currentPosition > 3) {
                    seekTo(0)
                } else {
                    val audioController = providers?.getAudioController()
                    val previousItem = audioController?.playlist?.previous()
                    if (previousItem == null) {
                        seekTo(0)
                    } else {
                        //TODO:  onRecordItemClick(previousItem, audioController.playlist.indexOf(previousItem))
                    }
                }
            }

            override fun canPause(): Boolean {
                return true
            }
        }

    }

    fun prepareMediaPlayerSource(url: String?) {
        if (mediaPlayer == null) {
            initMediaPlayer()
        }
        if (!NetworkUtils.isNetworkConnected()) {
            //TODO: noConnectionToast()
            return
        }
//        val ps = PrepareStream(this, mediaPlayer)
//        ps.setOnErrorListener {
//            AlertDialog.Builder(this)
//                    .setTitle(R.string.loading)
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setMessage(R.string.error_on_loading)
//                    .setPositiveButton(MainActivity.TRY_NEXT_RECORD) { dialog, _ ->
//                        audioPlayerControl!!.next()
//                        dialog.dismiss()
//                    }.setCancelable(true)
////                .setNegativeButton(STORNO, new DialogInterface.OnClickListener() {
////                  @Override
////                  public void onClick(DialogInterface dialogInterface, int i) {
////                    dialogInterface.dismiss();
////                  }
////                })
//                    .show()
//        }
//        ps.execute(url)
    }

    companion object {
        const val TAG = "MediaSessionService"
        const val NOTIFICATION_ID = 888
        const val ACTION_BIND = "android.intent.action.mediasessionservice.BIND"
        const val EXTRA_PROVIDERS = "android.intent.extra.mediasessionservice.PROVIDERS"
    }

    inner class LocalBinder : Binder() {
        fun getService(): MediaSessionService = this@MediaSessionService
    }

    interface Providers {

        fun getAudioController(): AudioController<PlaylistItem>
    }
}