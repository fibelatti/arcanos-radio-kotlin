package de.developercity.arcanosradio.features.streaming

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import de.developercity.arcanosradio.R
import de.developercity.arcanosradio.core.extension.getSystemService
import de.developercity.arcanosradio.core.extension.inOreo
import de.developercity.arcanosradio.core.provider.ResourceProvider
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
import javax.inject.Inject

private const val CHANNEL_ID = "arcanos_media_playback_channel"
private const val MEDIA_SESSION_TAG = "ARCANOS_MEDIA_SESSION_TAG"
private const val NOTIFICATION_ID = 1337

class StreamingNotificationManager @Inject constructor(
    context: Context,
    private val resourceProvider: ResourceProvider
) {

    private val notificationManager: NotificationManager? = context.getSystemService<NotificationManager>()
    private val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
    private val mediaSession: MediaSessionCompat = MediaSessionCompat(context, MEDIA_SESSION_TAG)

    fun getNotificationId(): Int = NOTIFICATION_ID

    fun createNotification(
        song: String,
        artist: String,
        albumArt: Bitmap,
        @DrawableRes actionIcon: Int,
        @StringRes actionDescription: Int,
        actionPendingIntent: PendingIntent,
        tapIntent: PendingIntent? = null
    ): Notification {
        createNotificationChannel()

        return notificationBuilder
            .setStyle(MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0)
            )
            .apply { mActions.clear() }
            .addAction(
                NotificationCompat.Action.Builder(
                    actionIcon,
                    resourceProvider.getString(actionDescription),
                    actionPendingIntent
                ).build()
            )
            .setSmallIcon(R.drawable.ic_radio)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .apply { tapIntent?.let(::setContentIntent) }
            .setContentTitle(song)
            .setContentText(artist)
            .setLargeIcon(albumArt)
            .build()
    }

    fun showNotification(
        song: String,
        artist: String,
        albumArt: Bitmap,
        @DrawableRes actionIcon: Int,
        @StringRes actionDescription: Int,
        actionPendingIntent: PendingIntent
    ) {
        notificationManager?.notify(
            NOTIFICATION_ID,
            createNotification(
                song = song,
                artist = artist,
                albumArt = albumArt,
                actionIcon = actionIcon,
                actionDescription = actionDescription,
                actionPendingIntent = actionPendingIntent
            )
        )
    }

    fun showNowPlayingNotification(
        nowPlaying: NowPlaying,
        defaultAlbumArt: Bitmap,
        @DrawableRes actionIcon: Int,
        @StringRes actionDescription: Int,
        actionPendingIntent: PendingIntent
    ) {
        with(nowPlaying) {
            Picasso.get().load(song.albumArt)
                .placeholder(R.drawable.arcanos)
                .into(object : Target {
                    override fun onPrepareLoad(placeHolderDrawable: Drawable) {
                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {
                        showNotification(
                            song = song.name,
                            artist = song.artist.name,
                            albumArt = defaultAlbumArt,
                            actionIcon = actionIcon,
                            actionDescription = actionDescription,
                            actionPendingIntent = actionPendingIntent
                        )
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                        showNotification(
                            song = song.name,
                            artist = song.artist.name,
                            albumArt = bitmap,
                            actionIcon = actionIcon,
                            actionDescription = actionDescription,
                            actionPendingIntent = actionPendingIntent
                        )
                    }
                })
        }
    }

    private fun createNotificationChannel() {
        inOreo {
            notificationManager?.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    resourceProvider.getString(R.string.now_playing_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = resourceProvider.getString(R.string.now_playing_notification_channel_description)
                    setShowBadge(false)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
            )
        }
    }
}
