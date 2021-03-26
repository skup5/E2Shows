package cz.skup5.e2shows.manager

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import cz.skup5.e2shows.MainActivity
import cz.skup5.e2shows.R
import cz.skup5.e2shows.service.MediaSessionService

/**
 * Keeps track of a notification and updates it automatically for a given MediaSession. This is
 * required so that the music service don't get killed during playback.
 *
 * Source [https://stackoverflow.com/questions/63501425/java-android-media-player-notification](https://stackoverflow.com/a/63606999)
 */
class MediaNotificationManager(musicContext: MediaSessionService) {
    private val channelId: String
    private val mService: MediaSessionService = musicContext
    private val mPlayAction: NotificationCompat.Action
    private val mPauseAction: NotificationCompat.Action
    val notificationManager: NotificationManager

    fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
    }

    fun getNotification(metadata: MediaMetadataCompat,
                        state: PlaybackStateCompat,
                        token: MediaSessionCompat.Token): Notification {
        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING
        val description = metadata.description
        val builder = buildNotification(state, token, isPlaying, description)
        return builder.build()
    }

    private fun buildNotification(state: PlaybackStateCompat,
                                  token: MediaSessionCompat.Token,
                                  isPlaying: Boolean,
                                  description: MediaDescriptionCompat): NotificationCompat.Builder {

        // Create the (mandatory) notification channel when running on Android Oreo.
        if (isAndroidOOrHigher) {
            createChannel()
        }
        val builder = NotificationCompat.Builder(mService, channelId)
        builder.setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token)
                        .setShowActionsInCompactView(0) // For backwards compatibility with Android L and earlier.
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        mService,
                                        PlaybackStateCompat.ACTION_STOP)))
                .setColor(ContextCompat.getColor(mService, R.color.colorPrimary))
                .setSmallIcon(android.R.drawable.ic_media_play) // Pending intent that is fired when user clicks on notification.
                .setContentIntent(createContentIntent()) // Title - Usually Song name.
                .setContentTitle(description.title) // When notification is deleted (when playback is paused and notification can be
                // deleted) fire MediaButtonPendingIntent with ACTION_PAUSE.
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService, PlaybackStateCompat.ACTION_PAUSE))
        builder.addAction(if (isPlaying) mPauseAction else mPlayAction)
        return builder
    }

    // Does nothing on versions of Android earlier than O.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (notificationManager.getNotificationChannel(channelId) == null) {
            // The user-visible name of the channel.
            val name: CharSequence = "MediaSession"
            // The user-visible description of the channel.
            val description = "MediaSession and MediaPlayer"
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(channelId, name, importance)
            // Configure the notification channel.
            mChannel.description = description
            mChannel.enableLights(true)
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(mChannel)
            Log.d(TAG, "createChannel: New channel created")
        } else {
            Log.d(TAG, "createChannel: Existing channel reused")
        }
    }

    private val isAndroidOOrHigher: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    private fun createContentIntent(): PendingIntent {
        val openUI = Intent(mService, MainActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
                mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    companion object {
        const val NOTIFICATION_ID = 412
        private val TAG = MediaNotificationManager::class.java.simpleName
        private const val REQUEST_CODE = 501
    }

    init {
        channelId = mService.packageName
        notificationManager = mService.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        mPlayAction = NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService,
                        PlaybackStateCompat.ACTION_PLAY))
        mPauseAction = NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService,
                        PlaybackStateCompat.ACTION_PAUSE))
        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        notificationManager.cancelAll()
    }
}