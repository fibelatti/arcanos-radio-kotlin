package de.developercity.arcanosradio.core.extension

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

fun ImageView.load(url: String, @DrawableRes placeholder: Int = -1) {
    Picasso.get().load(url)
        .apply {
            if (placeholder != -1) {
                placeholder(placeholder)
                error(placeholder)
            }
        }
        .into(this)
}

fun loadImageWithFallback(
    url: String?,
    fallbackImage: Bitmap,
    @DrawableRes placeholder: Int = -1,
    onImageLoaded: (Bitmap) -> Unit
) {
    if (url == null) {
        onImageLoaded(fallbackImage)
    } else {
        Picasso.get().load(url)
            .apply {
                if (placeholder != -1) {
                    placeholder(placeholder)
                    error(placeholder)
                }
            }
            .into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable) {
                }

                override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {
                    onImageLoaded(fallbackImage)
                }

                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    onImageLoaded(bitmap)
                }
            })
    }
}
