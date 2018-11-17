package de.developercity.arcanosradio.features.streaming.device

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import de.developercity.arcanosradio.R
import de.developercity.arcanosradio.core.extension.getSystemService
import de.developercity.arcanosradio.core.extension.inOreo
import de.developercity.arcanosradio.core.extension.loadImageWithFallback
import de.developercity.arcanosradio.core.provider.ResourceProvider
import javax.inject.Inject

private const val CHANNEL_ID = "arcanos_media_playback_channel"
private const val NOTIFICATION_ID = 1337

class StreamingNotificationManager @Inject constructor(
    context: Context,
    private val resourceProvider: ResourceProvider
) {

    private val notificationManager: NotificationManager? = context.getSystemService<NotificationManager>()
    private val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)

    fun getNotificationId(): Int = NOTIFICATION_ID

    fun createNotification(
        song: String,
        artist: String,
        albumArt: Bitmap,
        @DrawableRes actionIcon: Int,
        @StringRes actionDescription: Int,
        actionPendingIntent: PendingIntent,
        tapIntent: PendingIntent? = null,
        deleteIntent: PendingIntent? = null,
        mediaSessionToken: MediaSessionCompat.Token? = null
    ): Notification {
        createNotificationChannel()

        return notificationBuilder
            .setSmallIcon(R.drawable.ic_radio)
            .apply {
                mediaSessionToken?.let {
                    setStyle(MediaStyle()
                        .setMediaSession(mediaSessionToken)
                        .setShowActionsInCompactView(0)
                    )
                }
            }
            .setContentTitle(song)
            .setContentText(artist)
            .setLargeIcon(albumArt)
            .clearActions()
            .addAction(
                NotificationCompat.Action.Builder(
                    actionIcon,
                    resourceProvider.getString(actionDescription),
                    actionPendingIntent
                ).build()
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .apply { tapIntent?.let(::setContentIntent) }
            .apply { deleteIntent?.let(::setDeleteIntent) }
            .build()
    }

    fun showNowPlayingNotification(
        song: String,
        artist: String,
        albumArtUrl: String? = null,
        defaultAlbumArt: Bitmap,
        @DrawableRes actionIcon: Int,
        @StringRes actionDescription: Int,
        actionPendingIntent: PendingIntent
    ) {
        loadImageWithFallback(
            url = albumArtUrl,
            fallbackImage = defaultAlbumArt,
            placeholder = R.drawable.arcanos
        ) { loadedAlbumArt ->
            notificationManager?.notify(
                NOTIFICATION_ID,
                createNotification(
                    song = song,
                    artist = artist,
                    albumArt = loadedAlbumArt,
                    actionIcon = actionIcon,
                    actionDescription = actionDescription,
                    actionPendingIntent = actionPendingIntent
                )
            )
        }
    }

    private fun createNotificationChannel() {
        inOreo {
            notificationManager?.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    resourceProvider.getString(R.string.now_playing_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = resourceProvider.getString(R.string.now_playing_notification_channel_description)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setShowBadge(false)
                }
            )
        }
    }

    private fun NotificationCompat.Builder.clearActions() = apply { mActions.clear() }
}
