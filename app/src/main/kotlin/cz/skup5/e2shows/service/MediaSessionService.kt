package cz.skup5.e2shows.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import cz.skup5.e2shows.APlayerAdapter
import cz.skup5.e2shows.manager.MediaNotificationManager
import cz.skup5.e2shows.playlist.PlaylistItem
import cz.skup5.e2shows.utils.NetworkUtils

/**
 *
 * Source [https://stackoverflow.com/questions/63501425/java-android-media-player-notification](https://stackoverflow.com/a/63606999)
 * Source [https://developer.android.com/guide/topics/media/mediaplayer#mpandservices](https://developer.android.com/guide/topics/media/mediaplayer#mpandservices)
 */
class MediaSessionService : Service() {

    private lateinit var wifiLock: WifiManager.WifiLock
    private val mBinder: IBinder = LocalBinder()

    var audioPlayerControl: APlayerAdapter? = null
    private var mMediaNotificationManager: MediaNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null

    val metadata: MediaMetadataCompat
        get() {
            val currentTrack: PlaylistItem? = audioPlayerControl?.getCurrentTrack()
            return MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack?.artist ?: "")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack?.title ?: "")
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                            audioPlayerControl?.duration?.toLong() ?: 0L)
                    .build()
        }

    private val state: PlaybackStateCompat
        get() {
            val actions = if (audioPlayerControl?.isPlaying == true) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY
            val state = if (audioPlayerControl?.isPlaying == true) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            return PlaybackStateCompat.Builder()
                    .setActions(actions)
                    .setState(state,
                            audioPlayerControl?.currentPosition?.toLong() ?: 0L,
                            1.0f,
                            SystemClock.elapsedRealtime())
                    .build()
        }

    override fun onCreate() {
        super.onCreate()
        initAudioPlayerControl()
        mMediaNotificationManager = MediaNotificationManager(this)
        initMediaSession()
        val notification = mMediaNotificationManager!!.getNotification(
                metadata, state, mediaSession!!.sessionToken)
        startForeground(NOTIFICATION_ID, notification)

        wifiLock = createWifiLock()

    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerControl?.release()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Intent.ACTION_MEDIA_BUTTON == intent.action) {
            val keyEvent = intent.extras!![Intent.EXTRA_KEY_EVENT] as KeyEvent?
            if (keyEvent!!.keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                audioPlayerControl?.pause()
            } else {
                audioPlayerControl?.start()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder = mBinder

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "SOME_TAG")
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                audioPlayerControl?.start()
            }

            override fun onPause() {
                audioPlayerControl?.pause()
            }
        })
    }

    private fun initAudioPlayerControl() {
        audioPlayerControl = object : APlayerAdapter() {

            init {
                mediaPlayer.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            }

            override fun next() {
                /* val audioController = providers?.getAudioController()?.let {
                     val nextItem = it.playlist.next()
                     if (nextItem != null) {
                         //TODO: onRecordItemClick(nextItem, it.playlist.indexOf(nextItem))
                     }
                 }*/
            }

            override fun previous() {
                if (currentPosition > 3) {
                    seekTo(0)
                } else {
                    /*     val audioController = providers?.getAudioController()
                         val previousItem = audioController?.playlist?.previous()
                         if (previousItem == null) {
                             seekTo(0)
                         } else {
                             //TODO:  onRecordItemClick(previousItem, audioController.playlist.indexOf(previousItem))
                         }*/
                }
            }

            override fun canPause(): Boolean {
                return true
            }

            override fun onPrepared(mediaPlayer: MediaPlayer) {
                super.onPrepared(mediaPlayer)

            }

            override fun start() {
                super.start()
                wifiLock.acquire()
            }

            override fun pause() {
                super.pause()
                releaseWifiLock()
            }

            override fun stop(){
                super.stop()
                releaseWifiLock()
            }

            override fun release(){
                super.release()
                releaseWifiLock()
            }
        }

    }

    private fun releaseWifiLock() {
        if (wifiLock.isHeld) {
            wifiLock.release()
        }
    }

    private fun createWifiLock(): WifiManager.WifiLock {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "E2Shows.MediaSessionService.WifiLock")
    }

    fun prepareMediaPlayerSource(url: String) {

        if (!NetworkUtils.isNetworkConnected(this)) {
            //TODO: noConnectionToast()
            return
        }

        audioPlayerControl?.prepareAsync(url)
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

        wifiLock.acquire()
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

}